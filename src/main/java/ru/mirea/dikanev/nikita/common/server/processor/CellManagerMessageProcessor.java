package ru.mirea.dikanev.nikita.common.server.processor;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.extern.log4j.Log4j2;
import ru.mirea.dikanev.nikita.common.math.Rectangle;
import ru.mirea.dikanev.nikita.common.server.CellManagerServer;
import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;
import ru.mirea.dikanev.nikita.common.server.entity.Message;
import ru.mirea.dikanev.nikita.common.server.handler.CellManagerHandler;
import ru.mirea.dikanev.nikita.common.server.handler.MessageHandler;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.AddressCodec;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.PositionCodec;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.ReconnectCodec;
import ru.mirea.dikanev.nikita.common.server.protocol.pack.AddressPackage;
import ru.mirea.dikanev.nikita.common.server.protocol.pack.PositionPackage;
import ru.mirea.dikanev.nikita.common.server.protocol.pack.ReconnectPackage;
import ru.mirea.dikanev.nikita.common.server.service.client.ReconnectService;
import ru.mirea.dikanev.nikita.common.server.service.client.SimpleReconnectService;

@Log4j2
public class CellManagerMessageProcessor implements MessageProcessor {

    private CellManagerServer server;

    private ExecutorService messageTasks;

    private ReconnectCodec reconnectCodec = new ReconnectCodec();
    private PositionCodec positionCodec = new PositionCodec();
    private AddressCodec addressCodec = new AddressCodec();

    private Map<Integer, ChannelConnector> reconnectingChannels = new ConcurrentHashMap<>();

    public CellManagerMessageProcessor(CellManagerServer server, int nThreads) {
        this.server = server;
        this.messageTasks = Executors.newFixedThreadPool(nThreads);
    }

    @Override
    public void process(MessageHandler handler, Message message) {
        messageTasks.submit(() -> {
            try {
                if (Codes.GET_ADDRESS_ACTION == message.getAction()) {
                    getAddress((CellManagerHandler) handler, message);
                    return;
                } else if (Codes.SET_ADDRESS_ACTION == message.getAction()) {
                    setAddress((CellManagerHandler) handler, message);
                    return;
                } else if (Codes.GET_RECTANGLE_ACTION == message.getAction()) {
                    getRectangle((CellManagerHandler) handler, message);
                    return;
                } else if (Codes.GET_SECTOR_ADDRESS_ACTION == message.getAction()) {
                    getSectorAddress((CellManagerHandler) handler, message);
                    return;
                } else if (Codes.RECONNECT_ACTION == message.getAction()) {
                    reconnectAddress((CellManagerHandler) handler, message);
                    return;
                }
                server.getMessageHandlers().forEach(h -> h.sendMessage(message));
            } catch (Exception e) {
                log.error("Process has failed", e);
            }
        });
    }

    private void reconnectAddress(CellManagerHandler handler, Message message) {
        ReconnectPackage recPack = reconnectCodec.decode(message.payload());

        Optional.ofNullable(reconnectingChannels.get(recPack.userId)).ifPresent(connector -> {
            reconnectingChannels.remove(recPack.userId);
            handler.sendMessage(connector.getChannel(), message);
        });
    }

    private void setAddress(CellManagerHandler handler, Message message) {
        AddressPackage addrPack = addressCodec.decode(message.payload());
        InetSocketAddress addr = new InetSocketAddress(new String(addrPack.host), addrPack.port);
        handler.setAddrCell(message.getFrom(), addr);
        log.info("Cell addr was added for {}: {}", message.getFrom(), addr);

        getRectangle(handler, message);
    }

    private void getAddress(CellManagerHandler handler, Message message) {
        PositionPackage posPack = positionCodec.decode(message.payload());
        InetSocketAddress addr = handler.getAddrCell(posPack.x, posPack.y);
        if (addr == null) {
            handler.sendMessage(message.getFrom().getChannel(),
                    Message.send(message.getFrom(), "Server was not found"));
            return;
        }

        handler.sendMessage(message.getFrom().getChannel(),
                Message.create(null,
                        Codes.RECONNECT_ACTION,
                        ReconnectCodec.newReconnectPack(posPack.userId,
                                posPack.x,
                                posPack.y,
                                addr.getHostName().getBytes(),
                                addr.getPort())));
    }

    private void getSectorAddress(CellManagerHandler handler, Message message) {
        PositionPackage posPack = positionCodec.decode(message.payload());
        if (posPack.userId < 0) {
            // TODO: 01.06.2020 it doesn't work with multiple players
//            return;
        }

        reconnectingChannels.put(posPack.userId, message.getFrom());
        handler.sendMessage(handler.getCellInfo(posPack.x, posPack.y).channelConnector.getChannel(),
                Message.create(null,
                        Codes.GET_ADDRESS_ACTION,
                        PositionCodec.newPositionPack(posPack.userId, posPack.x, posPack.y)));
    }

    protected void getRectangle(CellManagerHandler handler, Message message) {
        Rectangle rectangle = handler.getRectangle(message.getFrom());
        handler.sendMessage(message.getFrom().getChannel(),
                Message.create(null,
                        Codes.SET_RECTANGLE_ACTION,
                        PositionCodec.newPositionPack(-1, rectangle.upperLeftCorner.x, rectangle.upperLeftCorner.y),
                        PositionCodec.newPositionPack(-1, rectangle.bottomRightCorner.x, rectangle.bottomRightCorner.y)));
    }

    @Override
    public void clear(MessageHandler handler) {
        messageTasks.shutdown();
    }
}
