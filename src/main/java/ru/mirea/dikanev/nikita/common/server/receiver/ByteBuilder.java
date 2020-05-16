package ru.mirea.dikanev.nikita.common.server.receiver;

public class ByteBuilder {

    private byte[][] data;
    private short dataCounter = 0;

    public ByteBuilder() {
        this(1);
    }

    public ByteBuilder(int capacity) {
        data = new byte[capacity][];
    }

    public int position() {
        return dataCounter;
    }

    public int size() {
        return data.length;
    }

    public void put(byte[] dataFragment) {
        if (dataFragment == null) {
            throw new NullPointerException("Data fragment can't be null");
        } else if (dataCounter == data.length) {
            throw new IllegalStateException("The network package is full");
        }

        data[dataCounter] = dataFragment;
        dataCounter++;
    }

    public byte[][] getData() {
        return data;
    }

    public byte[] build() {
        int newSize = 0;
        for (int i = 0; i < dataCounter; i++) {
            newSize += data[i].length;
        }

        int pointer = 0;
        byte[] newData = new byte[newSize];
        for (int i = 0; i < dataCounter; i++) {
            System.arraycopy(data[i], 0, newData, pointer, data[i].length);
            pointer += data[i].length;
        }

        return newData;
    }
}
