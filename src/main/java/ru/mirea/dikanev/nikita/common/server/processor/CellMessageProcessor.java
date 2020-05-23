package ru.mirea.dikanev.nikita.common.server.processor;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.extern.log4j.Log4j2;
import ru.mirea.dikanev.nikita.common.server.CellServer;
import ru.mirea.dikanev.nikita.common.server.entity.Message;
import ru.mirea.dikanev.nikita.common.server.entity.client.Client;
import ru.mirea.dikanev.nikita.common.server.exception.AuthenticationException;
import ru.mirea.dikanev.nikita.common.server.handler.CellHandler;
import ru.mirea.dikanev.nikita.common.server.handler.MessageHandler;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.LoginCodec;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.MessageCodec;
import ru.mirea.dikanev.nikita.common.server.protocol.pack.LoginPackage;
import ru.mirea.dikanev.nikita.common.server.protocol.pack.MessagePackage;
import ru.mirea.dikanev.nikita.common.server.service.ClientService;
import ru.mirea.dikanev.nikita.common.server.service.SimpleClientService;

@Log4j2
public class CellMessageProcessor implements MessageProcessor, Codes {

    private CellServer server;
    private ClientService clientService;

    private ExecutorService messageTasks;

    private MessageCodec messageCodec = new MessageCodec();
    private LoginCodec loginCodec = new LoginCodec();

    public CellMessageProcessor(CellServer server, int nThreads) {
        this.server = server;
        this.messageTasks = Executors.newFixedThreadPool(nThreads);
        this.clientService = new SimpleClientService();
    }

    @Override
    public void process(MessageHandler handler, Message message) {
        messageTasks.submit(() -> {
            int actionCode = message.getData().getInt();
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
            default:
                return;
        }
    }

    private void resize(Message message) {
        //TODO: make
    }

    private void login(CellHandler handler, Message message) {
        LoginPackage loginPack = loginCodec.decode(message.getData());
        String login = new String(loginPack.login);
        String password = new String(loginPack.password);

        Message newMessage;
        try {
            Client client = clientService.login(message.getFrom(), login, password);
            message.getFrom().setClient(client);
            newMessage = new Message(MessageCodec.newMessagePack("Login successful"));
        } catch (AuthenticationException e) {
            newMessage = new Message(MessageCodec.newMessagePack("Login failed"));
        }

        handler.sendMessage(message.getFrom().getChannel(), newMessage);
    }

    private void communication(CellHandler handler, Message message) {
        if (!clientService.isAuth(message.getFrom().getClient().get())) {
            handler.sendMessage(message.getFrom().getChannel(),
                    new Message(MessageCodec.newMessagePack("Permission denied")));
            return;
        }

        MessagePackage messagePackage = messageCodec.decode(message.getData());

        if (messagePackage.space == MessagePackage.WORLD) {
            server.getMessageHandlers().forEach(h -> h.sendMessage(message));
            return;
        } else if (messagePackage.space == MessagePackage.SECTOR_SPACE || messagePackage.receiverId == 0) {
            handler.sendMessage(message);
            return;
        }
        Optional<Client> receiverClient = clientService.getClient(messagePackage.receiverId);
        receiverClient.ifPresent(client -> handler.sendMessage(client.getChannel().getChannel(), message));
    }

    private void ping(Message message) {
        //TODO: make
    }

    @Override
    public void clear(MessageHandler handler) {

    }
}
