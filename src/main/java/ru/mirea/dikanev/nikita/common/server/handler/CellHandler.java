package ru.mirea.dikanev.nikita.common.server.handler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import lombok.Data;
import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;
import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnectorProvider;
import ru.mirea.dikanev.nikita.common.server.exception.AuthenticationException;
import ru.mirea.dikanev.nikita.common.server.processor.CellMessageProcessor;
import ru.mirea.dikanev.nikita.common.server.processor.MessageProcessor;
import ru.mirea.dikanev.nikita.common.server.receiver.MessageReceiver;
import ru.mirea.dikanev.nikita.common.server.receiver.SimpleMessageReceiver;
import ru.mirea.dikanev.nikita.common.server.sender.MessageSender;
import ru.mirea.dikanev.nikita.common.server.sender.SimpleMessageSender;
import ru.mirea.dikanev.nikita.common.server.service.ClientService;
import ru.mirea.dikanev.nikita.common.server.service.ConnectorService;
import ru.mirea.dikanev.nikita.common.server.service.SimpleClientService;
import ru.mirea.dikanev.nikita.common.server.service.SimpleConnectorService;

/**
 * This is Cell.
 */
public class CellHandler extends SimpleMessageHandler {

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
}
