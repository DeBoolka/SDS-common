package ru.mirea.dikanev.nikita.common;

import java.io.IOException;
import java.net.InetSocketAddress;

import ru.mirea.dikanev.nikita.common.server.MessageServer;
import ru.mirea.dikanev.nikita.common.server.handler.MasterRemoteMessageHandler;
import ru.mirea.dikanev.nikita.common.server.handler.SlaveRemoteMessageServer;

public class App {

    public static void main(String[] args) throws IOException {
        System.out.println("Server has been started!");

        startMaster();
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
