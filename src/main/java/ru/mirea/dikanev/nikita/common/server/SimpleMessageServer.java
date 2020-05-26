package ru.mirea.dikanev.nikita.common.server;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import lombok.extern.log4j.Log4j2;
import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;
import ru.mirea.dikanev.nikita.common.server.entity.Message;
import ru.mirea.dikanev.nikita.common.server.exception.AuthenticationException;
import ru.mirea.dikanev.nikita.common.server.handler.MessageHandler;
import ru.mirea.dikanev.nikita.common.server.processor.MessageProcessor;
import ru.mirea.dikanev.nikita.common.server.processor.SimpleMessageProcessor;

@Log4j2
public class SimpleMessageServer implements MessageServer {

    private List<MessageHandler> handlers;
    private MessageProcessor processor;

    private ExecutorService handlersExecutor;

    public SimpleMessageServer(int countSenders, MessageHandler... handlers) {
        processor = new SimpleMessageProcessor(this, countSenders);
        handlers(handlers);
    }

    public SimpleMessageServer() {
    }

    public SimpleMessageServer handlers(MessageHandler... handlers) {
        this.handlers = Arrays.asList(handlers);
        this.handlers.forEach(h -> h.setUp(processor));
        return this;
    }

    public void processor(MessageProcessor processor) {
        this.processor = processor;
    }

    public MessageHandler bind(ChannelConnector connector) throws IOException {
        if (handlersExecutor == null) {
            throw new IllegalStateException("Server isn't running");
        }

        MessageHandler handler = balanceHandlers();
        try {
            handler.bind(connector);
        } catch (AuthenticationException e) {
            log.warn(e);
        }

        return handler;
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
        if (message.getFrom() == null) {
            handlers.get(0).sendMessage(message);
            return;
        }

        handlers.stream()
                .filter(h -> h.contains(message.getFrom()))
                .findFirst()
                .ifPresentOrElse(h -> h.sendMessage(message), () -> handlers.get(0).sendMessage(message));
    }

    protected MessageHandler balanceHandlers() {
        //TODO: make later
        return handlers.get((int) (System.currentTimeMillis() % handlers.size()));
    }

    public List<MessageHandler> getMessageHandlers() {
        return handlers;
    }
}
