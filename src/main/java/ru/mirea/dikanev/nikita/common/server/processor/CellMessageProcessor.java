package ru.mirea.dikanev.nikita.common.server.processor;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import ru.mirea.dikanev.nikita.common.math.Point;
import ru.mirea.dikanev.nikita.common.math.Rectangle;
import ru.mirea.dikanev.nikita.common.server.CellServer;
import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;
import ru.mirea.dikanev.nikita.common.server.entity.Message;
import ru.mirea.dikanev.nikita.common.server.entity.client.Client;
import ru.mirea.dikanev.nikita.common.server.exception.AuthenticationException;
import ru.mirea.dikanev.nikita.common.server.handler.CellHandler;
import ru.mirea.dikanev.nikita.common.server.handler.MessageHandler;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.AddressCodec;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.LoginCodec;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.MessageCodec;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.PositionCodec;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.ReconnectCodec;
import ru.mirea.dikanev.nikita.common.server.protocol.pack.AddressPackage;
import ru.mirea.dikanev.nikita.common.server.protocol.pack.LoginPackage;
import ru.mirea.dikanev.nikita.common.server.protocol.pack.MessagePackage;
import ru.mirea.dikanev.nikita.common.server.protocol.pack.PositionPackage;
import ru.mirea.dikanev.nikita.common.server.protocol.pack.ReconnectPackage;
import ru.mirea.dikanev.nikita.common.server.service.ClientService;
import ru.mirea.dikanev.nikita.common.server.service.ReconnectService;
import ru.mirea.dikanev.nikita.common.server.service.SimpleClientService;

@Log4j2
@Data
public class CellMessageProcessor implements MessageProcessor, Codes {

    protected static final double BUFFER_ZONE_NEAR_BORDERS = 100;

    protected CellServer server;
    protected ClientService clientService;
    protected ReconnectService reconnectService;

    private ExecutorService messageTasks;

    protected ReconnectCodec reconnectCodec = new ReconnectCodec();
    protected PositionCodec positionCodec = new PositionCodec();
    protected AddressCodec addressCodec = new AddressCodec();
    protected MessageCodec messageCodec = new MessageCodec();
    protected LoginCodec loginCodec = new LoginCodec();

    private Rectangle cellRectangle = new Rectangle(0, 0, 0, 0);

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
            case SET_ADDRESS_ACTION:
                setSectorAddr(handler, message);
                return;
            case GET_ADDRESS_ACTION:
                getSectorAddr(handler, message);
                return;
            case SET_RECTANGLE_ACTION:
                setRectangle(handler, message);
                return;
            case GET_RECTANGLE_ACTION:
                getRectangle(handler, message);
                return;
            default:
                log.warn("Unknown action code: {}", actionCode);
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

    protected void reconnect(CellHandler handler, Message message) {
        ReconnectPackage reconnectPackage = reconnectCodec.decode(message.payload());
        if (reconnectPackage.host.length != 0 && reconnectPackage.port > 0) {
            /*try {
                handler.reconnect(connector);
                connector.reconnect(new InetSocketAddress(new String(reconnectPackage.host), reconnectPackage.port));
                handler.bind(connector);
            } catch (IOException | AuthenticationException e) {
                log.error("Couldn't reconnect to new socket({}, {})",
                        new String(reconnectPackage.host),
                        reconnectPackage.port,
                        e);
            }
            return;*/
            handler.sendMessage(reconnectService.pop(reconnectPackage.userId).getChannel().getChannel(),
                    Message.create(null, Codes.RECONNECT_ACTION, ReconnectCodec.newReconnectPack(reconnectPackage)));
            return;
        }

        ChannelConnector connector = handler.getSector(reconnectPackage.posX, reconnectPackage.posY);
        InetSocketAddress sectorAddr = connector.getLocalAddress();
        reconnectPackage.host = sectorAddr.getHostName().getBytes();
        reconnectPackage.port = sectorAddr.getPort();
        handler.sendMessage(message.getFrom().getChannel(),
                Message.create(null, RECONNECT_ACTION, ReconnectCodec.newReconnectPack(reconnectPackage)));
    }

    protected void position(CellHandler handler, Message message) {
        PositionPackage posPackage = positionCodec.decode(message.payload());
        Client client = message.getFrom().getClient().orElse(null);
        if (client == null) {
            return;
        }

        Point newPoint = new Point(posPackage.x, posPackage.y);
        if (cellRectangle.isIntersectionBufferZone(BUFFER_ZONE_NEAR_BORDERS, newPoint)) {
            reconnectService.push(posPackage.userId, client);
            handler.sendMessage(Message.create(null,
                    RECONNECT_ACTION,
                    ReconnectCodec.newReconnectPack(client.getId(), newPoint.x, newPoint.y)), onlyParentServer());
        }
    }

    private void setSectorAddr(CellHandler handler, Message message) {
        AddressPackage addrPack = addressCodec.decode(message.payload());

        InetSocketAddress addr = new InetSocketAddress(new String(addrPack.host), addrPack.port);
        handler.setSector(message.getFrom(), addr);
        log.info("Sector addr was added for {}: {}", message.getFrom(), addr);
    }

    private void getSectorAddr(CellHandler handler, Message message) {
        PositionPackage posPack = positionCodec.decode(message.payload());
        InetSocketAddress addr = handler.getAddrSector(posPack.x, posPack.y);
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

    private void setRectangle(CellHandler handler, Message message) {
        ByteBuffer payload = message.payload();
        PositionPackage upperLeftCorner = positionCodec.decode(payload);
        PositionPackage bottomRightCorner = positionCodec.decode(payload);

        this.cellRectangle = new Rectangle(upperLeftCorner.x,
                upperLeftCorner.y,
                bottomRightCorner.x,
                bottomRightCorner.y);
        handler.setRectangle(this.cellRectangle);

        handler.getSectors().forEach(sector -> handler.sendMessage(sector.getChannel(), message));
    }

    private void getRectangle(CellHandler handler, Message message) {
        Rectangle rectangle = handler.getRectangle();
        handler.sendMessage(message.getFrom().getChannel(),
                Message.create(null,
                        Codes.SET_RECTANGLE_ACTION,
                        PositionCodec.newPositionPack(-1, rectangle.upperLeftCorner.x, rectangle.upperLeftCorner.y),
                        PositionCodec.newPositionPack(-1, rectangle.bottomRightCorner.x, rectangle.bottomRightCorner.y)));
    }

    protected Predicate<SelectionKey> onlyParentServer() {
        return key -> ((ChannelConnector) key.attachment()).getClient()
                .map(value -> SimpleClientService.ROOT_USER_ID.equals(value.getId()))
                .orElse(false);
    }

    @Override
    public void clear(MessageHandler handler) {

    }
}
