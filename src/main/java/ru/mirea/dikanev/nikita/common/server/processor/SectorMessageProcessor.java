package ru.mirea.dikanev.nikita.common.server.processor;

import java.util.Optional;

import ru.mirea.dikanev.nikita.common.server.CellServer;
import ru.mirea.dikanev.nikita.common.server.entity.Message;
import ru.mirea.dikanev.nikita.common.server.entity.client.Client;
import ru.mirea.dikanev.nikita.common.server.handler.CellHandler;
import ru.mirea.dikanev.nikita.common.server.protocol.pack.MessagePackage;
import ru.mirea.dikanev.nikita.common.server.protocol.pack.PositionPackage;
import ru.mirea.dikanev.nikita.common.server.protocol.pack.ReconnectPackage;

public class SectorMessageProcessor extends CellMessageProcessor {

    public SectorMessageProcessor(CellServer server, int nThreads) {
        super(server, nThreads);
    }

    @Override
    protected void communication(CellHandler handler, Message message) {
        if (message.getFrom() != handler.getRootConnector() &&
                !clientService.isAuth(message.getFrom().getClient().orElse(null))) {

            handler.sendMessage(message.getFrom().getChannel(), Message.send(message.getFrom(), "Permission denied"));
            return;
        }

        MessagePackage messagePackage = messageCodec.decode(message.payload());

        if (messagePackage.space == MessagePackage.WORLD
                || messagePackage.space == MessagePackage.CELL_SPACE && messagePackage.receiverId == -1) {

            handler.sendMessage(message);
            return;
        }

        Optional<Client> receiverClient = clientService.getClient(messagePackage.receiverId);
        receiverClient.ifPresentOrElse(client -> handler.sendMessage(client.getChannel().getChannel(), message),
                () -> handler.sendMessage(handler.getRootConnector().getChannel(), message));
    }

    @Override
    protected void reconnect(CellHandler handler, Message message) {
        ReconnectPackage rcPack = reconnectCodec.decode(message.payload());
        clientService.getClient(rcPack.userId)
                .ifPresent(client -> {
                    if (client.getChannel() != null) {
                        handler.sendMessage(client.getChannel().getChannel(), message);
                    }
                });
    }

    @Override
    protected void position(CellHandler handler, Message message) {
        //send to the cell
        ping(message);
        handler.sendMessage(handler.getRootConnector().getChannel(), message);
    }

    @Override
    protected void setState(CellHandler handler, Message message) {
        int id = message.getFrom().getClient().map(Client::getId).orElse(-1);
        if (message.getFrom() == handler.getRootConnector()) {
            PositionPackage posPack = positionCodec.decode(message.payload());
            handler.sendMessage(clientService.getClient(posPack.userId).get().getChannel().getChannel(), message);
            return;
        } else if (id == -1) {
            return;
        }

        super.setState(handler, message);
        handler.sendMessage(handler.getRootConnector().getChannel(), message);

        handler.sendMessage(message.getFrom().getChannel(), Message.send(null, "State has been set"));
    }
}
