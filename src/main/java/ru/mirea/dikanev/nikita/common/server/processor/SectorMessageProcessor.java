package ru.mirea.dikanev.nikita.common.server.processor;

import java.util.Optional;

import ru.mirea.dikanev.nikita.common.server.CellServer;
import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;
import ru.mirea.dikanev.nikita.common.server.entity.Message;
import ru.mirea.dikanev.nikita.common.server.entity.client.Client;
import ru.mirea.dikanev.nikita.common.server.handler.CellHandler;
import ru.mirea.dikanev.nikita.common.server.protocol.pack.MessagePackage;
import ru.mirea.dikanev.nikita.common.server.service.SimpleClientService;

public class SectorMessageProcessor extends CellMessageProcessor {

    public SectorMessageProcessor(CellServer server, int nThreads) {
        super(server, nThreads);
    }

    @Override
    protected void communication(CellHandler handler, Message message) {
        if (!clientService.isAuth(message.getFrom().getClient().orElse(null))) {
            handler.sendMessage(message.getFrom().getChannel(), Message.send(message.getFrom(), "Permission denied"));
            return;
        }

        MessagePackage messagePackage = messageCodec.decode(message.payload());

        if (messagePackage.space == MessagePackage.WORLD
                || messagePackage.space == MessagePackage.CELL_SPACE && messagePackage.receiverId == 0) {

            handler.sendMessage(message);
            return;
        }

        Optional<Client> receiverClient = clientService.getClient(messagePackage.receiverId);
        receiverClient.ifPresentOrElse(client -> handler.sendMessage(client.getChannel().getChannel(), message),
                () -> handler.sendMessage(message, key -> {
                    //Send to Cell only, excluding another channel
                    //I understand that this is a piece of shit, but now I don't want to do otherwise
                    return ((ChannelConnector) key.attachment()).getClient()
                            .map(value -> SimpleClientService.ROOT_USER_ID.equals(value.getId()))
                            .orElse(false);
                }));
    }
}
