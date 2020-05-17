package ru.mirea.dikanev.nikita.common.server;

import java.io.IOException;
import java.util.List;

import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;
import ru.mirea.dikanev.nikita.common.server.entity.Message;
import ru.mirea.dikanev.nikita.common.server.handler.MessageHandler;

public interface MessageServer {

    void start();

    void stop() throws InterruptedException;

    MessageHandler bind(ChannelConnector connector) throws IOException;

    void send(Message message);

    List<MessageHandler> getMessageHandlers();
}
