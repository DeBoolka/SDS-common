package ru.mirea.dikanev.nikita.common.server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.mirea.dikanev.nikita.common.entity.Message;

public class MessageSender {

    private MessageServer server;

    private ExecutorService pendingMessage;

    public MessageSender(MessageServer server, int sendersCount) {
        this.server = server;
        this.pendingMessage = Executors.newFixedThreadPool(sendersCount);
    }

    public void send(Message message) {
        pendingMessage.submit(() -> server.getMessageHandlers().forEach(handler -> handler.sendMessage(message)));
    }
}
