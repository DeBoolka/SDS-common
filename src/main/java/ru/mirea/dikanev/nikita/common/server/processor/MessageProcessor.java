package ru.mirea.dikanev.nikita.common.server.processor;

import ru.mirea.dikanev.nikita.common.server.entity.Message;
import ru.mirea.dikanev.nikita.common.server.handler.MessageHandler;

public interface MessageProcessor {

    void process(MessageHandler handler, Message message);

    void clear(MessageHandler handler);
}
