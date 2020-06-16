package ru.mirea.dikanev.nikita.common.server;

import java.util.List;

import ru.mirea.dikanev.nikita.common.server.handler.SimpleMessageHandler;
import ru.mirea.dikanev.nikita.common.server.processor.TestMessageProcessor;
import ru.mirea.dikanev.nikita.common.server.receiver.TestReceiver;
import ru.mirea.dikanev.nikita.common.server.sender.TestSender;
import ru.mirea.dikanev.nikita.common.server.service.connector.SimpleConnectorService;

public class TestServer extends SimpleMessageServer {

    private TestServer() {
    }

    public static TestServer create(int nMessageProcessors) {
        TestServer server = new TestServer();

        SimpleMessageHandler handler = new SimpleMessageHandler();
        TestMessageProcessor processor = new TestMessageProcessor(nMessageProcessors);
        SimpleConnectorService connectorService = new SimpleConnectorService(handler);
        TestReceiver receiver = new TestReceiver(handler, connectorService, processor);
        TestSender sender = new TestSender(handler, connectorService);

        server.processor(processor);
        handler.setService(connectorService);
        handler.setReceiver(receiver);
        handler.setSender(sender);
        handler.setProcessor(processor);

        server.handlers = List.of(handler);

        return server;
    }

}
