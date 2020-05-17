package ru.mirea.dikanev.nikita.common.server;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.stream.IntStream;

import ru.mirea.dikanev.nikita.common.server.exception.HandlerInternalException;
import ru.mirea.dikanev.nikita.common.server.handler.CellManagerHandler;
import ru.mirea.dikanev.nikita.common.server.processor.CellManagerMessageProcessor;

public class CellManagerServer extends SimpleMessageServer {

    private CellManagerServer() {
    }

    public static CellManagerServer create(int nMessageProcessors, int nHandlers) {
        CellManagerServer server = new CellManagerServer();
        CellManagerMessageProcessor processor = new CellManagerMessageProcessor(server, nMessageProcessors);

        server.processor(processor);
        server.handlers(IntStream.range(0, nHandlers)
                .mapToObj(i -> {
                    try {
                        return CellManagerHandler.create(processor);
                    } catch (IOException e) {
                        throw new HandlerInternalException("Created Cell handler failed", e);
                    }
                })
                .toArray(CellManagerHandler[]::new));

        return server;
    }

    public CellManagerHandler bind(SocketAddress address) throws IOException {
        CellManagerHandler handler = (CellManagerHandler) balanceHandlers();
        handler.bind(address);

        return handler;
    }

}
