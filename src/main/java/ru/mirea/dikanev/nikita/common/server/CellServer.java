package ru.mirea.dikanev.nikita.common.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.stream.IntStream;

import lombok.extern.log4j.Log4j2;
import ru.mirea.dikanev.nikita.common.math.Rectangle;
import ru.mirea.dikanev.nikita.common.server.entity.Message;
import ru.mirea.dikanev.nikita.common.server.exception.AuthenticationException;
import ru.mirea.dikanev.nikita.common.server.exception.HandlerInternalException;
import ru.mirea.dikanev.nikita.common.server.handler.CellHandler;
import ru.mirea.dikanev.nikita.common.server.handler.SimpleMessageHandler;
import ru.mirea.dikanev.nikita.common.server.processor.CellMessageProcessor;
import ru.mirea.dikanev.nikita.common.server.processor.Codes;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.AddressCodec;

@Log4j2
public class CellServer extends SimpleMessageServer {

    public InetSocketAddress remoteAddr;
    public InetSocketAddress localServerAddr;

    protected CellServer() {
    }

    public static CellServer create(int nMessageProcessors, int nHandlers, InetSocketAddress cellManager,
            InetSocketAddress localAddress) throws IOException, AuthenticationException {

        CellServer server = new CellServer();
        CellMessageProcessor processor = new CellMessageProcessor(server, nMessageProcessors);

        server.processor(processor);
        server.handlers(IntStream.range(0, nHandlers).mapToObj(i -> {
            try {
                return CellHandler.create(processor);
            } catch (IOException e) {
                throw new HandlerInternalException("Created Cell handler failed", e);
            }
        }).toArray(CellHandler[]::new));

        log.info("Connect to Cell Manager: {}", cellManager);
        server.bindClient(cellManager);
        log.info("Create a server socket: {}", localAddress);
        server.bindServer(localAddress);

        server.remoteAddr = cellManager;
        server.localServerAddr = localAddress;

        return server;
    }

    public CellHandler bindServer(InetSocketAddress address) throws IOException, AuthenticationException {
        CellHandler handler = (CellHandler) balanceHandlers();
        handler.bindServer(address);

        return handler;
    }

    @Override
    public void start() {
        log.info("A local address is sent to the Cell Manager");
        ((SimpleMessageHandler) handlers.get(0)).addFinishCallback(handler -> handler.sendMessage(Message.create(null,
                Codes.SET_ADDRESS_ACTION,
                AddressCodec.newAddressPack(localServerAddr.getHostName(), localServerAddr.getPort()))));

        super.start();
    }

    public CellHandler bindClient(InetSocketAddress address) throws IOException, AuthenticationException {
        CellHandler handler = (CellHandler) balanceHandlers();
        handler.bindClient(address);

        return handler;
    }

}
