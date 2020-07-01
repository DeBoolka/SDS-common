package ru.mirea.dikanev.nikita.common;

import java.io.IOException;
import java.net.InetSocketAddress;

import ru.mirea.dikanev.nikita.common.server.TestServer;
import ru.mirea.dikanev.nikita.common.server.connector.ServerSocketChannelConnector;

public class TestServerApp {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Server is being started!");
        long t = System.currentTimeMillis();
        System.out.println(t);
        System.out.println(t % 10000000);

        String cHost = args.length > 0 ? args[0] : "192.168.2.40";
        int cPort = args.length > 1 ? Integer.parseInt(args[1]) : 12000;

        int processors = args.length > 3 ? Integer.parseInt(args[2]) : Runtime.getRuntime().availableProcessors() - 1;
        System.out.println("Processors: " + processors);

        startServer(cHost, cPort, processors);

    }

    public static void startServer(String cHost, int cPort, int processors) throws IOException, InterruptedException {

        TestServer testServer = TestServer.create(processors);
        testServer.start();
        Thread.sleep(1000);
        testServer.bind(new ServerSocketChannelConnector(new InetSocketAddress(cHost, cPort)));
    }

}
