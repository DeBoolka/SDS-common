package ru.mirea.dikanev.nikita.common;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class Client {

    public static final int PORT = 18000;

    private Selector selector;
    private SocketChannel channel;
    private ByteBuffer buffer = ByteBuffer.allocate(16);

    BlockingQueue<String> queue = new ArrayBlockingQueue<>(2);

    // TODO: Нужно создать блокирующую очередь, в которую складывать данные для обмена между потоками

    public void init() throws Exception {

        // Слушаем ввод данных с консоли
        Thread t = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String line = scanner.nextLine();
                if ("q".equals(line)) {
                    log.info("Exit!");
                    System.exit(0);
                }

                // TODO: здесь нужно сложить прочитанные данные в очередь

                try {
                    queue.put(line);
                } catch (InterruptedException e) {
                    e.printStackTrace();

                }

                // Будим селектор
                SelectionKey key = channel.keyFor(selector);
                log.info("wake up: {}", key.hashCode());
                key.interestOps(SelectionKey.OP_WRITE);
                selector.wakeup();
            }
        });
        t.start();

        selector = Selector.open();
        channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_CONNECT);

        channel.connect(new InetSocketAddress("127.0.0.1", PORT));

        while (true) {
            int num = selector.select();

            Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
            while (keyIterator.hasNext()) {
                SelectionKey sKey = keyIterator.next();
                keyIterator.remove();

                if (sKey.isConnectable()) {
                    log.info("[connectable] {}", sKey.hashCode());

                    channel.finishConnect();

                    // теперь в канал можно писать
                    sKey.interestOps(SelectionKey.OP_WRITE);
                } else if (sKey.isReadable()) {
                    log.info("[readable]");

                    buffer.clear();
                    int numRead = channel.read(buffer);
                    if (numRead < 0) {
                        break;
                    }
                    log.info("From server: {}", new String(Arrays.copyOf(buffer.array(), numRead)));

                } else if (sKey.isWritable()) {
                    log.info("[writable]");

                    //TODO: здесь нужно вытащить данные из очереди и отдать их на сервер

                    //byte[] userInput = ...;
                    //channel.write(ByteBuffer.wrap(userInput));

                    while (!queue.isEmpty()) {
                        String line = queue.poll();
                        if (line == null) {
                            line = "Default message";
                        }

                        if (line != null) {
                            channel.write(ByteBuffer.wrap(line.getBytes()));
                        }
                    }

                    // Ждем записи в канал
                    sKey.interestOps(SelectionKey.OP_READ);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Client client = new Client();
        client.init();
    }

}
