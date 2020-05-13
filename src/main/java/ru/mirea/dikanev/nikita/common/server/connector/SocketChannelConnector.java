package ru.mirea.dikanev.nikita.common.server.connector;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import ru.mirea.dikanev.nikita.common.server.handler.MessageHandler;

public class SocketChannelConnector implements ChannelConnector {

    private SocketChannel channel;
    private SocketAddress address = null;

    private int operation;

    public SocketChannelConnector(SocketChannel channel, int op) throws IOException {
        operation = op;
        this.channel = channel;
        channel.configureBlocking(false);
    }

    public SocketChannelConnector(SocketChannel channel) throws IOException {
        this(channel, SelectionKey.OP_READ);
    }

    public SocketChannelConnector(SocketAddress address) {
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
        operation = SelectionKey.OP_CONNECT;
    }

    @Override
    public void onAccept(Selector selector, MessageHandler handler) {
        throw new UnsupportedOperationException("Accept event for SocketChannel is not supported");
    }

    @Override
    public void onConnect(Selector selector, MessageHandler handler) throws IOException {
        channel.finishConnect();
    }

    @Override
    public int onRead(Selector selector, MessageHandler handler, ByteBuffer readBuffer) throws IOException {
        return channel.read(readBuffer);
    }

    @Override
    public int onWrite(Selector selector, MessageHandler handler, ByteBuffer writeBuffer) throws IOException {
        return channel.write(writeBuffer);
    }
}
