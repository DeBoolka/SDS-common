package ru.mirea.dikanev.nikita.common.server.handler;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;
import ru.mirea.dikanev.nikita.common.server.entity.Message;
import ru.mirea.dikanev.nikita.common.server.exception.AuthenticationException;
import ru.mirea.dikanev.nikita.common.server.exception.HandlerInternalException;
import ru.mirea.dikanev.nikita.common.server.processor.MessageProcessor;
import ru.mirea.dikanev.nikita.common.server.receiver.MessageReceiver;
import ru.mirea.dikanev.nikita.common.server.receiver.SimpleMessageReceiver;
import ru.mirea.dikanev.nikita.common.server.sender.MessageSender;
import ru.mirea.dikanev.nikita.common.server.sender.SimpleMessageSender;
import ru.mirea.dikanev.nikita.common.server.service.connector.ConnectorService;
import ru.mirea.dikanev.nikita.common.server.service.connector.SimpleConnectorService;

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
    private List<CallBack> finishCallBacks = null;
    private ChannelConnector rootConnector;

    @Override
    public void setUp(MessageProcessor processor) {
        service = new SimpleConnectorService(this);
        sender = new SimpleMessageSender(this, service);
        receiver = new SimpleMessageReceiver(this, service, processor);
        this.processor = processor;
    }

    @Override
    public void sendMessage(Message message) {
        sendMessage(message, null);
    }

    @Override
    public void sendMessage(Message message, Predicate<SelectionKey> predicate) {
        sender.send(message, predicate);
    }

    @Override
    public void sendMessage(SelectableChannel channel, Message message) {
        sendMessage(channel, message, null);
    }

    @Override
    public void sendMessage(SelectableChannel channel, Message msg, Predicate<SelectionKey> predicate) {
        sender.send(channel, msg, predicate);
    }

    @Override
    public Selector selector() {
        return selector;
    }

    @Override
    public void closeConnection(ChannelConnector connector) {
        service.closeConnection(connector.getChannel().keyFor(selector), connector);
    }

    @Override
    public void reconnect(ChannelConnector connector) {
        service.closeConnection(connector.getChannel().keyFor(selector), connector.getChannel());
    }

    @Override
    public boolean contains(ChannelConnector connector) {
        return connector.getChannel().keyFor(selector) != null;
    }

    @Override
    public void bind(ChannelConnector connector) throws IOException, AuthenticationException {
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
            finishRun();

            while (true) {
                service.changeOps(selector.keys());
                selector.select();
                handle();

                if (Thread.interrupted()) {
                    selector.keys().forEach(key -> service.closeConnection(key, (ChannelConnector) key.attachment()));
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

    public void addFinishCallback(CallBack callBack) {
        if (finishCallBacks == null) {
            finishCallBacks = new ArrayList<>();
        }

        finishCallBacks.add(callBack);
    }

    private void finishRun() {
        if (finishCallBacks == null) {
            return;
        }

        finishCallBacks.forEach(callBack -> {
            try {
                callBack.callback(this);
            } catch (Exception e) {
                log.warn("Callback failed", e);
            }
        });
        finishCallBacks.clear();
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
            } catch (IOException | AuthenticationException e) {
                throw new HandlerInternalException("Handler setup failed", e);
            }
        });
        preparedConnectorsForBinding.clear();
    }

    private void handle() {
        for (SelectionKey key : selector.selectedKeys()) {
            try {
                ChannelConnector connector = (ChannelConnector) key.attachment();

                if (!key.isValid()) {
                    service.closeConnection(key, connector);
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
            } catch (Exception e) {
                log.error("Event error:", e);
            }
        }

        selector.selectedKeys().clear();
    }

    public interface CallBack {
        void callback(MessageHandler handler);
    }

}
