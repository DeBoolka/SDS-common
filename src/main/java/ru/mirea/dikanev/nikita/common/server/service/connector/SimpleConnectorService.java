package ru.mirea.dikanev.nikita.common.server.service.connector;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import lombok.extern.log4j.Log4j2;
import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;
import ru.mirea.dikanev.nikita.common.server.entity.ChangeOpsRequest;
import ru.mirea.dikanev.nikita.common.server.entity.client.Client;
import ru.mirea.dikanev.nikita.common.server.exception.AuthenticationException;
import ru.mirea.dikanev.nikita.common.server.handler.MessageHandler;
import ru.mirea.dikanev.nikita.common.server.service.client.ClientService;
import ru.mirea.dikanev.nikita.common.server.service.client.SimpleClientService;

@Log4j2
public class SimpleConnectorService implements ConnectorService {

    protected MessageHandler handler;
    protected ClientService clientService;

    protected final List<ChangeOpsRequest> changeRequests;

    public SimpleConnectorService(MessageHandler handler) {
        this(handler, new SimpleClientService());
    }

    public SimpleConnectorService(MessageHandler handler, ClientService clientService) {
        this.handler = handler;
        this.clientService = clientService;
        changeRequests = Collections.synchronizedList(new ArrayList<>());
    }

    @Override
    public void closeConnection(SelectionKey key, ChannelConnector connector) {
        connector.getClient().ifPresent(client -> clientService.exitClient(client));
        closeConnection(key, connector.getChannel());
    }

    @Override
    public void closeConnection(SelectionKey key, SelectableChannel channel) {
        try {
            key.cancel();
            channel.close();
        } catch (IOException e) {
            log.error("Failed to close the channel: ", e);
        }
    }

    @Override
    public void bind(ChannelConnector connector) throws IOException, AuthenticationException {
        log.info("New client");
        Optional<Client> client = connector.getClient();
        connector.setClient(clientService.login(connector, client.orElse(null)));

        Selector selector = handler.selector();

        connector.bind(selector, handler);
        SelectableChannel channel = connector.getChannel();
        channel.configureBlocking(false);

        SelectionKey key = channel.register(selector, connector.op());
        key.attach(connector);
        selector.wakeup();
    }

    @Override
    public void accept(SelectionKey key, ChannelConnector connector) throws IOException {
        ChannelConnector newConnector = null;
        try {
            newConnector = connector.onAccept(handler.selector(), handler);
            handler.bind(newConnector);
        } catch (AuthenticationException ignore) {
            newConnector.getChannel().close();
        }
    }

    @Override
    public void connect(SelectionKey key, ChannelConnector connector) throws IOException {
        connector.onConnect(handler.selector(), handler);
        key.interestOps(SelectionKey.OP_READ);
    }

    @Override
    public int changeOps(Set<SelectionKey> keys) {
        if (changeRequests.isEmpty()) {
            return 0;
        }

        synchronized (changeRequests) {
            if (changeRequests.isEmpty()) {
                return 0;
            }

            changeRequests.forEach(change -> {
                if (change.type == ChangeOpsRequest.CHANGE_OPS && change.channel.isOpen()) {
                    SelectionKey key = change.channel.keyFor(handler.selector());
                    key.interestOps(change.ops);
                }
            });

            int changedCount = changeRequests.size();
            changeRequests.clear();
            return changedCount;
        }
    }

    @Override
    public void changeOps(SelectableChannel channel, int op) {
        changeRequests.add(new ChangeOpsRequest(channel, ChangeOpsRequest.CHANGE_OPS, ChangeOpsRequest.OP_READ_WRITE));
        handler.selector().wakeup();
    }

    @Override
    public void clear() {
        changeRequests.clear();
    }

}
