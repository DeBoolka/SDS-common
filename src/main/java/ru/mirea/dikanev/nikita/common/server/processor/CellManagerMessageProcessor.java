package ru.mirea.dikanev.nikita.common.server.processor;

import java.net.InetSocketAddress;
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

@Log4j2
public class CellManagerMessageProcessor implements MessageProcessor {

    private CellManagerServer server;

    private ExecutorService messageTasks;

    private PositionCodec positionCodec = new PositionCodec();
    private AddressCodec addressCodec = new AddressCodec();

    public CellManagerMessageProcessor(CellManagerServer server, int nThreads) {
        this.server = server;
        this.messageTasks = Executors.newFixedThreadPool(nThreads);
    }

    @Override
    public void process(MessageHandler handler, Message message) {
        messageTasks.submit(() -> {
            if (Codes.GET_ADDRESS_ACTION == message.getAction()) {
                getAddress((CellManagerHandler) handler, message);
                return;
            } else if (Codes.SET_ADDRESS_ACTION == message.getAction()) {
                setAddress((CellManagerHandler) handler, message);
                return;
            } else if (Codes.GET_RECTANGLE_ACTION == message.getAction()) {
                getRectangle((CellManagerHandler) handler, message);
                return;
            }
            server.getMessageHandlers().forEach(h -> h.sendMessage(message));
        });
    }

    private void setAddress(CellManagerHandler handler, Message message) {
        AddressPackage addrPack = addressCodec.decode(message.payload());
        InetSocketAddress addr = new InetSocketAddress(new String(addrPack.host), addrPack.port);
        handler.setAddrCell(message.getFrom(), addr);
        log.info("Cell addr was added for {}: {}", message.getFrom(), addr);
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
