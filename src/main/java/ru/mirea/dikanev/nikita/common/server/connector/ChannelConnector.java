package ru.mirea.dikanev.nikita.common.server.connector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Optional;

import ru.mirea.dikanev.nikita.common.server.entity.client.Client;
import ru.mirea.dikanev.nikita.common.server.entity.Message;
import ru.mirea.dikanev.nikita.common.server.handler.MessageHandler;

public interface ChannelConnector {

    SelectableChannel getChannel();

    int op();

    void bind(Selector selector, MessageHandler handler) throws IOException;

    void onAccept(Selector selector, MessageHandler handler) throws IOException;

    void onConnect(Selector selector, MessageHandler handler) throws IOException;

    /**
     * @return The number of bytes read, possibly zero, -1 if the channel has reached end-of-stream, or -2 if the
     *         channel is not yet ready to reading
     */
    int onRead(Selector selector, MessageHandler handler, ByteBuffer readBuffer) throws IOException;

    /**
     * @return The number of bytes written is possibly zero. If the channel is not ready, -1 is returned
     */
    int onWrite(Selector selector, MessageHandler handler, ByteBuffer writeBuffer) throws IOException;

    boolean isUnnecessaryMessage(SelectionKey key, Message message);

    Optional<Client> getClient();

    void setClient(Client client);

    void reconnect(InetSocketAddress inetSocketAddress);
}
