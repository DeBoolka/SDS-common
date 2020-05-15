package ru.mirea.dikanev.nikita.common.server.processor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.mirea.dikanev.nikita.common.entity.Message;
import ru.mirea.dikanev.nikita.common.server.MessageServer;
import ru.mirea.dikanev.nikita.common.server.handler.MessageHandler;

public class SimpleMessageProcessor implements MessageProcessor {

    private MessageServer server;

    private ExecutorService pendingMessage;

    public SimpleMessageProcessor(MessageServer server, int processCount) {
        this.server = server;
        this.pendingMessage = Executors.newFixedThreadPool(processCount);
    }

    @Override
    public void process(Message message) {
        pendingMessage.submit(() -> server.getMessageHandlers().forEach(handler -> handler.sendMessage(message)));
    }

    @Override
    public void clear(MessageHandler handler) { }

}
