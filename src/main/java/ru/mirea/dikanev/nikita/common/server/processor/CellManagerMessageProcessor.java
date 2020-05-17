package ru.mirea.dikanev.nikita.common.server.processor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.mirea.dikanev.nikita.common.server.CellManagerServer;
import ru.mirea.dikanev.nikita.common.server.entity.Message;
import ru.mirea.dikanev.nikita.common.server.handler.MessageHandler;

public class CellManagerMessageProcessor implements MessageProcessor {

    private CellManagerServer server;

    private ExecutorService messageTasks;

    public CellManagerMessageProcessor(CellManagerServer server, int nThreads) {
        this.server = server;
        this.messageTasks = Executors.newFixedThreadPool(nThreads);
    }

    @Override
    public void process(Message message) {
        messageTasks.submit(() -> server.getMessageHandlers().forEach(handler -> handler.sendMessage(message)));
    }

    @Override
    public void clear(MessageHandler handler) {
        messageTasks.shutdown();
    }
}
