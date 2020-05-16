package ru.mirea.dikanev.nikita.common.server.protocol.pack;

import org.junit.jupiter.api.Test;
import ru.mirea.dikanev.nikita.common.server.receiver.ByteBuilder;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class ByteBuilderTest {

    @Test
    void testPut() {
        byte[] data = new byte[]{1, 2};

        ByteBuilder byteBuilder = new ByteBuilder();
        byteBuilder.put(data);

        byte[] expectRes = new byte[]{1, 2};
        byte[] res = byteBuilder.build();

        assertArrayEquals(expectRes, res);
    }

    @Test
    void testBuild() {
        byte[] data1 = new byte[]{1, 2};
        byte[] data2 = new byte[]{3, 4, 5};
        byte[] data3 = new byte[]{6};

        ByteBuilder byteBuilder = new ByteBuilder(3);
        byteBuilder.put(data1);
        byteBuilder.put(data2);
        byteBuilder.put(data3);

        byte[] expectRes = new byte[]{1, 2, 3, 4, 5, 6};
        byte[] res = byteBuilder.build();

        assertArrayEquals(expectRes, res);
    }

    @Test
    void testBuildNotFull() {
        byte[] data1 = new byte[]{1, 2};
        byte[] data2 = new byte[]{3, 4, 5};

        ByteBuilder byteBuilder = new ByteBuilder(3);
        byteBuilder.put(data1);
        byteBuilder.put(data2);

        byte[] expectRes = new byte[]{1, 2, 3, 4, 5};
        byte[] res = byteBuilder.build();

        assertArrayEquals(expectRes, res);
    }

    @Test
    void testBuildEmpty() {
        ByteBuilder byteBuilder = new ByteBuilder(3);

        byte[] expectRes = new byte[]{};
        byte[] res = byteBuilder.build();

        assertArrayEquals(expectRes, res);
    }
}