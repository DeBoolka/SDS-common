package ru.mirea.dikanev.nikita.common.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.log4j.Log4j2;
import ru.mirea.dikanev.nikita.common.entity.ChangeOpsRequest;
import ru.mirea.dikanev.nikita.common.entity.Message;
import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;

@Log4j2
public class MessageHandler implements Runnable {

    private MessageSender sender;

    private Selector selector;

    private final Map<SelectableChannel, List<Message>> sendingMessages;
    private final List<ChangeOpsRequest> changeRequests;

    private ByteBuffer readBuffer = ByteBuffer.allocate(8192);

    public MessageHandler() {
        sendingMessages = new ConcurrentHashMap<>();
        changeRequests = new ArrayList<>();
    }

    public void sendMessage(Message message) {
        if (message == null) {
            throw new NullPointerException("Sent message should be non-null");
        }

        selector.keys().forEach(key -> {
            if (key.interestOps() == SelectionKey.OP_ACCEPT) {
                return;
            }

            List<Message> pendingMessages = sendingMessages.get(key.channel());
            if (pendingMessages == null) {
                pendingMessages = Collections.synchronizedList(new ArrayList<>());
                if (sendingMessages.putIfAbsent(key.channel(), pendingMessages) != null) {
                    pendingMessages = sendingMessages.get(key.channel());
                }
            }

            pendingMessages.add(message);
            changeRequests.add(new ChangeOpsRequest(key.channel(),
                    ChangeOpsRequest.CHANGE_OPS,
                    ChangeOpsRequest.OP_READ_WRITE));
            selector.wakeup();
        });
    }

    public void bind(ChannelConnector connector) throws IOException {
        log.info("New client");

        connector.bind(selector, this);
        SelectableChannel channel = connector.getChannel();
        channel.configureBlocking(false);

        SelectionKey key = channel.register(selector, connector.op());
        key.attach(connector);
        selector.wakeup();
    }

    public void setSender(MessageSender sender) {
        this.sender = sender;
    }

    @Override
    public void run() {
        try (Selector selector = SelectorProvider.provider().openSelector()) {
            this.selector = selector;

            while (true) {
                changeOps();
                selector.select();
                handle();

                if (Thread.interrupted()) {
                    selector.keys().forEach(this::closeConnection);
                    changeRequests.clear();
                    sendingMessages.clear();
                    break;
                }
            }

        } catch (Exception e) {
            log.error("Message handler error: ", e);
        }

        log.info("Message handler has stopped");
    }

    private void changeOps() {
        if (changeRequests.isEmpty()) {
            return;
        }

        synchronized (changeRequests) {
            if (changeRequests.isEmpty()) {
                return;
            }

            changeRequests.forEach(change -> {
                if (change.type == ChangeOpsRequest.CHANGE_OPS) {
                    SelectionKey key = change.channel.keyFor(selector);
                    key.interestOps(change.ops);
                }
            });

            changeRequests.clear();
        }
    }

    private void handle() throws IOException {
        for (SelectionKey key : selector.selectedKeys()) {
            ChannelConnector connector = (ChannelConnector) key.attachment();

            if (!key.isValid()) {
                closeConnection(key, connector.getChannel());
            } else if (key.isAcceptable()) {
                log.info("[Accept]");
                connector.onAccept(selector, this);
            } else if (key.isConnectable()) {
                log.info("[Connect]");
                connector.onConnect(selector, this);
                key.interestOps(SelectionKey.OP_READ);
            } else if (key.isReadable()) {
                log.info("[Read]");
                readMessage(key, connector);
            } else if (key.isWritable()) {
                log.info("[Write]");
                writeMessage(key, connector);
            }
        }

        selector.selectedKeys().clear();
    }

    private void readMessage(SelectionKey key, ChannelConnector connector) throws IOException {
        readBuffer.clear();

        int numRead;
        try {
            numRead = connector.onRead(selector, this, readBuffer);
        } catch (IOException e) {
            log.error("Failed to read from the channel: ", e);
            closeConnection(key, connector.getChannel());
            return;
        }

        if (numRead == -1) {
            log.info("Client is disconnected");
            closeConnection(key, connector.getChannel());
            return;
        }

        byte[] messageCopy = new byte[numRead];
        System.arraycopy(readBuffer.array(), 0, messageCopy, 0, numRead);
        Message message = new Message(this, connector, Message.WORLD, messageCopy);

        System.out.println("Receive: " + new String(messageCopy));
        sender.send(message);
    }

    private void writeMessage(SelectionKey key, ChannelConnector connector) throws IOException {
        List<Message> messages = sendingMessages.get(connector.getChannel());

        while (!messages.isEmpty()) {
            Message message = messages.get(0);
            ByteBuffer writeBuffer = ByteBuffer.wrap(message.getMessage());
            int numWrite = connector.onWrite(selector, this, writeBuffer);
            if (writeBuffer.remaining() > 0) {
                break;
            }

            messages.remove(0);
        }

        if (messages.isEmpty()) {
            key.interestOps(SelectionKey.OP_READ);
        }
    }

    private void closeConnection(SelectionKey key) {
        closeConnection(key, key.channel());
    }

    private void closeConnection(SelectableChannel channel) {
        SelectionKey key = channel.keyFor(selector);
        closeConnection(key, channel);
    }

    private void closeConnection(SelectionKey key, SelectableChannel channel) {
        try {
            key.cancel();
            channel.close();
        } catch (IOException e) {
            log.error("Failed to close the channel: ", e);
        }
    }
}
