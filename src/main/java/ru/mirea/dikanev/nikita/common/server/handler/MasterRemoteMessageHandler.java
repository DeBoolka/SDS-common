package ru.mirea.dikanev.nikita.common.server.handler;

import java.net.SocketAddress;

import lombok.extern.log4j.Log4j2;
import ru.mirea.dikanev.nikita.common.server.connector.ServerSocketChannelConnector;

@Log4j2
public class MasterRemoteMessageHandler extends SimpleMessageHandler {

    private SocketAddress address;

    public MasterRemoteMessageHandler(SocketAddress address) {
        this.address = address;
    }

    @Override
    protected void setUpRunning() {
        super.setUpRunning();

        try {
            bind(new ServerSocketChannelConnector(address));
        } catch (Exception e) {
            log.error("Failed to create master socket", e);
            Thread.currentThread().interrupt();
        }
    }
}
