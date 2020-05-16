package ru.mirea.dikanev.nikita.common.server.protocol.codec;

import java.nio.ByteBuffer;

import ru.mirea.dikanev.nikita.common.server.protocol.pack.NetworkPackage;

public abstract class Codec<T extends NetworkPackage> {

    public abstract ByteBuffer encode(ByteBuffer buffer, T netPack);

    public abstract T decode(ByteBuffer buffer);

    protected byte[] readByteString(ByteBuffer buffer) {
        int stringSize = buffer.getInt();
        byte[] byteStr = new byte[stringSize];
        buffer.get(byteStr, 0, stringSize);

        return byteStr;
    }

    protected String readString(ByteBuffer buffer) {
        return new String(readByteString(buffer));
    }

    protected void putByteString(ByteBuffer buffer, byte[] data) {
        buffer.putInt(data.length);
        buffer.put(data, 0, data.length);
    }

    protected static int stringBytes(byte[] data) {
        return Integer.BYTES + data.length;
    }

}
