package ru.mirea.dikanev.nikita.common.server.service;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import lombok.extern.log4j.Log4j2;
import ru.mirea.dikanev.nikita.common.server.entity.ChangeOpsRequest;
import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;
import ru.mirea.dikanev.nikita.common.server.handler.MessageHandler;

@Log4j2
public class SimpleConnectorService implements ConnectorService {

    private MessageHandler handler;

    private final List<ChangeOpsRequest> changeRequests;

    public SimpleConnectorService(MessageHandler handler) {
        this.handler = handler;
        changeRequests = Collections.synchronizedList(new ArrayList<>());
    }

    @Override
    public void closeConnection(SelectionKey key) {
        closeConnection(key, key.channel());
    }

    @Override
    public void closeConnection(SelectableChannel channel) {
        SelectionKey key = channel.keyFor(handler.selector());
        closeConnection(key, channel);
    }

    @Override
    public void closeConnection(SelectionKey key, SelectableChannel channel) {
        try {
            key.cancel();
            channel.close();
        } catch (IOException e) {
            log.error("Failed to close the channel: ", e);
        }
    }

    @Override
    public void bind(ChannelConnector connector) throws IOException {
        log.info("New client");
        Selector selector = handler.selector();

        connector.bind(selector, handler);
        SelectableChannel channel = connector.getChannel();
        channel.configureBlocking(false);

        SelectionKey key = channel.register(selector, connector.op());
        key.attach(connector);
        selector.wakeup();
    }

    @Override
    public void accept(SelectionKey key, ChannelConnector connector) throws IOException {
        connector.onAccept(handler.selector(), handler);
    }

    @Override
    public void connect(SelectionKey key, ChannelConnector connector) throws IOException {
        connector.onConnect(handler.selector(), handler);
        key.interestOps(SelectionKey.OP_READ);
    }

    @Override
    public int changeOps(Set<SelectionKey> keys) {
        if (changeRequests.isEmpty()) {
            return 0;
        }

        synchronized (changeRequests) {
            if (changeRequests.isEmpty()) {
                return 0;
            }

            changeRequests.forEach(change -> {
                if (change.type == ChangeOpsRequest.CHANGE_OPS) {
                    SelectionKey key = change.channel.keyFor(handler.selector());
                    key.interestOps(change.ops);
                }
            });

            int changedCount = changeRequests.size();
            changeRequests.clear();
            return changedCount;
        }
    }

    @Override
    public void changeOps(SelectableChannel channel, int op) {
        changeRequests.add(new ChangeOpsRequest(channel, ChangeOpsRequest.CHANGE_OPS, ChangeOpsRequest.OP_READ_WRITE));
        handler.selector().wakeup();
    }

    @Override
    public void clear() {
        changeRequests.clear();
    }
}
