package ru.mirea.dikanev.nikita.common;

import java.io.IOException;
import java.net.InetSocketAddress;

import ru.mirea.dikanev.nikita.common.server.CellManagerServer;
import ru.mirea.dikanev.nikita.common.server.connector.ServerSocketChannelConnector;
import ru.mirea.dikanev.nikita.common.server.exception.AuthenticationException;

public class CellManagerApp {

    public static void main(String[] args) throws IOException, AuthenticationException {
        System.out.println("Server is starting!");

        CellManagerServer master = CellManagerServer.create(1, 1);
        master.start();
        master.bind(new ServerSocketChannelConnector(new InetSocketAddress("127.0.0.1", 18000)));
        master.bindCellAccepter(new ServerSocketChannelConnector(new InetSocketAddress("127.0.0.1", 11000)));
    }

}
