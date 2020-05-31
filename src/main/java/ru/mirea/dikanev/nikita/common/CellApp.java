package ru.mirea.dikanev.nikita.common;

import java.io.IOException;
import java.net.InetSocketAddress;

import ru.mirea.dikanev.nikita.common.math.Rectangle;
import ru.mirea.dikanev.nikita.common.server.CellServer;
import ru.mirea.dikanev.nikita.common.server.SectorServer;
import ru.mirea.dikanev.nikita.common.server.exception.AuthenticationException;

public class CellApp {

    public static void main(String[] args) throws IOException, AuthenticationException {
        System.out.println("Server is starting!");

        CellServer server = CellServer.create(1,
                1,
                new InetSocketAddress("localhost", 19000),
                new InetSocketAddress("localhost", 13000),
                new Rectangle(500, 500, 1000, 1000));
        server.start();
    }

}
