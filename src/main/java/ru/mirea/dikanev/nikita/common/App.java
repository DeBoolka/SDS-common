package ru.mirea.dikanev.nikita.common;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import ru.mirea.dikanev.nikita.common.server.CellServer;
import ru.mirea.dikanev.nikita.common.server.MessageServer;
import ru.mirea.dikanev.nikita.common.server.SimpleMessageServer;
import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;
import ru.mirea.dikanev.nikita.common.server.connector.ServerSocketChannelConnector;
import ru.mirea.dikanev.nikita.common.server.connector.SocketChannelConnector;
import ru.mirea.dikanev.nikita.common.server.entity.Message;
import ru.mirea.dikanev.nikita.common.server.exception.AuthenticationException;
import ru.mirea.dikanev.nikita.common.server.handler.MasterRemoteMessageHandler;
import ru.mirea.dikanev.nikita.common.server.handler.MessageHandler;
import ru.mirea.dikanev.nikita.common.server.handler.SimpleMessageHandler;
import ru.mirea.dikanev.nikita.common.server.handler.SlaveRemoteMessageServer;
import ru.mirea.dikanev.nikita.common.server.processor.Codes;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.LoginCodec;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.MessageCodec;

public class App {

    public static void main(String[] args) throws IOException, InterruptedException, AuthenticationException {
        System.out.println("Server has been started!");

        /*CellServer server = CellServer.create(1, 1);
        server.bindClient(new InetSocketAddress("localhost", 18000));
        server.bindServer(new InetSocketAddress("localhost", 19000));
        server.start();*/
//        masterDat();
        slaveDat();
    }

    private static void masterDat() throws IOException, InterruptedException {
        MessageServer master = new SimpleMessageServer(1, new SimpleMessageHandler());
        master.start();
        Thread.sleep(100);
        master.bind(new ServerSocketChannelConnector(new InetSocketAddress("127.0.0.1", 18000)));
    }

    private static void slaveDat() throws IOException, InterruptedException {
        MessageHandler msgHandler = new SimpleMessageHandler();
        MessageServer slave = new SimpleMessageServer(1, msgHandler);
        slave.start();
        slave.bind(new SocketChannelConnector(new InetSocketAddress("127.0.0.1", 19000)));
        Thread.sleep(100);

        slave.send(Message.create(null, Codes.LOGIN_ACTION, LoginCodec.newLoginPack("admin", "admin")));
        Thread.sleep(2000);
        slave.send(Message.send(null, "Hello mather fuckers"));
    }

}
