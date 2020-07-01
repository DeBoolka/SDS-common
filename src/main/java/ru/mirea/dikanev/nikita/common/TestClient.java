package ru.mirea.dikanev.nikita.common;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import lombok.extern.log4j.Log4j2;
import ru.mirea.dikanev.nikita.common.server.processor.Codes;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.AddressCodec;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.LoginCodec;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.MessageCodec;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.MetricCodec;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.PositionCodec;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.ReconnectCodec;
import ru.mirea.dikanev.nikita.common.server.protocol.pack.MessagePackage;
import ru.mirea.dikanev.nikita.common.server.protocol.pack.MetricPackage;
import ru.mirea.dikanev.nikita.common.server.protocol.pack.PositionPackage;
import ru.mirea.dikanev.nikita.common.server.protocol.pack.ReconnectPackage;
import ru.mirea.dikanev.nikita.common.server.receiver.ByteBuilder;

@Log4j2
public class TestClient {

    public static final String DEFAULT_HOST = "192.168.2.35";
    public static final int PORT = 12000;
    public static final int BUFFER_SIZE = 8192;

    private Selector selector;
    private SocketChannel channel;
    private ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
    private ByteBuilder byteBuilder = null;

    private MetricCodec metricCodec = new MetricCodec();

    private int maxRequestAtSecond = 0;
    private int maxRequestStep = 200;
    private int requestCounter = 0;
    private long requestTime = 0;

    volatile BlockingQueue<byte[]> queue = new ArrayBlockingQueue<>(10);

    int id = -1;

    private Scanner scanner;

    public void init() throws Exception {

        scanner = new Scanner(System.in);
        System.out.println("host: ");
        String host = scanner.nextLine();
        if (host.isBlank() || host.equals("0") || host.equals("-")) {
            System.out.println("[WARNING] Default host ");
            host = DEFAULT_HOST;
        }

        System.out.println("port: ");
        String port = scanner.nextLine();
        if (port.isBlank() || port.equals("0") || port.equals("-")) {
            //log.warn("[WARNING] Default port");
            port = String.valueOf(PORT);
        }
        for (char ch : port.toCharArray()) {
            if (ch < '0' || ch > '9') {
                System.out.println("[WARNING] Port is not integer");
                System.out.println("[WARNING] Default port");
                port = String.valueOf(PORT);
                break;
            }
        }

        // Слушаем ввод данных с консоли
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            while (true) {
                try {
                    ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
                    buffer.putInt(Integer.BYTES * 4);
                    queue.put(buffer.array());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Будим селектор
                SelectionKey key = channel.keyFor(selector);
                key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                selector.wakeup();
            }
        });
        t.start();

        selector = Selector.open();
        channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_CONNECT);

        System.out.println(String.format("Connect to server [%s:%s]", host, port));
        channel.connect(new InetSocketAddress(host, Integer.parseInt(port)));

        while (true) {
            try {
                int num = selector.select();

                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey sKey = keyIterator.next();
                    keyIterator.remove();

                    if (sKey.isConnectable()) {
                        //log.info("[connectable] {}", sKey.hashCode());

                        channel.finishConnect();

                        // теперь в канал можно писать
                        sKey.interestOps(SelectionKey.OP_WRITE);
                    } else if (sKey.isReadable()) {
                        buffer.clear();
                        int numRead = channel.read(buffer);
                        if (numRead < 0) {
                            break;
                        }

                        buffer.rewind();
                        byte[] gottenData = buffer.array();
                        if (byteBuilder != null) {
                            int missedData = byteBuilder.len() - byteBuilder.getData()[0].length;
                            byte[] arr = new byte[missedData];
                            System.arraycopy(gottenData, 0, arr, 0, missedData);
                            byteBuilder.put(arr);
                            buffer.position(missedData + buffer.position());

                            MetricPackage metricPackage = metricCodec.decode(ByteBuffer.wrap(byteBuilder.build()));
                            log.info("{}", metricPackage);
                            byteBuilder = null;
                        }

                        while (buffer.position() < numRead) {
                            int len = buffer.getInt();
                            if (len + buffer.position() >= numRead) {
                                int missedData = len + buffer.position() - numRead;
                                byte[] messageCopy = new byte[len - missedData];
                                System.arraycopy(gottenData, buffer.position(), messageCopy, 0, len - missedData);
                                byteBuilder = new ByteBuilder(2);
                                byteBuilder.put(messageCopy);
                                byteBuilder.setLen(len);
                                break;
                            }

                            byte[] messageCopy = new byte[len];

                            System.arraycopy(gottenData, buffer.position(), messageCopy, 0, len);
                            buffer.position(len + buffer.position());

                            MetricPackage metricPackage = metricCodec.decode(ByteBuffer.wrap(messageCopy));
                            log.info("{}", metricPackage);
                        }

                    } else if (sKey.isWritable()) {
                        if (queue.size() == 0) {
    //                        sKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                            continue;
                        }

                        long curTime = System.currentTimeMillis();
                        if (curTime > requestTime + 1000) {
                            System.out.println(requestCounter);
                            requestTime = curTime;
                            requestCounter = 0;
                            maxRequestAtSecond += maxRequestStep;
                        }

                        if (requestCounter > maxRequestAtSecond) {
                            continue;
                        }
                        requestCounter++;

                        byte[] line = queue.poll();
                        if (line == null) {
                            break;
                        }

                        channel.write(ByteBuffer.wrap(line));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args)  {
        try {
            TestClient client = new TestClient();
            client.init();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("Error");
        }
    }

}
