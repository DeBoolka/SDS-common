package ru.mirea.dikanev.nikita.common.server.protocol.codec;

import java.nio.ByteBuffer;

import ru.mirea.dikanev.nikita.common.server.protocol.pack.PositionPackage;

public class PositionCodec extends Codec<PositionPackage> {

    private static PositionCodec positionCodec = new PositionCodec();

    @Override
    public ByteBuffer encode(ByteBuffer buffer, PositionPackage positionPackage) {
        buffer.putInt(positionPackage.userId);
        buffer.putDouble(positionPackage.x);
        buffer.putDouble(positionPackage.y);

        return buffer;
    }

    @Override
    public PositionPackage decode(ByteBuffer buffer) {
        int id = buffer.getInt();
        double x = buffer.getDouble();
        double y = buffer.getDouble();

        return new PositionPackage(id, x, y);
    }

    public static int size() {
        return Double.BYTES * 2 + Integer.BYTES;
    }

    public static byte[] newPositionPack(int id, double x, double y) {
        PositionPackage positionPackage = new PositionPackage(id, x, y);
        ByteBuffer buffer = ByteBuffer.allocate(size());

        return positionCodec.encode(buffer, positionPackage).array();
    }
}
