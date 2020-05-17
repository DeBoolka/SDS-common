package ru.mirea.dikanev.nikita.common.server.receiver;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

import lombok.extern.log4j.Log4j2;
import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;
import ru.mirea.dikanev.nikita.common.server.entity.Message;
import ru.mirea.dikanev.nikita.common.server.handler.MessageHandler;
import ru.mirea.dikanev.nikita.common.server.processor.MessageProcessor;
import ru.mirea.dikanev.nikita.common.server.service.ConnectorService;

@Log4j2
public class SimpleMessageReceiver implements MessageReceiver {

    private static final int BUFFER_SIZE = 8192;

    private MessageHandler handler;
    private ConnectorService service;
    private MessageProcessor processor;

//    private Map<SelectableChannel, ByteBuilder> incompleteMessages;
    private ByteBuffer readBuffer = ByteBuffer.allocate(BUFFER_SIZE);

    public SimpleMessageReceiver(MessageHandler handler, ConnectorService service, MessageProcessor processor) {
        this.handler = handler;
        this.service = service;
        this.processor = processor;
    }

    @Override
    public void receive(SelectionKey key, ChannelConnector connector) {
        readBuffer.clear();

        int numRead;
        try {
            numRead = connector.onRead(key.selector(), handler, readBuffer);
        } catch (IOException e) {
            log.error("Failed to read from the channel: ", e);
            service.closeConnection(key, connector.getChannel());
            return;
        }

        if (numRead == -1) {
            log.info("Client is disconnected");
            service.closeConnection(key, connector.getChannel());
            return;
        } else if (numRead == -2) {
            //The channel is not yet ready for reading
            return;
        }

        byte[] messageCopy = new byte[numRead];
        System.arraycopy(readBuffer.array(), 0, messageCopy, 0, numRead);
        Message message = new Message(connector, messageCopy);

        System.out.println("[Read new package]");
        processor.process(handler, message);
    }

    @Override
    public void clear() { }
}
