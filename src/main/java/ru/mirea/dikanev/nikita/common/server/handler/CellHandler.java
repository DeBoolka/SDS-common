package ru.mirea.dikanev.nikita.common.server.handler;

import java.io.IOException;
import java.net.SocketAddress;

import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnectorProvider;
import ru.mirea.dikanev.nikita.common.server.entity.CellSize;
import ru.mirea.dikanev.nikita.common.server.processor.MessageProcessor;
import ru.mirea.dikanev.nikita.common.server.receiver.MessageReceiver;
import ru.mirea.dikanev.nikita.common.server.receiver.SimpleMessageReceiver;
import ru.mirea.dikanev.nikita.common.server.sender.MessageSender;
import ru.mirea.dikanev.nikita.common.server.sender.SimpleMessageSender;
import ru.mirea.dikanev.nikita.common.server.service.ConnectorService;
import ru.mirea.dikanev.nikita.common.server.service.SimpleConnectorService;

/**
 * This is Cell.
 */
public class CellHandler extends SimpleMessageHandler {

    private CellSize cellSize;

    private CellHandler(){
    }

    public static CellHandler create(MessageProcessor processor) throws IOException {
        return buildHandler(processor);
    }

    private static CellHandler buildHandler(MessageProcessor processor) {
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

    public void bindServer(SocketAddress address) throws IOException {
        super.bind(ChannelConnectorProvider.openServerConnector(address));
    }

    public void bindClient(SocketAddress address) throws IOException {
        super.bind(ChannelConnectorProvider.openClientConnector(address));
    }

}
