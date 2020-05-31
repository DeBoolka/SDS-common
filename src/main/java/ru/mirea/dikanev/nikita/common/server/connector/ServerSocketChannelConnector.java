package ru.mirea.dikanev.nikita.common.server.connector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Optional;

import ru.mirea.dikanev.nikita.common.server.entity.Message;
import ru.mirea.dikanev.nikita.common.server.entity.client.Client;
import ru.mirea.dikanev.nikita.common.server.handler.MessageHandler;
import ru.mirea.dikanev.nikita.common.server.service.SimpleClientService;

public class ServerSocketChannelConnector implements ChannelConnector {

    private ServerSocketChannel channel;
    private InetSocketAddress socketAddress;
    private Client client;

    private int operation;

    public ServerSocketChannelConnector(InetSocketAddress address, int op) {
        operation = op;
        socketAddress = address;
    }

    public ServerSocketChannelConnector(InetSocketAddress address) {
        this(address, SelectionKey.OP_ACCEPT);
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

        channel = ServerSocketChannel.open();
        channel.socket().bind(socketAddress);

        Client client = SimpleClientService.getRootClient();
        client.setChannel(this);
        setClient(client);
    }

    @Override
    public ChannelConnector onAccept(Selector selector, MessageHandler handler) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) getChannel();

        SocketChannel newChannel = serverSocketChannel.accept();
        System.out.println(String.format("A: %s -- %s", newChannel.getLocalAddress(), newChannel.getRemoteAddress()));
        return new SocketChannelConnector(newChannel);
    }

    @Override
    public void onConnect(Selector selector, MessageHandler handler) throws IOException {
        throw new UnsupportedOperationException("Connection event for ServerSocketChannel is not supported");
    }

    @Override
    public int onRead(Selector selector, MessageHandler handler, ByteBuffer readBuffer) {
        throw new UnsupportedOperationException("Read event for ServerSocketChannel is not supported");
    }

    @Override
    public int onWrite(Selector selector, MessageHandler handler, ByteBuffer writeBuffer) {
        throw new UnsupportedOperationException("Write event for ServerSocketChannel is not supported");
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
        channel = null;
        socketAddress = inetSocketAddress;
    }

    @Override
    public boolean isAccepting() {
        return true;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return socketAddress;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return null;
    }

}
