package ru.mirea.dikanev.nikita.common.server.processor;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import ru.mirea.dikanev.nikita.common.balance.Balancer;
import ru.mirea.dikanev.nikita.common.balance.voronoi.graph.VoronoiPoint;
import ru.mirea.dikanev.nikita.common.math.Point;
import ru.mirea.dikanev.nikita.common.math.Rectangle;
import ru.mirea.dikanev.nikita.common.server.CellServer;
import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;
import ru.mirea.dikanev.nikita.common.server.entity.Message;
import ru.mirea.dikanev.nikita.common.server.entity.PlayerState;
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
import ru.mirea.dikanev.nikita.common.server.secure.AuthenticationClient;
import ru.mirea.dikanev.nikita.common.server.service.PlayerService;
import ru.mirea.dikanev.nikita.common.server.service.SimplePlayerService;
import ru.mirea.dikanev.nikita.common.server.service.client.ClientService;
import ru.mirea.dikanev.nikita.common.server.service.client.ReconnectService;
import ru.mirea.dikanev.nikita.common.server.service.client.SimpleClientService;
import ru.mirea.dikanev.nikita.common.server.service.client.SimpleReconnectService;

@Log4j2
@Data
public class CellMessageProcessor implements MessageProcessor, Codes {

    protected static final double BUFFER_ZONE_NEAR_BORDERS = 100;

