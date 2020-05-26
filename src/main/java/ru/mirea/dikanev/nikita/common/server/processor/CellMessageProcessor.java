package ru.mirea.dikanev.nikita.common.server.processor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.extern.log4j.Log4j2;
import ru.mirea.dikanev.nikita.common.math.Point;
import ru.mirea.dikanev.nikita.common.server.CellServer;
import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;
import ru.mirea.dikanev.nikita.common.server.entity.Message;
import ru.mirea.dikanev.nikita.common.server.entity.client.Client;
import ru.mirea.dikanev.nikita.common.server.exception.AuthenticationException;
import ru.mirea.dikanev.nikita.common.server.handler.CellHandler;
import ru.mirea.dikanev.nikita.common.server.handler.MessageHandler;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.LoginCodec;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.MessageCodec;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.PositionCodec;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.ReconnectCodec;
import ru.mirea.dikanev.nikita.common.server.protocol.pack.LoginPackage;
import ru.mirea.dikanev.nikita.common.server.protocol.pack.MessagePackage;
import ru.mirea.dikanev.nikita.common.server.protocol.pack.PositionPackage;
import ru.mirea.dikanev.nikita.common.server.protocol.pack.ReconnectPackage;
import ru.mirea.dikanev.nikita.common.server.service.ClientService;
import ru.mirea.dikanev.nikita.common.server.service.SimpleClientService;

@Log4j2
public class CellMessageProcessor implements MessageProcessor, Codes {

    protected CellServer server;
    protected ClientService clientService;

    private ExecutorService messageTasks;

    protected ReconnectCodec reconnectCodec = new ReconnectCodec();
    protected PositionCodec positionCodec = new PositionCodec();
    protected MessageCodec messageCodec = new MessageCodec();
    protected LoginCodec loginCodec = new LoginCodec();

    public CellMessageProcessor(CellServer server, int nThreads) {
        this.server = server;
        this.messageTasks = Executors.newFixedThreadPool(nThreads);
        this.clientService = new SimpleClientService();
    }

    @Override
    public void process(MessageHandler handler, Message message) {
        messageTasks.submit(() -> {
            int actionCode = message.getAction();
            action((CellHandler) handler, actionCode, message);
        });
    }

    private void action(CellHandler handler, int actionCode, Message message) {
        switch (actionCode) {
            case LOGIN_ACTION:
                login(handler, message);
                return;
            case PING_ACTION:
                ping(message);
                return;
            case COMMUNICATION_ACTION:
                communication(handler, message);
                return;
            case RESIZE_ACTION:
                resize(message);
                return;
            case RECONNECT_ACTION:
                reconnect(handler, message);
                return;
            case POSITION_ACTION:
                position(handler, message);
                return;
            default:
                return;
        }
    }

    protected void resize(Message message) {
        //TODO: make
    }

    protected void login(CellHandler handler, Message message) {
        LoginPackage loginPack = loginCodec.decode(message.payload());
        String login = new String(loginPack.login);
        String password = new String(loginPack.password);

        Message newMessage;
        try {
            Client client = clientService.login(message.getFrom(), login, password);
            message.getFrom().setClient(client);
            newMessage = Message.send(message.getFrom(), "Login successful");
        } catch (AuthenticationException e) {
            newMessage = Message.send(message.getFrom(), "Login failed");
        }

        handler.sendMessage(message.getFrom().getChannel(), newMessage);
    }

    protected void communication(CellHandler handler, Message message) {
        MessagePackage messagePackage = messageCodec.decode(message.payload());

        if (messagePackage.space == MessagePackage.WORLD) {
            handler.sendMessage(message);
            return;
        }

        handler.sendMessage(message, key -> {
            //Send only to Sectors excluding other Cells
            //And too this is a piece of shit.
            //If you want to plunge into shit, look at the communication method in the SectorMessageProcessor
            return ((ChannelConnector) key.attachment()).getClient()
                    .map(value -> !SimpleClientService.ROOT_USER_ID.equals(value.getId()))
                    .orElse(true);
        });
    }

    protected void ping(Message message) {
        //TODO: make
    }

    private void reconnect(CellHandler handler, Message message) {
        ReconnectPackage reconnectPackage = reconnectCodec.decode(message.payload());
        ChannelConnector connector = message.getFrom();

        try {
            handler.reconnect(connector);
            connector.reconnect(new InetSocketAddress(new String(reconnectPackage.host), reconnectPackage.port));
            handler.bind(connector);
        } catch (IOException | AuthenticationException e) {
            log.error("Couldn't reconnect to new socket({}, {})",
                    new String(reconnectPackage.host),
                    reconnectPackage.port,
                    e);
        }
    }

    protected void position(CellHandler handler, Message message) {
        PositionPackage posPackage = positionCodec.decode(message.payload());
        ChannelConnector connector = message.getFrom();

        Client client = connector.getClient().orElse(null);
        if (client == null) {
            return;
        }

        clientService.setPosition(client.getId(), new Point(posPackage.x, posPackage.y));
        //TODO: if the user is outside the Cell reconnect to another cell
    }

    @Override
    public void clear(MessageHandler handler) {

    }
}
