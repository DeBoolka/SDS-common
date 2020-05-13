package ru.mirea.dikanev.nikita.common.server.connector;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.Selector;

import ru.mirea.dikanev.nikita.common.server.handler.MessageHandler;

public interface ChannelConnector {

    SelectableChannel getChannel();

    int op();

    void bind(Selector selector, MessageHandler handler) throws IOException;

    void onAccept(Selector selector, MessageHandler handler) throws IOException;

    void onConnect(Selector selector, MessageHandler handler) throws IOException;

    int onRead(Selector selector, MessageHandler handler, ByteBuffer readBuffer) throws IOException;

    int onWrite(Selector selector, MessageHandler handler, ByteBuffer writeBuffer) throws IOException;
}
