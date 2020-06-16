package ru.mirea.dikanev.nikita.common.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.stream.IntStream;

import lombok.extern.log4j.Log4j2;
import ru.mirea.dikanev.nikita.common.server.exception.AuthenticationException;
import ru.mirea.dikanev.nikita.common.server.exception.HandlerInternalException;
import ru.mirea.dikanev.nikita.common.server.handler.CellHandler;
import ru.mirea.dikanev.nikita.common.server.processor.SectorMessageProcessor;

@Log4j2
public class SectorServer extends CellServer {

    private SectorServer() {
    }

    public static SectorServer create(int nMessageProcessors, int nHandlers, InetSocketAddress cell,
            InetSocketAddress localAddress) throws IOException, AuthenticationException {

        SectorServer server = new SectorServer();
        SectorMessageProcessor processor = new SectorMessageProcessor(server, nMessageProcessors);

        server.processor(processor);
        server.handlers(IntStream.range(0, nHandlers).mapToObj(i -> {
            try {
                return CellHandler.create(processor);
            } catch (IOException e) {
                throw new HandlerInternalException("Created Sector handler failed", e);
            }
        }).toArray(CellHandler[]::new));

        //log.info("Connect to Cell: {}", cell);
        server.bindClient(cell);
        //log.info("Creat a server socket: {}", localAddress);
        server.bindServer(localAddress);

        server.remoteAddr = cell;
        server.localServerAddr = localAddress;

        return server;
    }

    public CellHandler bindServer(InetSocketAddress address) throws IOException, AuthenticationException {
        CellHandler handler = (CellHandler) balanceHandlers();
        handler.bindServer(address);

        return handler;
    }

    public CellHandler bindClient(InetSocketAddress address) throws IOException, AuthenticationException {
        CellHandler handler = (CellHandler) balanceHandlers();
        handler.bindClient(address);

        return handler;
    }

}
