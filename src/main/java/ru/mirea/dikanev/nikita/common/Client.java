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
import ru.mirea.dikanev.nikita.common.server.processor.Codes;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.LoginCodec;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.MessageCodec;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.PositionCodec;
import ru.mirea.dikanev.nikita.common.server.protocol.pack.MessagePackage;

@Log4j2
public class Client {

    public static final String MESSAGE_ACTION = "m";
    public static final String LOGIN_ACTION = "l";
    public static final String POSITION_ACTION = "p";

    public static final String DEFAULT_HOST = "127.0.0.1";
    public static final int PORT = 18000;

    private Selector selector;
    private SocketChannel channel;
    private ByteBuffer buffer = ByteBuffer.allocate(1000);

    BlockingQueue<byte[]> queue = new ArrayBlockingQueue<>(2);

    private Scanner scanner;

    // TODO: Нужно создать блокирующую очередь, в которую складывать данные для обмена между потоками

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
            System.out.println("[WARNING] Default port");
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
            while (true) {
                String line = scanner.nextLine();
                if ("q".equals(line)) {
                    log.info("Exit!");
                    System.exit(0);
                }

                // TODO: здесь нужно сложить прочитанные данные в очередь

                try {
                    switch (line.toLowerCase()) {
                        case MESSAGE_ACTION:
                            queue.put(toMessage());
                            break;
                        case LOGIN_ACTION:
                            queue.put(toLogin());
                            break;
                        case POSITION_ACTION:
                            queue.put(toPosition());
                            break;
                    }
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

        System.out.println(String.format("Connecting to server [%s:%s]", host, port));
        channel.connect(new InetSocketAddress(host, Integer.parseInt(port)));

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
                    log.info("New message from server");
                    parseMessage(Arrays.copyOf(buffer.array(), numRead));

                } else if (sKey.isWritable()) {
                    log.info("[writable]");

                    //TODO: здесь нужно вытащить данные из очереди и отдать их на сервер

                    //byte[] userInput = ...;
                    //channel.write(ByteBuffer.wrap(userInput));

                    while (!queue.isEmpty()) {
                        byte[] line = queue.poll();
                        if (line == null) {
                            break;
                        }

                        channel.write(ByteBuffer.wrap(line));
                    }

                    // Ждем записи в канал
                    sKey.interestOps(SelectionKey.OP_READ);
                }
            }
        }
    }

    private void parseMessage(byte[] copyOf) {
        ByteBuffer buffer = ByteBuffer.wrap(copyOf);
        int action = buffer.getInt();
        switch (action) {
            case Codes.COMMUNICATION_ACTION:
                System.out.println(new MessageCodec().decode(buffer));
                return;
            case Codes.LOGIN_ACTION:
                System.out.println(new LoginCodec().decode(buffer));
                return;
            case Codes.POSITION_ACTION:
                System.out.println(new PositionCodec().decode(buffer));
                return;
        }
        System.out.println("Action code is unknown: " + action);
    }

    private byte[] toMessage() {
        System.out.println("Space (c/w):");
        String line = scanner.nextLine();
        System.out.println("Write text:");
        if (line.toLowerCase().equals("c")) {
            return addAction(Codes.COMMUNICATION_ACTION,
                    MessageCodec.newByteMessagePack(MessagePackage.CELL_SPACE, scanner.nextLine()));
        }
        return addAction(Codes.COMMUNICATION_ACTION, MessageCodec.newByteMessagePack(scanner.nextLine()));
    }

    private byte[] toLogin() {
        System.out.println("login:");
        String login = scanner.nextLine();
        System.out.println("password:");
        String password = scanner.nextLine();
        if (login.isBlank()) {
            login = "root";
        }
        if (password.isBlank()) {
            password = "root";
        }

        return addAction(Codes.LOGIN_ACTION, LoginCodec.newLoginPack(login, password));
    }

    private byte[] toPosition() {
        System.out.println("direction (a/w/d/s):");
        String direction = scanner.nextLine();
        if (direction.isBlank()) {
            direction = "w";
        }

        switch (direction.toLowerCase().charAt(0)) {
            case 'a':
                return addAction(Codes.POSITION_ACTION, PositionCodec.newPositionPack(-1, -1, 0));
            case 's':
                return addAction(Codes.POSITION_ACTION, PositionCodec.newPositionPack(-1, 0, -1));
            case 'd':
                return addAction(Codes.POSITION_ACTION, PositionCodec.newPositionPack(-1, 1, 0));
            case 'w':
                return addAction(Codes.POSITION_ACTION, PositionCodec.newPositionPack(-1, 0, 1));
        }
        return addAction(Codes.POSITION_ACTION, PositionCodec.newPositionPack(-1, 0, 0));
    }

    private byte[] addAction(int action, byte[] buffer) {
        byte[] newBuff = new byte[Integer.BYTES + buffer.length];
        ByteBuffer buff = ByteBuffer.wrap(newBuff);
        buff.putInt(action);
        buff.put(buffer);
        return buff.array();
    }

    public static void main(String[] args) throws Exception {
        Client client = new Client();
        client.init();
    }

}
