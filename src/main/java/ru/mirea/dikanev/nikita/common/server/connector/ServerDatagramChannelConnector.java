package ru.mirea.dikanev.nikita.common.server.connector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Optional;

import lombok.extern.log4j.Log4j2;
import ru.mirea.dikanev.nikita.common.server.entity.Message;
import ru.mirea.dikanev.nikita.common.server.entity.client.Client;
import ru.mirea.dikanev.nikita.common.server.exception.AuthenticationException;
import ru.mirea.dikanev.nikita.common.server.handler.MessageHandler;
import ru.mirea.dikanev.nikita.common.server.service.SimpleClientService;

@Log4j2
public class ServerDatagramChannelConnector implements ChannelConnector {

    private DatagramChannel channel;
    private InetSocketAddress address;
    private Client client;

    private int operation;

    public ServerDatagramChannelConnector(InetSocketAddress address) {
        this.address = address;
        this.operation = SelectionKey.OP_READ;
    }

    @Override
    public SelectableChannel getChannel() {
        return channel;
    }

    @Override
    public int op() {
        return operation;
    }

    @Override
    public void bind(Selector selector, MessageHandler handler) throws IOException {
        if (channel != null) {
            return;
        }

        channel = DatagramChannel.open();
        channel.bind(address);

        Client client = SimpleClientService.getRootClient();
        client.setChannel(this);
        setClient(client);
    }

    @Override
    public void onAccept(Selector selector, MessageHandler handler) throws IOException {

    }

    @Override
    public void onConnect(Selector selector, MessageHandler handler) throws IOException {

    }

    @Override
    public int onRead(Selector selector, MessageHandler handler, ByteBuffer readBuffer) throws IOException {
        SocketAddress remoteAddress = channel.receive(readBuffer);
        ClientDatagramChannelConnector clientChannel = new ClientDatagramChannelConnector(remoteAddress);

        try {
            handler.bind(clientChannel);
        } catch (AuthenticationException ignore) {
        }

        return -2;
    }

    @Override
    public int onWrite(Selector selector, MessageHandler handler, ByteBuffer writeBuffer) throws IOException {
        return -1;
    }

    @Override
    public boolean isUnnecessaryMessage(SelectionKey key, Message message) {
        return true;
    }

    @Override
    public Optional<Client> getClient() {
        return Optional.ofNullable(client);
    }

    @Override
    public void setClient(Client client) {
        this.client = client;
    }

    @Override
    public void reconnect(InetSocketAddress inetSocketAddress) {
        this.address = inetSocketAddress;
        this.operation = SelectionKey.OP_READ;
        channel = null;
    }

    @Override
    public boolean isAccepting() {
        return true;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return address;
    }
}
