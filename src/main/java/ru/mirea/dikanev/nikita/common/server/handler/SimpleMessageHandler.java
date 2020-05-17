package ru.mirea.dikanev.nikita.common.server.handler;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;
import ru.mirea.dikanev.nikita.common.server.entity.Message;
import ru.mirea.dikanev.nikita.common.server.exception.HandlerInternalException;
import ru.mirea.dikanev.nikita.common.server.processor.MessageProcessor;
import ru.mirea.dikanev.nikita.common.server.receiver.MessageReceiver;
import ru.mirea.dikanev.nikita.common.server.receiver.SimpleMessageReceiver;
import ru.mirea.dikanev.nikita.common.server.sender.MessageSender;
import ru.mirea.dikanev.nikita.common.server.sender.SimpleMessageSender;
import ru.mirea.dikanev.nikita.common.server.service.ConnectorService;
import ru.mirea.dikanev.nikita.common.server.service.SimpleConnectorService;

@Log4j2
@Data
public class SimpleMessageHandler implements MessageHandler {

    protected MessageSender sender;
    protected MessageReceiver receiver;
    protected ConnectorService service;
    protected MessageProcessor processor;

    protected Selector selector;

    private volatile boolean isRunning = false;

    private List<ChannelConnector> preparedConnectorsForBinding = null;

    @Override
    public void setUp(MessageProcessor processor) {
        service = new SimpleConnectorService(this);
        sender = new SimpleMessageSender(this, service);
        receiver = new SimpleMessageReceiver(this, service, processor);
        this.processor = processor;
    }

    @Override
    public void sendMessage(Message message) {
        sender.send(message);
    }

    @Override
    public void sendMessage(SelectableChannel channel, Message message) {
        sender.send(channel, message);
    }

    @Override
    public Selector selector() {
        return selector;
    }

    @Override
    public void bind(ChannelConnector connector) throws IOException {
        if (isRunning()) {
            service.bind(connector);
            return;
        } else if (preparedConnectorsForBinding == null) {
            preparedConnectorsForBinding = new ArrayList<>();
        }

        preparedConnectorsForBinding.add(connector);
    }

    @Override
    public void run() {
        try (Selector selector = SelectorProvider.provider().openSelector()) {
            this.selector = selector;
            isRunning = true;
            setUpRunning();

            while (true) {
                service.changeOps(selector.keys());
                selector.select();
                handle();

                if (Thread.interrupted()) {
                    selector.keys().forEach(service::closeConnection);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Message handler error: ", e);
        } finally {
            isRunning = false;
            receiver.clear();
            processor.clear(this);
            sender.clear();
            service.clear();
        }

        log.info("Message handler has stopped");
    }

    /**
     * This method is required descendants to adjust the space before running.
     */
    protected void setUpRunning() {
        if (preparedConnectorsForBinding == null) {
            return;
        }

        preparedConnectorsForBinding.forEach(connector -> {
            try {
                bind(connector);
            } catch (IOException e) {
                throw new HandlerInternalException("Handler setup failed", e);
            }
        });
        preparedConnectorsForBinding.clear();
    }

    private void handle() throws IOException {
        for (SelectionKey key : selector.selectedKeys()) {
            ChannelConnector connector = (ChannelConnector) key.attachment();

            if (!key.isValid()) {
                service.closeConnection(key, connector.getChannel());
            } else if (key.isAcceptable()) {
                log.info("[Accept]");
                service.accept(key, connector);;
            } else if (key.isConnectable()) {
                log.info("[Connect]");
                service.connect(key, connector);
            } else if (key.isReadable()) {
                log.info("[Read]");
                receiver.receive(key, connector);
            } else if (key.isWritable()) {
                log.info("[Write]");
                sender.writeToChannel(key, connector);
            }
        }

        selector.selectedKeys().clear();
    }

}
