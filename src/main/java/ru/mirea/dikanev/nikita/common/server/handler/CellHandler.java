package ru.mirea.dikanev.nikita.common.server.handler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.log4j.Log4j2;
import ru.mirea.dikanev.nikita.common.math.Rectangle;
import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;
import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnectorProvider;
import ru.mirea.dikanev.nikita.common.server.exception.AuthenticationException;
import ru.mirea.dikanev.nikita.common.server.processor.CellMessageProcessor;
import ru.mirea.dikanev.nikita.common.server.receiver.MessageReceiver;
import ru.mirea.dikanev.nikita.common.server.receiver.SimpleMessageReceiver;
import ru.mirea.dikanev.nikita.common.server.sender.MessageSender;
import ru.mirea.dikanev.nikita.common.server.sender.SimpleMessageSender;
import ru.mirea.dikanev.nikita.common.server.service.SimpleClientService;
import ru.mirea.dikanev.nikita.common.server.service.connector.ConnectorService;
import ru.mirea.dikanev.nikita.common.server.service.connector.SimpleConnectorService;

/**
 * This is Cell.
 */
@Log4j2
public class CellHandler extends SimpleMessageHandler {

    private Rectangle rectangle = new Rectangle(0, 0, 0, 0);
    private Map<ChannelConnector, InetSocketAddress> sectors = new ConcurrentHashMap<>();

    private CellHandler(){
    }

    public static CellHandler create(CellMessageProcessor processor) throws IOException {
        return buildHandler(processor);
    }

    private static CellHandler buildHandler(CellMessageProcessor processor) {
        CellHandler handler = new CellHandler();
        ConnectorService service = new SimpleConnectorService(handler);
        MessageSender sender = new SimpleMessageSender(handler, service);
        MessageReceiver receiver = new SimpleMessageReceiver(handler, service, processor);

        handler.setProcessor(processor);
        handler.setService(service);
        handler.setSender(sender);
        handler.setReceiver(receiver);

        return handler;
    }

    @Override
    public void closeConnection(ChannelConnector connector) {
        sectors.remove(connector);
        super.closeConnection(connector);
    }

    public void setSector(ChannelConnector connector, InetSocketAddress address) {
        sectors.put(connector, address);
    }

    public void bindServer(InetSocketAddress address) throws IOException, AuthenticationException {
        super.bind(ChannelConnectorProvider.openServerConnector(address));
    }

    public void bindClient(InetSocketAddress address) throws IOException, AuthenticationException {
        super.bind(ChannelConnectorProvider.openClientConnector(address));
    }

    public ChannelConnector getSector(double x, double y) {
        //get accepting sockets and return any address
        return ((CellMessageProcessor) processor).getClientService()
                .getClients()
                .values()
                .stream()
                .filter(si -> !SimpleClientService.ROOT_USER_ID.equals(si.getClient().getId()) &&
                        si.getClient().getChannel().isAccepting())
                .findAny()
                .map(si -> si.getClient().getChannel())
                .orElse(null);
    }

    public InetSocketAddress getAddrSector(double x, double y) {
        return sectors.values().stream().findAny().orElse(null);
    }

    public void setRectangle(Rectangle rectangle) {
        log.info("Set rectangle: {}", rectangle);
        this.rectangle = rectangle;
    }

    public Set<ChannelConnector> getSectors() {
        return sectors.keySet();
    }

    public Rectangle getRectangle() {
        return rectangle;
    }
}
