package ru.mirea.dikanev.nikita.common.server.processor;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.mirea.dikanev.nikita.common.server.entity.Message;
import ru.mirea.dikanev.nikita.common.server.MessageServer;
import ru.mirea.dikanev.nikita.common.server.handler.MessageHandler;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.MessageCodec;
import ru.mirea.dikanev.nikita.common.server.protocol.pack.MessagePackage;

public class SimpleMessageProcessor implements MessageProcessor {

    private MessageServer server;

    private ExecutorService pendingMessage;

    private MessageCodec messageCodec = new MessageCodec();

    public SimpleMessageProcessor(MessageServer server, int processCount) {
        this(processCount);
        this.server = server;

    }

    public SimpleMessageProcessor(int processCount) {
        this.pendingMessage = Executors.newFixedThreadPool(processCount);
    }

    public void setServer(MessageServer server) {
        this.server = server;
    }

    @Override
    public void process(MessageHandler handler, Message message) {
        pendingMessage.submit(() -> {
            MessagePackage pack = messageCodec.decode(message.getData());
            System.out.println(String.format("Receive -> %d: %s", pack.hop, new String(pack.data)));

            pack.hop++;
            ByteBuffer writeBuffer = ByteBuffer.allocate(MessageCodec.size(pack));
            messageCodec.encode(writeBuffer, pack);

            server.getMessageHandlers()
                    .forEach(h -> h.sendMessage(new Message(message.getFrom(), writeBuffer)));
        });
    }

    @Override
    public void clear(MessageHandler handler) { }

}
