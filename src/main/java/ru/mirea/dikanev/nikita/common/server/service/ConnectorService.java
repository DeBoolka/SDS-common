package ru.mirea.dikanev.nikita.common.server.service;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.Set;

import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;

public interface ConnectorService {

    void closeConnection(SelectionKey key);

    void closeConnection(SelectableChannel channel);

    void closeConnection(SelectionKey key, SelectableChannel channel);

    void bind(ChannelConnector connector) throws IOException;

    void accept(SelectionKey key, ChannelConnector connector) throws IOException;

    void connect(SelectionKey key, ChannelConnector connector) throws IOException;

    int changeOps(Set<SelectionKey> keys);

    void changeOps(SelectableChannel channel, int op);

    void clear();
}
