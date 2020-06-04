package ru.mirea.dikanev.nikita.common.server.handler;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.function.Predicate;

import ru.mirea.dikanev.nikita.common.Client;
import ru.mirea.dikanev.nikita.common.server.entity.Message;
import ru.mirea.dikanev.nikita.common.server.exception.AuthenticationException;
import ru.mirea.dikanev.nikita.common.server.processor.MessageProcessor;
import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;

public interface MessageHandler extends Runnable {

    void bind(ChannelConnector connector) throws IOException, AuthenticationException;

    void setUp(MessageProcessor processor);

    void sendMessage(Message message);

    void sendMessage(Message message, Predicate<SelectionKey> predicate);

    void sendMessage(SelectableChannel channel, Message msg);

    void sendMessage(SelectableChannel channel, Message msg, Predicate<SelectionKey> predicate);

    Selector selector();

    void closeConnection(ChannelConnector connector);

    void reconnect(ChannelConnector connector);

    boolean contains(ChannelConnector connector);

    ChannelConnector getRootConnector();
}
