package ru.mirea.dikanev.nikita.common.server.processor;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.extern.log4j.Log4j2;
import ru.mirea.dikanev.nikita.common.math.Rectangle;
import ru.mirea.dikanev.nikita.common.server.CellManagerServer;
import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;
import ru.mirea.dikanev.nikita.common.server.entity.Message;
import ru.mirea.dikanev.nikita.common.server.handler.CellManagerHandler;
import ru.mirea.dikanev.nikita.common.server.handler.MessageHandler;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.AddressCodec;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.MessageCodec;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.PositionCodec;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.ReconnectCodec;
import ru.mirea.dikanev.nikita.common.server.protocol.pack.AddressPackage;
import ru.mirea.dikanev.nikita.common.server.protocol.pack.MessagePackage;
import ru.mirea.dikanev.nikita.common.server.protocol.pack.PositionPackage;
import ru.mirea.dikanev.nikita.common.server.protocol.pack.ReconnectPackage;

@Log4j2
public class TestMessageProcessor implements MessageProcessor {

    private ExecutorService messageTasks;

    public TestMessageProcessor(int nThreads) {
        this.messageTasks = Executors.newFixedThreadPool(nThreads);
    }

    @Override
    public void process(MessageHandler handler, Message message) {
        message.getData().putInt((int) (System.currentTimeMillis() % 10000000));
        messageTasks.submit(() -> {
            message.getData().putInt((int) (System.currentTimeMillis() % 10000000));
            handler.sendMessage(message.getFrom().getChannel(), message);
        });
    }

    @Override
    public void clear(MessageHandler handler) {
        messageTasks.shutdown();
    }
}
