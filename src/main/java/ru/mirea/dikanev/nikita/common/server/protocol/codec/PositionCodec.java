package ru.mirea.dikanev.nikita.common.server.protocol.codec;

import java.nio.ByteBuffer;

import ru.mirea.dikanev.nikita.common.server.protocol.pack.PositionPackage;

public class PositionCodec extends Codec<PositionPackage> {

    private static PositionCodec positionCodec = new PositionCodec();

    @Override
    public ByteBuffer encode(ByteBuffer buffer, PositionPackage positionPackage) {
        buffer.putInt(positionPackage.userId);
        buffer.putInt(positionPackage.x);
        buffer.putInt(positionPackage.y);

        return buffer;
    }

    @Override
    public PositionPackage decode(ByteBuffer buffer) {
        int id = buffer.getInt();
        int x = buffer.getInt();
        int y = buffer.getInt();

        return new PositionPackage(id, x, y);
    }

    public static int size() {
        return Integer.BYTES * 3;
    }

    public static byte[] newPositionPack(int id, int x, int y) {
        PositionPackage positionPackage = new PositionPackage(id, x, y);
        ByteBuffer buffer = ByteBuffer.allocate(size());

        return positionCodec.encode(buffer, positionPackage).array();
    }
}
