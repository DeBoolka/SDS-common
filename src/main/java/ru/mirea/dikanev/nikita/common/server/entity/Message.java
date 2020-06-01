package ru.mirea.dikanev.nikita.common.server.entity;

import java.nio.ByteBuffer;
import java.util.stream.StreamSupport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;
import ru.mirea.dikanev.nikita.common.server.processor.Codes;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.MessageCodec;
import ru.mirea.dikanev.nikita.common.server.protocol.pack.MessagePackage;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Message {

    public static final int ACTION_BYTES = Integer.BYTES;
    public static final int INACTION = -1;

    private ChannelConnector from;
    private int action = INACTION;
    private ByteBuffer data;

    public Message(ChannelConnector from, int action) {
        this.from = from;
        this.action = action;
    }

    private Message(ChannelConnector from, int action,  byte[] data) {
        this(from, action);

        this.data = ByteBuffer.allocate(ACTION_BYTES + data.length);
        this.data.putInt(action);
        this.data.put(data);
    }

    public static Message create(ChannelConnector from, int action, byte[] data) {
        return new Message(from, action, data);
    }

    public static Message create(ChannelConnector from, int action, byte[] data1, byte[]... dataArray) {
        int lenDataArray = 0;
        for (byte[] dataN : dataArray){
            lenDataArray += dataN.length;
        }

        byte[] data = new byte[data1.length + lenDataArray];
        System.arraycopy(data1, 0, data, 0, data1.length);

        int index = data1.length;
        for (byte[] dataN : dataArray) {
            System.arraycopy(dataN, 0, data, index, dataN.length);
            index += dataN.length;
        }

        return new Message(from, action, data);
    }


    public static Message create(ChannelConnector from, int action, ByteBuffer data) {
        return new Message(from, action, data);
    }

    public static Message createWithAction(ChannelConnector from, ByteBuffer data) {
        int action = data.getInt();

        Message message = new Message(from, action);
        message.setData(data);
        return message;
    }

    public static Message send(ChannelConnector from, MessagePackage pack) {
        int action = Codes.COMMUNICATION_ACTION;
        ByteBuffer data = ByteBuffer.allocate(ACTION_BYTES + MessageCodec.size(pack));

        data.putInt(action);
        MessageCodec.inst().encode(data, pack);
        return create(from, action, data);
    }

    public static Message send(ChannelConnector from, String text) {
        return send(from, MessageCodec.newMessagePack(MessagePackage.WORLD, text));
    }

    public ByteBuffer payload() {
        data.rewind();
        data.position(ACTION_BYTES);
        return data;
    }

    public boolean isAction(int action) {
        return this.action == action;
    }

    private static byte[] copy(byte[] array, int size) {
        byte[] messageCopy = new byte[size];
        System.arraycopy(array, 0, messageCopy, 0, size);
        return messageCopy;
    }
}
