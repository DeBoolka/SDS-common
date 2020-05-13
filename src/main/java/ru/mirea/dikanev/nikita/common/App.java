package ru.mirea.dikanev.nikita.common;

import java.io.IOException;
import java.net.InetSocketAddress;

import ru.mirea.dikanev.nikita.common.server.MessageHandler;
import ru.mirea.dikanev.nikita.common.server.MessageServer;
import ru.mirea.dikanev.nikita.common.server.connector.ServerSocketChannelConnector;

public class App {

    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println("Server has been started!");

        MessageServer server = new MessageServer(2, new MessageHandler());
        server.start();
        server.bind(new ServerSocketChannelConnector(new InetSocketAddress("127.0.0.1", 19000)));
//        server.stop();
    }

}
