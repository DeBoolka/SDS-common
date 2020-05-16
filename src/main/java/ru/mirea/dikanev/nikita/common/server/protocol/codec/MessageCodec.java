package ru.mirea.dikanev.nikita.common.server.protocol.codec;

import java.nio.ByteBuffer;

import ru.mirea.dikanev.nikita.common.server.protocol.pack.MessagePackage;

public class MessageCodec extends Codec<MessagePackage> {

    private static MessageCodec messageCodec = new MessageCodec();

    @Override
    public ByteBuffer encode(ByteBuffer buffer, MessagePackage messagePackage) {
        buffer.putShort(messagePackage.space);
        buffer.putInt(messagePackage.hop);
        putByteString(buffer, messagePackage.data);

        return buffer;
    }

    @Override
    public MessagePackage decode(ByteBuffer buffer) {
        short space = buffer.getShort();
        int hop = buffer.getInt();
        byte[] data = readByteString(buffer);

        return new MessagePackage(space, hop, data);
    }

    public static int size(MessagePackage messagePackage) {
        return Short.BYTES + Integer.BYTES + stringBytes(messagePackage.data);
    }

    public static byte[] newMessagePack(String text) {
        MessagePackage messagePackage = new MessagePackage((short) 1, 0, text.getBytes());
        ByteBuffer buffer = ByteBuffer.allocate(size(messagePackage));

        return messageCodec.encode(buffer, messagePackage).array();
    }
}
