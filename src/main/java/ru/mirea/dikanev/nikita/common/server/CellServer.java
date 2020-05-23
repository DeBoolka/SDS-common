package ru.mirea.dikanev.nikita.common.server;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.stream.IntStream;

import ru.mirea.dikanev.nikita.common.server.exception.AuthenticationException;
import ru.mirea.dikanev.nikita.common.server.exception.HandlerInternalException;
import ru.mirea.dikanev.nikita.common.server.handler.CellHandler;
import ru.mirea.dikanev.nikita.common.server.processor.CellMessageProcessor;

public class CellServer extends SimpleMessageServer {

    private CellServer() {
    }

    public static CellServer create(int nMessageProcessors, int nHandlers) {
        CellServer server = new CellServer();
        CellMessageProcessor processor = new CellMessageProcessor(server, nMessageProcessors);

        server.processor(processor);
        server.handlers(IntStream.range(0, nHandlers)
                .mapToObj(i -> {
                    try {
                        return CellHandler.create(processor);
                    } catch (IOException e) {
                        throw new HandlerInternalException("Created Cell handler failed", e);
                    }
                })
                .toArray(CellHandler[]::new));

        return server;
    }

    public CellHandler bindServer(SocketAddress address) throws IOException, AuthenticationException {
        CellHandler handler = (CellHandler) balanceHandlers();
        handler.bindServer(address);

        return handler;
    }

    public CellHandler bindClient(SocketAddress address) throws IOException, AuthenticationException {
        CellHandler handler = (CellHandler) balanceHandlers();
        handler.bindClient(address);

        return handler;
    }

}
