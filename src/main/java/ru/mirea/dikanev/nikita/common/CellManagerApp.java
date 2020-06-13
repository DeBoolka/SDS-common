package ru.mirea.dikanev.nikita.common;

import java.io.IOException;
import java.net.InetSocketAddress;

import lombok.extern.log4j.Log4j2;
import ru.mirea.dikanev.nikita.common.server.CellManagerServer;
import ru.mirea.dikanev.nikita.common.server.connector.ServerSocketChannelConnector;
import ru.mirea.dikanev.nikita.common.server.exception.AuthenticationException;

@Log4j2
public class CellManagerApp {

    public static void main(String[] args) throws IOException, AuthenticationException {
        System.out.println("Main is being started!");

        String uHost = args.length > 0 ? args[0] : "localhost";
        int uPort = args.length > 1 ? Integer.parseInt(args[1]) : 18000;

        String cmHost = args.length > 2 ? args[2] : "localhost";
        int cmPort = args.length > 3 ? Integer.parseInt(args[3]) : 11000;

        startServer(uHost, uPort, cmHost, cmPort);
        return;
    }

    public static void startServer(String uHost, int uPort, String cmHost, int cmPort)
            throws IOException, AuthenticationException {
        CellManagerServer master = CellManagerServer.create(1, 1);
        master.start();
        master.bind(new ServerSocketChannelConnector(new InetSocketAddress(uHost, uPort)));
        master.bindCellAccepter(new ServerSocketChannelConnector(new InetSocketAddress(cmHost, cmPort)));

        log.info("User host: {}:{}", uHost, uPort);
        log.info("Cell host: {}:{}", cmHost, cmPort);
    }

}
