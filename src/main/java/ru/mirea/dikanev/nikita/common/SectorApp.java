package ru.mirea.dikanev.nikita.common;

import java.io.IOException;
import java.net.InetSocketAddress;

import ru.mirea.dikanev.nikita.common.math.Rectangle;
import ru.mirea.dikanev.nikita.common.server.SectorServer;
import ru.mirea.dikanev.nikita.common.server.exception.AuthenticationException;

public class SectorApp {

    public static void main(String[] args) throws IOException, AuthenticationException {
        System.out.println("Server is starting!");

        SectorServer server = SectorServer.create(1,
                1,
                new InetSocketAddress("localhost", 12000),
                new InetSocketAddress("localhost", 13000));
        server.start();
    }

}
