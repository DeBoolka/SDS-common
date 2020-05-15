package ru.mirea.dikanev.nikita.common.server;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import lombok.extern.log4j.Log4j2;
import ru.mirea.dikanev.nikita.common.entity.Message;
import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;
import ru.mirea.dikanev.nikita.common.server.handler.MessageHandler;
import ru.mirea.dikanev.nikita.common.server.processor.MessageProcessor;

@Log4j2
public class MessageServer {

    private List<MessageHandler> handlers;
    private MessageProcessor sender;

    private ExecutorService handlersExecutor;

    public MessageServer(int countSenders, MessageHandler... handlers) {
        sender = new MessageProcessor(this, countSenders);

        this.handlers = Arrays.asList(handlers);
        this.handlers.forEach(handler -> {
            handler.setProcessor(sender);
        });
    }

    public MessageServer bind(ChannelConnector connector) throws IOException {
        if (handlersExecutor == null) {
            throw new IllegalStateException("Server isn't running");
        }

        MessageHandler handler = balanceHandlers(connector);
        handler.bind(connector);

        return this;
    }

    public void start() {
        log.info("Server is starting...\nNumber of handlers: {}", handlers.size());
        handlersExecutor = Executors.newFixedThreadPool(handlers.size());
        handlers.forEach(handler -> handlersExecutor.submit(handler));

        log.info("Server has been started");
    }

    public void stop() throws InterruptedException {
        log.info("Server is stopping...");

        log.info("Handlers are shutdowning");
        handlersExecutor.shutdownNow();
        handlersExecutor.awaitTermination(5, TimeUnit.SECONDS);

        log.info("Server has been stopped");
    }

    public void send(Message message) {
        sender.send(message);
    }

    private MessageHandler balanceHandlers(ChannelConnector connector) {
        //TODO: make later
        return handlers.get((int) (System.currentTimeMillis() % handlers.size()));
    }

    public List<MessageHandler> getMessageHandlers() {
        return handlers;
    }
}
