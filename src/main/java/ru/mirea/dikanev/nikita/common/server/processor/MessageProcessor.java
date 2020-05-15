package ru.mirea.dikanev.nikita.common.server.processor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.mirea.dikanev.nikita.common.entity.Message;
import ru.mirea.dikanev.nikita.common.server.MessageServer;

public class MessageProcessor {

    private MessageServer server;

    private ExecutorService pendingMessage;

    public MessageProcessor(MessageServer server, int sendersCount) {
        this.server = server;
        this.pendingMessage = Executors.newFixedThreadPool(sendersCount);
    }

    public void send(Message message) {
        pendingMessage.submit(() -> server.getMessageHandlers().forEach(handler -> handler.sendMessage(message)));
    }
}
