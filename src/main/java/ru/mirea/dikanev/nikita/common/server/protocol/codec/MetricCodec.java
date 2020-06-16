package ru.mirea.dikanev.nikita.common.server.protocol.codec;

import java.nio.ByteBuffer;

import ru.mirea.dikanev.nikita.common.server.protocol.pack.MetricPackage;
import ru.mirea.dikanev.nikita.common.server.protocol.pack.PositionPackage;

public class MetricCodec extends Codec<MetricPackage> {

    private static MetricCodec metricCodec = new MetricCodec();

    @Override
    public ByteBuffer encode(ByteBuffer buffer, MetricPackage metricPackage) {
        buffer.putInt(metricPackage.read);
        buffer.putInt(metricPackage.process);
        buffer.putInt(metricPackage.write);

        return buffer;
    }

    @Override
    public MetricPackage decode(ByteBuffer buffer) {
        int read = buffer.getInt();
        int process = buffer.getInt();
        int write = buffer.getInt();

        return new MetricPackage(read, process, write);
    }

    public static int size() {
        return Integer.BYTES * 3;
    }
}
