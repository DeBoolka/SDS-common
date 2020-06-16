package ru.mirea.dikanev.nikita.common.server.handler;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import lombok.extern.log4j.Log4j2;
import ru.mirea.dikanev.nikita.common.server.connector.ServerSocketChannelConnector;
import ru.mirea.dikanev.nikita.common.server.connector.SocketChannelConnector;

@Log4j2
public class SlaveRemoteMessageServer extends SimpleMessageHandler {

    private InetSocketAddress masterAddress;
    private InetSocketAddress slaveAddress;

    public SlaveRemoteMessageServer(InetSocketAddress masterAddress, InetSocketAddress slaveAddress) {
        this.masterAddress = masterAddress;
        this.slaveAddress = slaveAddress;
    }

    @Override
    protected void setUpRunning() {
        super.setUpRunning();

        try {
            bind(new SocketChannelConnector(masterAddress));
            bind(new ServerSocketChannelConnector(slaveAddress));
        } catch (Exception e) {
            log.error("Failed to create slave socket", e);
            Thread.currentThread().interrupt();
        }
    }
}
