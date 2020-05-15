package ru.mirea.dikanev.nikita.common.server.receiver;

import java.nio.channels.SelectionKey;

import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;

public interface MessageReceiver {

    void receive(SelectionKey key, ChannelConnector connector);

    void clear();

}
