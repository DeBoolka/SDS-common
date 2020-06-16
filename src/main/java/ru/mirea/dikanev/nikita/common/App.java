package ru.mirea.dikanev.nikita.common;

import java.io.IOException;
import java.net.InetSocketAddress;

import ru.mirea.dikanev.nikita.common.math.Rectangle;
import ru.mirea.dikanev.nikita.common.server.CellManagerServer;
import ru.mirea.dikanev.nikita.common.server.CellServer;
import ru.mirea.dikanev.nikita.common.server.SectorServer;
import ru.mirea.dikanev.nikita.common.server.connector.ServerSocketChannelConnector;
import ru.mirea.dikanev.nikita.common.server.connector.SocketChannelConnector;
import ru.mirea.dikanev.nikita.common.server.entity.Message;
import ru.mirea.dikanev.nikita.common.server.exception.AuthenticationException;
import ru.mirea.dikanev.nikita.common.server.processor.Codes;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.LoginCodec;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.MessageCodec;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.ReconnectCodec;
import ru.mirea.dikanev.nikita.common.server.protocol.pack.MessagePackage;

public class App {

    public static final String LOCAL_HOST = "localhost";

    public static final String cmHost = LOCAL_HOST;
    public static final int cmPort = 11000;

    public static final String cHost = LOCAL_HOST;
    public static final int cPort = 12000;

    public static final String sHost = LOCAL_HOST;
    public static final int sPort = 13000;

    public static final String uHost = LOCAL_HOST;
    public static final int uPort = 18000;

    public static void main(String[] args) throws IOException, InterruptedException, AuthenticationException {
        System.out.println("Main is being started!");

        CellManagerApp.startServer(uHost, uPort, cmHost, cmPort);

        Thread.sleep(2000);

        CellApp.startServer(cmHost, cmPort, cHost, cPort);
        Thread.sleep(200);
        CellApp.startServer(cmHost, cmPort, cHost, cPort + 1);
        Thread.sleep(200);
        CellApp.startServer(cmHost, cmPort, cHost, cPort + 2);
        Thread.sleep(200);
        CellApp.startServer(cmHost, cmPort, cHost, cPort + 3);

        Thread.sleep(5000);

        SectorApp.startServer(cHost, cPort, sHost, sPort);
        SectorApp.startServer(cHost, cPort, sHost, sPort + 10);
        SectorApp.startServer(cHost, cPort + 1, sHost, sPort + 1);
        SectorApp.startServer(cHost, cPort + 2, sHost, sPort + 2);
        SectorApp.startServer(cHost, cPort + 3, sHost, sPort + 3);
    }

}
