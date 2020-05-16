package ru.mirea.dikanev.nikita.common.server.handler;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.Selector;

import ru.mirea.dikanev.nikita.common.server.entity.Message;
import ru.mirea.dikanev.nikita.common.server.processor.MessageProcessor;
import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;

public interface MessageHandler extends Runnable {

    void bind(ChannelConnector connector) throws IOException;

    void setUp(MessageProcessor processor);

    void sendMessage(Message message);

    void sendMessage(SelectableChannel channel, Message msg);

    Selector selector();

}