package ru.mirea.dikanev.nikita.common.server.connector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Optional;

import lombok.extern.log4j.Log4j2;
import ru.mirea.dikanev.nikita.common.server.entity.Message;
import ru.mirea.dikanev.nikita.common.server.entity.client.Client;
import ru.mirea.dikanev.nikita.common.server.handler.MessageHandler;

@Log4j2
public class SocketChannelConnector implements ChannelConnector {

    private SocketChannel channel;
    private InetSocketAddress address = null;
    private Client client;

    private int operation;

    public SocketChannelConnector(SocketChannel channel, int op) throws IOException {
        operation = op;
        this.channel = channel;
        channel.configureBlocking(false);
    }

    public SocketChannelConnector(SocketChannel channel) throws IOException {
        this(channel, SelectionKey.OP_READ);
    }

    public SocketChannelConnector(InetSocketAddress address) {
        this.address = address;
        operation = SelectionKey.OP_CONNECT;
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
        if (channel != null && (channel.isConnectionPending() || channel.isConnected())) {
            return;
        }

        channel = SocketChannel.open();
        channel.connect(address);
        channel.finishConnect();
        System.out.println(String.format("New: %s", channel.getLocalAddress()));
        operation = SelectionKey.OP_CONNECT | SelectionKey.OP_READ;
    }

    @Override
    public ChannelConnector onAccept(Selector selector, MessageHandler handler) {
        throw new UnsupportedOperationException("Accept event for SocketChannel is not supported");
    }

    @Override
    public void onConnect(Selector selector, MessageHandler handler) throws IOException {
        channel.finishConnect();
        System.out.println(String.format("C: %s -- %s", channel.getLocalAddress(), channel.getRemoteAddress()));
    }

    @Override
    public int onRead(Selector selector, MessageHandler handler, ByteBuffer readBuffer) throws IOException {
        System.out.println(String.format("R: %s << %s", channel.getLocalAddress(), channel.getRemoteAddress()));
        return channel.read(readBuffer);
    }

    @Override
    public int onWrite(Selector selector, MessageHandler handler, ByteBuffer writeBuffer) throws IOException {
        System.out.println(String.format("W: %s >> %s", channel.getLocalAddress(), channel.getRemoteAddress()));
        return channel.write(writeBuffer);
    }

    @Override
    public boolean isUnnecessaryMessage(SelectionKey key, Message message) {
        return key.interestOps() == SelectionKey.OP_ACCEPT  || message.getFrom() != null && message.getFrom() == this;
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
        address = inetSocketAddress;
        channel = null;
    }

    @Override
    public boolean isAccepting() {
        return false;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        try {
            return (InetSocketAddress) channel.getLocalAddress();
        } catch (IOException e) {
            log.error(e);
        }
        return null;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        try {
            return (InetSocketAddress) channel.getRemoteAddress();
        } catch (IOException e) {
            log.error(e);
        }
        return null;
    }
}
