package ru.mirea.dikanev.nikita.common.server.handler;

import java.io.IOException;

import ru.mirea.dikanev.nikita.common.entity.Message;
import ru.mirea.dikanev.nikita.common.server.MessageSender;
import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;

public interface MessageHandler extends Runnable {

    void sendMessage(Message message);

    void bind(ChannelConnector connector) throws IOException;

    void setSender(MessageSender sender);
}
