package ru.mirea.dikanev.nikita.common.server.sender;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ru.mirea.dikanev.nikita.common.entity.ChangeOpsRequest;
import ru.mirea.dikanev.nikita.common.entity.Message;
import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;
import ru.mirea.dikanev.nikita.common.server.handler.MessageHandler;
import ru.mirea.dikanev.nikita.common.server.service.ConnectorService;

public class SimpleMessageSender implements MessageSender {

    private MessageHandler handler;
    private ConnectorService service;

    private final Map<SelectableChannel, List<Message>> sendingMessages;

    public SimpleMessageSender(MessageHandler handler, ConnectorService service) {
        this.handler = handler;
        this.service = service;
        this.sendingMessages = new ConcurrentHashMap<>();
    }

    @Override
    public void writeToChannel(SelectionKey key, ChannelConnector connector) throws IOException {
        List<Message> messages = sendingMessages.get(connector.getChannel());

        while (!messages.isEmpty()) {
            Message message = messages.get(0);
            ByteBuffer writeBuffer = ByteBuffer.wrap(message.getMessage());
            int numBytesWritten = connector.onWrite(handler.selector(), handler, writeBuffer);
            if (numBytesWritten == -1) {
                //The channel is not yet ready for writing
                return;
            } else if (writeBuffer.remaining() > 0) {
                break;
            }

            messages.remove(0);
        }

        if (messages.isEmpty()) {
            key.interestOps(SelectionKey.OP_READ);
        }
    }

    @Override
    public void send(Message message) {
        if (message == null) {
            throw new NullPointerException("Sent message should be non-null");
        }

        handler.selector().keys().forEach(key -> {
            if (((ChannelConnector) key.attachment()).isUnnecessaryMessage(key, message)) {
                return;
            }

            send(key.channel(), message);
            service.changeOps(key.channel(), ChangeOpsRequest.OP_READ_WRITE);
        });
    }

    @Override
    public void send(SelectableChannel channel, Message message) {
        List<Message> pendingMessages = sendingMessages.get(channel);
        if (pendingMessages == null) {
            pendingMessages = Collections.synchronizedList(new ArrayList<>());
            if (sendingMessages.putIfAbsent(channel, pendingMessages) != null) {
                pendingMessages = sendingMessages.get(channel);
            }
        }

        pendingMessages.add(message);
    }

    @Override
    public void clear() {
        sendingMessages.clear();
    }
}
