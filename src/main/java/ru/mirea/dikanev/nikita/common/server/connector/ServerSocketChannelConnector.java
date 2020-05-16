package ru.mirea.dikanev.nikita.common.server.connector;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import ru.mirea.dikanev.nikita.common.server.entity.Message;
import ru.mirea.dikanev.nikita.common.server.handler.MessageHandler;

public class ServerSocketChannelConnector implements ChannelConnector {

    private ServerSocketChannel channel;
    private SocketAddress socketAddress;

    private int operation;

    public ServerSocketChannelConnector(SocketAddress address, int op) throws IOException {
        operation = op;
        socketAddress = address;
    }

    public ServerSocketChannelConnector(SocketAddress address) throws IOException {
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
    }

    @Override
    public void onAccept(Selector selector, MessageHandler handler) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) getChannel();

        SocketChannel newChannel = serverSocketChannel.accept();
        System.out.println(String.format("A: %s -- %s", newChannel.getLocalAddress(), newChannel.getRemoteAddress()));
        handler.bind(new SocketChannelConnector(newChannel));
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

}
