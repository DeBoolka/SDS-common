package ru.mirea.dikanev.nikita.common.server.protocol.codec;

import java.nio.ByteBuffer;

import ru.mirea.dikanev.nikita.common.server.protocol.pack.MessagePackage;

public class MessageCodec extends Codec<MessagePackage> {

    private static MessageCodec messageCodec = new MessageCodec();

    public static MessageCodec inst() {
        return messageCodec;
    }

    @Override
    public ByteBuffer encode(ByteBuffer buffer, MessagePackage messagePackage) {
        buffer.putShort(messagePackage.space);
        buffer.putInt(messagePackage.hop);
        buffer.putInt(messagePackage.receiverId);
        putByteString(buffer, messagePackage.data);

        return buffer;
    }

    @Override
    public MessagePackage decode(ByteBuffer buffer) {
        short space = buffer.getShort();
        int hop = buffer.getInt();
        int receiverId = buffer.getInt();
        byte[] data = readByteString(buffer);

        return new MessagePackage(space, hop, receiverId, data);
    }

    public static int size(MessagePackage messagePackage) {
        return Short.BYTES + (Integer.BYTES * 2) + stringBytes(messagePackage.data);
    }

    public static byte[] newByteMessagePack(short space, String text){
        return newByteMessagePack(space, -1, text);
    }

    public static byte[] newByteMessagePack(short space, int receiverId, String text) {
        MessagePackage messagePackage = newMessagePack(space, receiverId, text);
        ByteBuffer buffer = ByteBuffer.allocate(size(messagePackage));

        return messageCodec.encode(buffer, messagePackage).array();
    }

    public static byte[] newByteMessagePack(String text) {
        return newByteMessagePack(MessagePackage.WORLD, text);
    }

    public static MessagePackage newMessagePack(short space, String text) {
        return newMessagePack(space, -1, text);
    }

    public static MessagePackage newMessagePack(short space, int receiverId, String text) {
        return new MessagePackage(space, 0,receiverId, text.getBytes());
    }
}
