package ru.mirea.dikanev.nikita.common.server.connector;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import lombok.extern.log4j.Log4j2;
import ru.mirea.dikanev.nikita.common.server.entity.ChangeOpsRequest;
import ru.mirea.dikanev.nikita.common.server.entity.Message;
import ru.mirea.dikanev.nikita.common.server.handler.MessageHandler;

@Log4j2
public  class ClientDatagramChannelConnector implements ChannelConnector {

    public static final short READY = 0;
    public static final short FINISHING_CONNECTION = 1;

    private short status = READY;
    private boolean isConfirming = false;

    private DatagramChannel channel;
    private SocketAddress remoteAddress;

    private int operation;

    public ClientDatagramChannelConnector(SocketAddress remoteAddress) {
        this(remoteAddress, false);
    }

    public ClientDatagramChannelConnector(SocketAddress remoteAddress, boolean isConfirming) {
        this.remoteAddress = remoteAddress;
        this.isConfirming = isConfirming;
        operation = ChangeOpsRequest.OP_READ_WRITE;
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
        channel.bind(null);
        System.out.println(String.format("New: %s", channel.getLocalAddress()));
        status = isConfirming ? FINISHING_CONNECTION : status;
        if (!isConfirming) {
            channel.connect(remoteAddress);
        }

        Message msg = new Message("I want to become friends".getBytes());
        handler.sendMessage(getChannel(), msg);
    }

    @Override
    public void onAccept(Selector selector, MessageHandler handler) throws IOException {

    }

    @Override
    public void onConnect(Selector selector, MessageHandler handler) throws IOException {

    }

    @Override
    public int onRead(Selector selector, MessageHandler handler, ByteBuffer readBuffer) throws IOException {
        if (status != READY) {
            try {
                return connect(selector, handler, readBuffer);
            } catch (Exception e) {
                log.error("Failed to connect: ", e);
                return -1;
            }
        }

        System.out.println(String.format("R: %s << %s", channel.getLocalAddress(), channel.getRemoteAddress()));
        return channel.read(readBuffer);
    }

    @Override
    public int onWrite(Selector selector, MessageHandler handler, ByteBuffer writeBuffer) throws IOException {
        if (status == FINISHING_CONNECTION) {
            System.out.println(String.format("S: %s >> %s", channel.getLocalAddress(), remoteAddress));
            return channel.send(writeBuffer, remoteAddress);
        }

        System.out.println(String.format("W: %s >> %s", channel.getLocalAddress(), channel.getRemoteAddress()));
        return channel.write(writeBuffer);
    }

    @Override
    public boolean isUnnecessaryMessage(SelectionKey key, Message message) {
        return key.interestOps() == SelectionKey.OP_ACCEPT || message.getFrom() != null && message.getFrom() == this;
    }

    private int connect(Selector selector, MessageHandler handler, ByteBuffer readBuffer) throws IOException {
        if (status == FINISHING_CONNECTION) {
            remoteAddress = channel.receive(readBuffer);
            channel.connect(remoteAddress);
            status = READY;
            log.info("Connect: {} >> {}", channel.getLocalAddress(), channel.getRemoteAddress());
        }

        return -2;
    }

}
