package ru.mirea.dikanev.nikita.common;

import java.io.IOException;
import java.net.InetSocketAddress;

import ru.mirea.dikanev.nikita.common.math.Rectangle;
import ru.mirea.dikanev.nikita.common.server.SectorServer;
import ru.mirea.dikanev.nikita.common.server.exception.AuthenticationException;

public class SectorApp {

    public static void main(String[] args) throws IOException, AuthenticationException {
        System.out.println("Server is starting!");

        String cHost = args.length > 0 ? args[0] : "localhost";
        int cPort = args.length > 1 ? Integer.parseInt(args[1]) : 12000;

        String sHost = args.length > 2 ? args[2] : "localhost";
        int sPort = args.length > 3 ? Integer.parseInt(args[3]) : 13009;

        startServer(cHost, cPort, sHost, sPort);
    }

    public static void startServer(String cHost, int cPort, String sHost, int sPort)
            throws IOException, AuthenticationException {
        SectorServer server = SectorServer.create(1,
                1,
                new InetSocketAddress(cHost, cPort),
                new InetSocketAddress(sHost, sPort));
        server.start();
    }

}
