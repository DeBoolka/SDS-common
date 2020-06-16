package ru.mirea.dikanev.nikita.common.server.receiver;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;
import ru.mirea.dikanev.nikita.common.server.entity.Message;
import ru.mirea.dikanev.nikita.common.server.handler.MessageHandler;
import ru.mirea.dikanev.nikita.common.server.handler.SimpleMessageHandler;
import ru.mirea.dikanev.nikita.common.server.processor.MessageProcessor;
import ru.mirea.dikanev.nikita.common.server.service.connector.ConnectorService;

public class TestReceiver implements MessageReceiver {

    private static final int BUFFER_SIZE = 8192;

    private MessageHandler handler;
    private ConnectorService service;
    private MessageProcessor processor;

    //    private Map<SelectableChannel, ByteBuilder> incompleteMessages;
    private final ByteBuffer readBuffer = ByteBuffer.allocate(BUFFER_SIZE);

    public TestReceiver(SimpleMessageHandler handler, ConnectorService service, MessageProcessor processor) {
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
            service.closeConnection(key, connector);
            return;
        }

        if (numRead == -1) {
            service.closeConnection(key, connector);
            return;
        } else if (numRead == -2) {
            //The channel is not yet ready for reading
            return;
        }

        readBuffer.rewind();
        byte[] gottenData = readBuffer.array();
        while (readBuffer.position() < numRead) {
            int len = readBuffer.getInt();
            byte[] messageCopy = new byte[len];
            System.arraycopy(gottenData, readBuffer.position(), messageCopy, 0, len);
            Message message = new Message(connector, -1, ByteBuffer.allocate(Integer.BYTES * 3));
            readBuffer.position(len + readBuffer.position());

            processor.process(handler, message);
        }

    }

    @Override
    public void clear() { }
}
