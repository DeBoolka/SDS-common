package ru.mirea.dikanev.nikita.common.server.protocol.codec;

import java.nio.ByteBuffer;

import ru.mirea.dikanev.nikita.common.server.protocol.pack.ReconnectPackage;

public class ReconnectCodec extends Codec<ReconnectPackage> {

    private static ReconnectCodec reconnectCodec = new ReconnectCodec();

    @Override
    public ByteBuffer encode(ByteBuffer buffer, ReconnectPackage reconnectPackage) {
        putByteString(buffer, reconnectPackage.host);
        buffer.putInt(reconnectPackage.port);

        return buffer;
    }

    @Override
    public ReconnectPackage decode(ByteBuffer buffer) {
        byte[] host = readByteString(buffer);
        int port = buffer.getInt();

        return new ReconnectPackage(host, port);
    }

    public static int size(ReconnectPackage reconnectPackage) {
        return stringBytes(reconnectPackage.host) + Integer.BYTES;
    }

    public static byte[] newReconnectPack(String host, int port) {
        ReconnectPackage reconnectPackage = new ReconnectPackage(host.getBytes(), port);
        ByteBuffer buffer = ByteBuffer.allocate(size(reconnectPackage));

        return reconnectCodec.encode(buffer, reconnectPackage).array();
    }
}