    protected CellServer server;
    protected ClientService clientService;
    protected PlayerService playerService;
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
        this.reconnectService = new SimpleReconnectService();
        this.clientService = new SimpleClientService();
        this.playerService = new SimplePlayerService();
    }

    @Override
    public void process(MessageHandler handler, Message message) {
        messageTasks.submit(() -> {
            try {
                int actionCode = message.getAction();
                action((CellHandler) handler, actionCode, message);
            } catch (Exception e) {
                log.error("Process has failed", e);
            }
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
            case SET_STATE_ACTION:
                setState(handler, message);
                return;
            case SET_CLIENT:
                setClient(handler, message);
                return;
            case BALANCE_ACTION:
                balanceAction(handler, message);
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
            newMessage = Message.create(null,
                    Codes.LOGIN_ACTION,
                    MessageCodec.newByteMessagePack(MessagePackage.WORLD, client.getId(), "Login successful"));
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
            return ((ChannelConnector) key.attachment()) != handler.getRootConnector();
        });
    }

    protected void ping(Message message) {
        PositionPackage posPack = positionCodec.decode(message.payload());
        clientService.getClient(posPack.userId).ifPresent(clientFromService -> {
            message.getFrom().getClient().ifPresent(clientFromChannel -> {
                if (clientFromChannel.getId() != clientFromService.getId()) {
                    message.getFrom().setClient(clientFromService);
                    clientFromService.setChannel(message.getFrom());
                }
            });
        });
    }

    protected void reconnect(CellHandler handler, Message message) {
        ReconnectPackage recPackage = reconnectCodec.decode(message.payload());
        if (recPackage.userId >= 0) {
            Optional.ofNullable(reconnectService.pop(recPackage.userId))
                    .ifPresent(c -> handler.sendMessage(c.getChannel().getChannel(),
                            Message.create(null, Codes.RECONNECT_ACTION, ReconnectCodec.newReconnectPack(recPackage))));
        }
    }

    protected void position(CellHandler handler, Message message) {
        PositionPackage posPackage = positionCodec.decode(message.payload());
        Client client = message.getFrom().getClient().orElse(null);
        if (client == null || posPackage.userId == -1) {
            return;
        }

        Point moveVector = new Point(posPackage.x, posPackage.y);
        Point position = playerService.getMap()
                .computeIfAbsent(posPackage.userId, k -> new PlayerState(new Point(0, 0))).position;

        position.x += moveVector.x;
        position.y += moveVector.y;

        handler.sendMessage(message.getFrom().getChannel(),
                Message.create(null,
                        SET_STATE_ACTION,
                        PositionCodec.newPositionPack(posPackage.userId, position.x, position.y)));

        if (cellRectangle.isIntersectionBufferZone(BUFFER_ZONE_NEAR_BORDERS, position)) {
            reconnectService.push(posPackage.userId, client);
            handler.sendMessage(handler.getRootConnector().getChannel(),
                    Message.create(null,
                            GET_SECTOR_ADDRESS_ACTION,
                            PositionCodec.newPositionPack(posPackage.userId, position.x, position.y)));
        }
    }

    protected void setSectorAddr(CellHandler handler, Message message) {
        AddressPackage addrPack = addressCodec.decode(message.payload());

        InetSocketAddress addr = new InetSocketAddress(new String(addrPack.host), addrPack.port);
        handler.setSector(message.getFrom(), addr);
        log.info("Sector addr was added for {}: {}", message.getFrom(), addr);

        getRectangle(handler, message);
    }

    protected void getSectorAddr(CellHandler handler, Message message) {
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

        if (posPack.userId <= 0) {
            return;
        }

        //This is turd. When a user is moving to this cell, it should call this method. At this moment, its id > 0
        playerService.getMap().put(posPack.userId, new PlayerState(new Point(posPack.x, posPack.y)));
        ChannelConnector sectorConnector = handler.getSector(posPack.x, posPack.y).getKey();
        handler.sendMessage(sectorConnector.getChannel(),
                Message.create(null,
                        Codes.SET_CLIENT,
                        PositionCodec.newPositionPack(posPack.userId, posPack.x, posPack.y)));
    }

    protected void setRectangle(CellHandler handler, Message message) {
        ByteBuffer payload = message.payload();
        PositionPackage upperLeftCorner = positionCodec.decode(payload);
        PositionPackage bottomRightCorner = positionCodec.decode(payload);

        if (upperLeftCorner.x == -1 && upperLeftCorner.y == -1
                && bottomRightCorner.x == -1 && bottomRightCorner.y == -1) {

            handler.sendMessage(handler.getRootConnector().getChannel(),
                    Message.create(null,
                            Codes.GET_RECTANGLE_ACTION,
                            AddressCodec.newAddressPack(server.localServerAddr.getHostName(),
                                    server.localServerAddr.getPort())));
            return;
        }

        this.cellRectangle = new Rectangle(upperLeftCorner.x,
                upperLeftCorner.y,
                bottomRightCorner.x,
                bottomRightCorner.y);
        handler.setRectangle(this.cellRectangle);

        handler.getSectors().forEach(sector -> handler.sendMessage(sector.getChannel(), message));
    }

    protected void getRectangle(CellHandler handler, Message message) {
        Rectangle rectangle = handler.getRectangle();
        handler.sendMessage(message.getFrom().getChannel(),
                Message.create(null,
                        Codes.SET_RECTANGLE_ACTION,
                        PositionCodec.newPositionPack(-1, rectangle.upperLeftCorner.x, rectangle.upperLeftCorner.y),
                        PositionCodec.newPositionPack(-1, rectangle.bottomRightCorner.x, rectangle.bottomRightCorner.y)));
    }

    protected void setState(CellHandler handler, Message message) {
        PositionPackage posPack = positionCodec.decode(message.payload());
        if (posPack.userId < 0) {
            return;
        }

        Point position = new Point(posPack.x, posPack.y);

        Point oldPosition = playerService.getMap()
                .computeIfAbsent(posPack.userId, k -> new PlayerState(position))
                .getPosition();

        oldPosition.x = position.x;
        oldPosition.y = position.y;
    }

    private void setClient(CellHandler handler, Message message) {
        PositionPackage posPack = positionCodec.decode(message.payload());
        if (posPack.userId < 0) {
            return;
        }

        Client client = clientService.getClient(posPack.userId).orElse(new AuthenticationClient(posPack.userId));
        clientService.newSession(client, new Point(posPack.x, posPack.y));
    }

    protected void balanceAction(CellHandler handler, Message message) {
        List<VoronoiPoint> points = playerService.getMap()
                .entrySet()
                .stream()
                .map(entry -> new VoronoiPoint(entry.getValue().getPosition(), entry.getKey()))
                .collect(Collectors.toList());

        List<InetSocketAddress> addresses = List.copyOf(handler.getSectorAddresses().values());
        Balancer balancer = new Balancer(points).cluster(addresses.size());
        IntStream.range(0, addresses.size())
                .forEach(i -> balancer.clusters()
                        .get(i)
                        .forEach(point -> handler.getSectors()
                                .forEach(connector -> {
                                    handler.sendMessage(connector.getChannel(),
                                            Message.create(null,
                                                    Codes.SET_CLIENT,
                                                    PositionCodec.newPositionPack(point.playerId,
                                                            (int) point.x(),
                                                            (int) point.y())));
                                    handler.sendMessage(connector.getChannel(),
                                            Message.create(null,
                                                    Codes.RECONNECT_ACTION,
                                                    ReconnectCodec.newReconnectPack(point.playerId,
                                                            (int) point.x(),
                                                            (int) point.y(),
                                                            addresses.get(i).getHostName().getBytes(),
                                                            addresses.get(i).getPort())));
                                })));

        //TODO: The Sectors don't know about other players. After reconnecting, players will log out
    }

    @Override
    public void clear(MessageHandler handler) {

    }
}
