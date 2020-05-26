package ru.mirea.dikanev.nikita.common.server.protocol.codec;

import java.nio.ByteBuffer;

import ru.mirea.dikanev.nikita.common.server.protocol.pack.ReconnectPackage;

public class ReconnectCodec extends Codec<ReconnectPackage> {

    private static ReconnectCodec reconnectCodec = new ReconnectCodec();

    @Override
    public ByteBuffer encode(ByteBuffer buffer, ReconnectPackage reconnectPackage) {
        buffer.putInt(reconnectPackage.userId);
        buffer.putDouble(reconnectPackage.posX);
        buffer.putDouble(reconnectPackage.posY);
        putByteString(buffer, reconnectPackage.host);
        buffer.putInt(reconnectPackage.port);

        return buffer;
    }

    @Override
    public ReconnectPackage decode(ByteBuffer buffer) {
        int id = buffer.getInt();
        double x = buffer.getDouble();
        double y = buffer.getDouble();
        byte[] host = readByteString(buffer);
        int port = buffer.getInt();

        return new ReconnectPackage(id, x, y, host, port);
    }

    public static int size(ReconnectPackage reconnectPackage) {
        return stringBytes(reconnectPackage.host) + Integer.BYTES * 2 + Double.BYTES * 2;
    }

    public static byte[] newReconnectPack(String host, int port) {
        return newReconnectPack(-1, -1, -1, host.getBytes(), port);
    }

    public static byte[] newReconnectPack(double x, double y, String host, int port) {
        return newReconnectPack(-1, x, y, host.getBytes(), port);
    }

    public static byte[] newReconnectPack(int id, double x, double y) {
        return newReconnectPack(id, x, y, "".getBytes(), -1);
    }

    public static byte[] newReconnectPack(ReconnectPackage rp) {
        return newReconnectPack(rp.userId, rp.posX, rp.posY, rp.host, rp.port);
    }

    public static byte[] newReconnectPack(int id, double x, double y, byte[] host, int port) {
        ReconnectPackage reconnectPackage = new ReconnectPackage(id, x, y, host, port);
        ByteBuffer buffer = ByteBuffer.allocate(size(reconnectPackage));

        return reconnectCodec.encode(buffer, reconnectPackage).array();
    }
}
