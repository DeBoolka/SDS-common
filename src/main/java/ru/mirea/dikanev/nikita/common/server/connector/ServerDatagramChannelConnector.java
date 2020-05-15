package ru.mirea.dikanev.nikita.common.server.connector;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import lombok.extern.log4j.Log4j2;
import ru.mirea.dikanev.nikita.common.entity.Message;
import ru.mirea.dikanev.nikita.common.server.handler.MessageHandler;

@Log4j2
public class ServerDatagramChannelConnector implements ChannelConnector {

    private DatagramChannel channel;
    private SocketAddress address;

    private int operation;

    public ServerDatagramChannelConnector(SocketAddress address) {
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
        handler.bind(clientChannel);

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
}
