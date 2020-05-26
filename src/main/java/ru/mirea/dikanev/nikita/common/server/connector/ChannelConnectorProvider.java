package ru.mirea.dikanev.nikita.common.server.connector;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class ChannelConnectorProvider {

    private static final int UDP_MOD = 1;
    private static final int TCP_MOD = 2;

    private static int mod = TCP_MOD;

    private ChannelConnectorProvider() { }

    public static ChannelConnector openServerConnector(InetSocketAddress address) {
        if (mod == UDP_MOD) {
            return new ServerDatagramChannelConnector(address);
        } else if (mod == TCP_MOD) {
            return new ServerSocketChannelConnector(address);
        }
        throw new UnsupportedOperationException("Unsupported connector provider mod: " + mod);
    }

    public static ChannelConnector openClientConnector(InetSocketAddress serverAddress) {
        if (mod == UDP_MOD) {
            return new ClientDatagramChannelConnector(serverAddress);
        } else if (mod == TCP_MOD) {
            return new SocketChannelConnector(serverAddress);
        }
        throw new UnsupportedOperationException("Unsupported connector provider mod: " + mod);
    }

}
