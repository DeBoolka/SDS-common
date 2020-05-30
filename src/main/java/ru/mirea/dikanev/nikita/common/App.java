package ru.mirea.dikanev.nikita.common;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import ru.mirea.dikanev.nikita.common.math.Rectangle;
import ru.mirea.dikanev.nikita.common.server.CellManagerServer;
import ru.mirea.dikanev.nikita.common.server.CellServer;
import ru.mirea.dikanev.nikita.common.server.MessageServer;
import ru.mirea.dikanev.nikita.common.server.SimpleMessageServer;
import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;
import ru.mirea.dikanev.nikita.common.server.connector.ServerSocketChannelConnector;
import ru.mirea.dikanev.nikita.common.server.connector.SocketChannelConnector;
import ru.mirea.dikanev.nikita.common.server.entity.Message;
import ru.mirea.dikanev.nikita.common.server.exception.AuthenticationException;
import ru.mirea.dikanev.nikita.common.server.handler.CellManagerHandler;
import ru.mirea.dikanev.nikita.common.server.handler.MasterRemoteMessageHandler;
import ru.mirea.dikanev.nikita.common.server.handler.MessageHandler;
import ru.mirea.dikanev.nikita.common.server.handler.SimpleMessageHandler;
import ru.mirea.dikanev.nikita.common.server.handler.SlaveRemoteMessageServer;
import ru.mirea.dikanev.nikita.common.server.processor.CellManagerMessageProcessor;
import ru.mirea.dikanev.nikita.common.server.processor.Codes;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.LoginCodec;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.MessageCodec;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.ReconnectCodec;
import ru.mirea.dikanev.nikita.common.server.protocol.pack.MessagePackage;

public class App {

    public static void main(String[] args) throws IOException, InterruptedException, AuthenticationException {
        System.out.println("Server has been started!");

        /*CellServer server = CellServer.create(1, 1);
        server.bindClient(new InetSocketAddress("localhost", 18000));
        server.bindServer(new InetSocketAddress("localhost", 11000));
        server.start();*/
        masterDat();
//        slaveDat();
    }

    private static void masterDat() throws IOException, InterruptedException {
        CellManagerServer master = CellManagerServer.create(1, 1);
        master.start();
        master.bind(new ServerSocketChannelConnector(new InetSocketAddress("127.0.0.1", 18000)));
    }

    private static void slaveDat() throws IOException, InterruptedException {
//        MessageHandler msgHandler = new SimpleMessageHandler();
//        MessageServer slave = new SimpleMessageServer(1, msgHandler);
        CellServer slave = CellServer.create(1, 1, new Rectangle(0, 100, 100, 0));
        SocketChannelConnector channel = new SocketChannelConnector(new InetSocketAddress("127.0.0.1", 19000));
        slave.start();
        slave.bind(channel);
        Thread.sleep(100);

        slave.send(Message.create(null, Codes.LOGIN_ACTION, LoginCodec.newLoginPack("admin", "admin")));
        Thread.sleep(2000);
        slave.send(Message.send(null, MessageCodec.newMessagePack(MessagePackage.WORLD, "Hi WORLD2 from Cell1")));

        Thread.sleep(5000);
        System.out.println("I have woken up");
        slave.send(Message.create(channel, Codes.RECONNECT_ACTION, ReconnectCodec.newReconnectPack("localhost", 11000)));
    }

}
