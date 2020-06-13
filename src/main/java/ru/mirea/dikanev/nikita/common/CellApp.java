package ru.mirea.dikanev.nikita.common;

import java.io.IOException;
import java.net.InetSocketAddress;

import ru.mirea.dikanev.nikita.common.server.CellServer;
import ru.mirea.dikanev.nikita.common.server.exception.AuthenticationException;

public class CellApp {

    public static void main(String... args) throws IOException, AuthenticationException {
        System.out.println("Server is being started!");

        String cmHost = args.length > 0 ? args[0] : "localhost";
        int cmPort = args.length > 1 ? Integer.parseInt(args[1]) : 11000;

        String cHost = args.length > 2 ? args[2] : "localhost";
        int cPort = args.length > 3 ? Integer.parseInt(args[3]) : 12003;

        startServer(cmHost, cmPort, cHost, cPort);
    }

    public static void startServer(String cmHost, int cmPort, String cHost, int cPort)
            throws IOException, AuthenticationException {
        CellServer server = CellServer.create(1,
                1,
                new InetSocketAddress(cmHost, cmPort),
                new InetSocketAddress(cHost, cPort));
        server.start();
    }

}
