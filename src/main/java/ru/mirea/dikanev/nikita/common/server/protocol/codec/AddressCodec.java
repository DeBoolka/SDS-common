package ru.mirea.dikanev.nikita.common.server.protocol.codec;

import java.nio.ByteBuffer;

import ru.mirea.dikanev.nikita.common.server.protocol.pack.AddressPackage;

public class AddressCodec extends Codec<AddressPackage> {

    private static AddressCodec addressCodec = new AddressCodec();

    @Override
    public ByteBuffer encode(ByteBuffer buffer, AddressPackage addressPackage) {
        putByteString(buffer, addressPackage.host);
        buffer.putInt(addressPackage.port);

        return buffer;
    }

    @Override
    public AddressPackage decode(ByteBuffer buffer) {
        byte[] host = readByteString(buffer);
        int port = buffer.getInt();

        return new AddressPackage(host, port);
    }

    public static int size(AddressPackage addressPackage) {
        return stringBytes(addressPackage.host) + Integer.BYTES;
    }

    public static byte[] newAddressPack(String host, int port) {
        AddressPackage addressPackage = new AddressPackage(host.getBytes(), port);
        ByteBuffer buffer = ByteBuffer.allocate(size(addressPackage));

        return addressCodec.encode(buffer, addressPackage).array();
    }
}
