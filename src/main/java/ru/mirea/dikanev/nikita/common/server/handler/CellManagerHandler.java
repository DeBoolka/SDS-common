package ru.mirea.dikanev.nikita.common.server.handler;

import java.io.IOException;
import java.net.SocketAddress;

import lombok.extern.log4j.Log4j2;
import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;
import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnectorProvider;
import ru.mirea.dikanev.nikita.common.server.processor.MessageProcessor;
import ru.mirea.dikanev.nikita.common.server.receiver.MessageReceiver;
import ru.mirea.dikanev.nikita.common.server.receiver.SimpleMessageReceiver;
import ru.mirea.dikanev.nikita.common.server.sender.MessageSender;
import ru.mirea.dikanev.nikita.common.server.sender.SimpleMessageSender;
import ru.mirea.dikanev.nikita.common.server.service.ConnectorService;
import ru.mirea.dikanev.nikita.common.server.service.SimpleConnectorService;

@Log4j2
public class CellManagerHandler extends SimpleMessageHandler {

    private CellManagerHandler(){
    }

    public static CellManagerHandler create(MessageProcessor processor, SocketAddress... addresses) throws IOException {
        CellManagerHandler handler = buildHandler(processor);
        for (SocketAddress address : addresses) {
            handler.bind(address);
        }

        return handler;
    }

    private static CellManagerHandler buildHandler(MessageProcessor processor) {
        CellManagerHandler handler = new CellManagerHandler();
        ConnectorService service = new SimpleConnectorService(handler);
        MessageSender sender = new SimpleMessageSender(handler, service);
        MessageReceiver receiver = new SimpleMessageReceiver(handler, service, processor);

        handler.setProcessor(processor);
        handler.setService(service);
        handler.setSender(sender);
        handler.setReceiver(receiver);

        return handler;
    }

    public void bind(SocketAddress address) throws IOException {
        super.bind(ChannelConnectorProvider.openServerConnector(address));
    }
}
