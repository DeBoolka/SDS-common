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
import ru.mirea.dikanev.nikita.common.server.processor.SectorMessageProcessor;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.AddressCodec;

@Log4j2
public class SectorServer extends CellServer {

    private InetSocketAddress cellAddr;
    private InetSocketAddress localServerAddr;

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

        log.info("Connecting to Cell: {}", cell);
        server.bindClient(cell);
        log.info("Creating a server socket: {}", localAddress);
        server.bindServer(localAddress);

        server.cellAddr = cell;
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
        super.start();
        //TODO: bullshit. Rewrite condition
        while (!((SimpleMessageHandler) handlers.get(0)).isRunning()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignore) {
            }
        }

        send(Message.create(null,
                Codes.SET_ADDRESS_ACTION,
                AddressCodec.newAddressPack(localServerAddr.getHostName(), localServerAddr.getPort())));

    }

    public CellHandler bindClient(InetSocketAddress address) throws IOException, AuthenticationException {
        CellHandler handler = (CellHandler) balanceHandlers();
        handler.bindClient(address);

        return handler;
    }

}
