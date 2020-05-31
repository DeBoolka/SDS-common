package ru.mirea.dikanev.nikita.common.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.stream.IntStream;

import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;
import ru.mirea.dikanev.nikita.common.server.exception.AuthenticationException;
import ru.mirea.dikanev.nikita.common.server.exception.HandlerInternalException;
import ru.mirea.dikanev.nikita.common.server.handler.CellManagerHandler;
import ru.mirea.dikanev.nikita.common.server.handler.MessageHandler;
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
                    } catch (IOException | AuthenticationException e) {
                        throw new HandlerInternalException("Created Cell Manager handler failed", e);
                    }
                })
                .toArray(CellManagerHandler[]::new));

        return server;
    }

    public CellManagerHandler bind(InetSocketAddress address) throws IOException, AuthenticationException {
        CellManagerHandler handler = (CellManagerHandler) balanceHandlers();
        handler.bind(address);

        return handler;
    }

    public CellManagerHandler bindCellAccepter(ChannelConnector connector) throws IOException, AuthenticationException {
        CellManagerHandler handler = (CellManagerHandler) balanceHandlers();
        handler.cellBind(connector);

        return handler;
    }

    @Override
    public CellManagerServer handlers(MessageHandler... handlers) {
        super.handlers = Arrays.asList(handlers);
        return this;
    }
}
