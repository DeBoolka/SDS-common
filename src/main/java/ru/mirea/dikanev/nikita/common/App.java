package ru.mirea.dikanev.nikita.common;

import java.io.IOException;
import java.net.InetSocketAddress;

import ru.mirea.dikanev.nikita.common.server.entity.Message;
import ru.mirea.dikanev.nikita.common.server.MessageServer;
import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;
import ru.mirea.dikanev.nikita.common.server.connector.ServerSocketChannelConnector;
import ru.mirea.dikanev.nikita.common.server.connector.SocketChannelConnector;
import ru.mirea.dikanev.nikita.common.server.handler.MasterRemoteMessageHandler;
import ru.mirea.dikanev.nikita.common.server.handler.MessageHandler;
import ru.mirea.dikanev.nikita.common.server.handler.SimpleMessageHandler;
import ru.mirea.dikanev.nikita.common.server.handler.SlaveRemoteMessageServer;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.MessageCodec;

public class App {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Server has been started!");

        dg();
    }

    private static void masterDat() throws IOException, InterruptedException {
        MessageServer master = new MessageServer(1, new SimpleMessageHandler());
        master.start();
        Thread.sleep(100);
        master.bind(new ServerSocketChannelConnector(new InetSocketAddress("127.0.0.1", 18000)));
    }

    private static void slaveDat() throws IOException, InterruptedException {
        MessageHandler msgHandler = new SimpleMessageHandler();
        MessageServer slave = new MessageServer(1, msgHandler);
        slave.start();
        Thread.sleep(100);
        slave.bind(new SocketChannelConnector(new InetSocketAddress("127.0.0.1", 18000)));
        slave.bind(new ServerSocketChannelConnector(new InetSocketAddress("127.0.0.1", 19000)));
    }

    private static void dg() throws IOException, InterruptedException {
        MessageHandler msgHandler;
        msgHandler = new SimpleMessageHandler();
        MessageServer sendServer = new MessageServer(1, msgHandler);
        sendServer.start();

        Thread.sleep(100);
        ChannelConnector sendCon = new SocketChannelConnector(new InetSocketAddress("127.0.0.1", 19000));
        sendServer.bind(new SocketChannelConnector(new InetSocketAddress("127.0.0.1", 19000)));
        sendServer.bind(sendCon);

        Thread.sleep(5000);

        sendServer.send(new Message(sendCon, MessageCodec.newMessagePack("Hello UDP")));
    }

    static void startMaster() {
        MessageServer server = new MessageServer(2,
                new MasterRemoteMessageHandler(new InetSocketAddress("127.0.0.1", 18000)));
        server.start();
    }

    static void startSlave() {
        MessageServer server = new MessageServer(2,
                new SlaveRemoteMessageServer(new InetSocketAddress("127.0.0.1", 18000),
                        new InetSocketAddress("127.0.0.1", 19000)));
        server.start();
    }

}
