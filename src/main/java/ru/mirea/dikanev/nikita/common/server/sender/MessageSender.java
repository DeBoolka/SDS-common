package ru.mirea.dikanev.nikita.common.server.sender;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

import ru.mirea.dikanev.nikita.common.entity.Message;
import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;

public interface MessageSender {

    void writeToChannel(SelectionKey key, ChannelConnector connector) throws IOException;

    void send(Message message);

    void send(SelectableChannel channel, Message message);

    void clear();
}
