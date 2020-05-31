package ru.mirea.dikanev.nikita.common.server.handler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import ru.mirea.dikanev.nikita.common.math.Rectangle;
import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;
import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnectorProvider;
import ru.mirea.dikanev.nikita.common.server.exception.AuthenticationException;
import ru.mirea.dikanev.nikita.common.server.processor.MessageProcessor;
import ru.mirea.dikanev.nikita.common.server.receiver.MessageReceiver;
import ru.mirea.dikanev.nikita.common.server.receiver.SimpleMessageReceiver;
import ru.mirea.dikanev.nikita.common.server.sender.MessageSender;
import ru.mirea.dikanev.nikita.common.server.sender.SimpleMessageSender;
import ru.mirea.dikanev.nikita.common.server.service.connector.CellManagerConnectorService;
import ru.mirea.dikanev.nikita.common.server.service.connector.ConnectorService;

@Log4j2
public class CellManagerHandler extends SimpleMessageHandler {

    public static final int WIDTH_WOLD = 1000;
    public static final int HEIGHT_WOLD = 1000;

    private List<Rectangle> rectangles;
    private Map<Rectangle, CellInfo> cells = new ConcurrentHashMap<>();

    private List<ChannelConnector> preparedConnectors = null;

    private CellManagerHandler() {
        rectangles = List.of(new Rectangle(0, HEIGHT_WOLD / 2, WIDTH_WOLD / 2, 0),
                new Rectangle(WIDTH_WOLD / 2, HEIGHT_WOLD / 2, WIDTH_WOLD, 0),
                new Rectangle(0, HEIGHT_WOLD, WIDTH_WOLD / 2, HEIGHT_WOLD / 2),
                new Rectangle(WIDTH_WOLD / 2, HEIGHT_WOLD, WIDTH_WOLD, HEIGHT_WOLD / 2));
    }

    @Override
    public void closeConnection(ChannelConnector connector) {
        super.closeConnection(connector);
        cells.remove(cells.entrySet()
                .stream()
                .filter(e -> e.getValue().channelConnector.equals(connector))
                .findFirst()
                .map(Entry::getKey)
                .orElse(null));
    }

    public static CellManagerHandler create(MessageProcessor processor, InetSocketAddress... addresses)
            throws IOException, AuthenticationException {
        CellManagerHandler handler = buildHandler(processor);
        for (InetSocketAddress address : addresses) {
            handler.bind(address);
        }

        return handler;
    }

    private static CellManagerHandler buildHandler(MessageProcessor processor) {
        CellManagerHandler handler = new CellManagerHandler();
        ConnectorService service = new CellManagerConnectorService(handler);
        MessageSender sender = new SimpleMessageSender(handler, service);
        MessageReceiver receiver = new SimpleMessageReceiver(handler, service, processor);

        handler.setProcessor(processor);
        handler.setService(service);
        handler.setSender(sender);
        handler.setReceiver(receiver);

        return handler;
    }

    public void bind(InetSocketAddress address) throws IOException, AuthenticationException {
        super.bind(ChannelConnectorProvider.openServerConnector(address));
    }

    public void cellBind(ChannelConnector connector) throws IOException, AuthenticationException {
        super.bind(connector);
        if (isRunning()) {
            ((CellManagerConnectorService) service).setCellAcceptableChannel(connector.getChannel());
            return;
        } else if (preparedConnectors == null) {
            preparedConnectors = Collections.synchronizedList(new ArrayList<>());
        }

        preparedConnectors.add(connector);
    }

    @Override
    protected void setUpRunning() {
        super.setUpRunning();
        if (preparedConnectors == null) {
            return;
        }

        preparedConnectors.forEach(connector -> ((CellManagerConnectorService) service).setCellAcceptableChannel(
                connector.getChannel()));
        preparedConnectors.clear();
    }

    public void newCell(ChannelConnector connector) {
        cells.put(rectangles.get(cells.size()), new CellInfo(connector, null));
    }

    public InetSocketAddress getAddrCell(double x, double y) {
        return getCellInfo(x, y).address;
    }

    public void setAddrCell(ChannelConnector channel, InetSocketAddress address) {
        cells.values()
                .stream()
                .filter(c -> c.channelConnector.equals(channel))
                .findFirst()
                .ifPresent(c -> c.address = address);
    }

    private CellInfo getCellInfo(double x, double y) {
        return rectangles.stream().filter(r -> r.contains(x, y)).findFirst().map(r -> cells.get(r)).get();
    }

    @AllArgsConstructor
    private static class CellInfo {

        public ChannelConnector channelConnector;
        public InetSocketAddress address;
    }
}
