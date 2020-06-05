package ru.mirea.dikanev.nikita.common.server.processor;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import ru.mirea.dikanev.nikita.common.server.CellServer;
import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;
import ru.mirea.dikanev.nikita.common.server.entity.Message;
import ru.mirea.dikanev.nikita.common.server.entity.client.Client;
import ru.mirea.dikanev.nikita.common.server.handler.CellHandler;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.PositionCodec;
import ru.mirea.dikanev.nikita.common.server.protocol.pack.MessagePackage;
import ru.mirea.dikanev.nikita.common.server.protocol.pack.PositionPackage;
import ru.mirea.dikanev.nikita.common.server.protocol.pack.ReconnectPackage;

public class SectorMessageProcessor extends CellMessageProcessor {

    protected Map<Integer, ChannelConnector> sectorSubscribes = new ConcurrentHashMap<>();

    public SectorMessageProcessor(CellServer server, int nThreads) {
        super(server, nThreads);
    }

    @Override
    protected void action(CellHandler handler, int actionCode, Message message) {
        switch (actionCode) {
            case SUBSCRIBE_TO_POSITION_ACTION:
                subscribeToPosition(handler, message);
                return;
        }

        super.action(handler, actionCode, message);
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

        PositionPackage posPack = positionCodec.decode(message.payload());
        sectorSubscribes.forEach((id, connector) -> handler.sendMessage(connector.getChannel(),
                Message.create(null,
                        Codes.SUBSCRIBED_POSITION_ACTION,
                        PositionCodec.newPositionPack(posPack.userId, posPack.x, posPack.y))));

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

    private void subscribeToPosition(CellHandler handler, Message message) {
        MessagePackage msgPack = messageCodec.decode(message.payload());
        if (msgPack.space != MessagePackage.SECTOR_SPACE) {
            cellSubscribes.put(msgPack.receiverId, message.getFrom());
            handler.sendMessage(handler.getRootConnector().getChannel(), message);
            return;
        }
        sectorSubscribes.put(msgPack.receiverId, message.getFrom());
    }

    @Override
    protected Map<Integer, ChannelConnector> subscribers() {
        return cellSubscribes;
    }
}
