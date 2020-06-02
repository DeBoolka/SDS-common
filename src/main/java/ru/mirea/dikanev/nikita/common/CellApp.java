package ru.mirea.dikanev.nikita.common;

import java.io.IOException;
import java.net.InetSocketAddress;

import ru.mirea.dikanev.nikita.common.server.CellServer;
import ru.mirea.dikanev.nikita.common.server.exception.AuthenticationException;

public class CellApp {

    public static void main(String[] args) throws IOException, AuthenticationException {
        System.out.println("Server is starting!");

        CellServer server = CellServer.create(1,
                1,
                new InetSocketAddress("localhost", 11000),
                new InetSocketAddress("localhost", 12000));
        server.start();
    }

}
