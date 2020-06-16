package ru.mirea.dikanev.nikita.common.server.processor;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.extern.log4j.Log4j2;
import ru.mirea.dikanev.nikita.common.server.entity.Message;
import ru.mirea.dikanev.nikita.common.server.MessageServer;
import ru.mirea.dikanev.nikita.common.server.handler.MessageHandler;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.MessageCodec;
import ru.mirea.dikanev.nikita.common.server.protocol.pack.MessagePackage;

@Log4j2
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
            try {
                if (message.isAction(Codes.LOGIN_ACTION)) {
                    server.getMessageHandlers().forEach(h -> h.sendMessage(message));
                }

                MessagePackage pack = messageCodec.decode(message.payload());
                System.out.println(String.format("Simple process, hop %d: %s", pack.hop, new String(pack.data)));
                pack.hop++;

                server.getMessageHandlers().forEach(h -> h.sendMessage(Message.send(message.getFrom(), pack)));
            } catch (Exception e) {
                //log.error("Process has failed", e);
            }
        });
    }

    @Override
    public void clear(MessageHandler handler) { }

}
