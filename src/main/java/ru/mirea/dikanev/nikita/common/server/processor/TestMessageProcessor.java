package ru.mirea.dikanev.nikita.common.server.processor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.extern.log4j.Log4j2;
import ru.mirea.dikanev.nikita.common.server.entity.Message;
import ru.mirea.dikanev.nikita.common.server.handler.MessageHandler;

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
            long time = System.currentTimeMillis() % 10000000;
            message.getData().putInt((int) (time));

            if (TestMessageProcessor.findSimpleNum(1000)) {
                return;
            }

//            System.out.println("SEND");
            handler.sendMessage(message.getFrom().getChannel(), message);
        });
    }

    public static boolean findSimpleNum(int num) {
        double sqrt = Math.sqrt(num);
        for (double i = 1; i < sqrt * sqrt; i++) {
            double prime = Math.sqrt(i)*sqrt;
            for (int j = 0; j < 10; j++) {
                i = i + (prime * i % 10 - prime / 2 * 2 * i % 10);
            }
        }
        return false;
    }

    @Override
    public void clear(MessageHandler handler) {
        messageTasks.shutdown();
    }
}
