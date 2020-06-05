package ru.mirea.dikanev.nikita.common.server.protocol.pack;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MessagePackage implements NetworkPackage {

    public static final short WORLD = 1;
    public static final short CELL_SPACE = 2;
    public static final short SECTOR_SPACE = 3;

    public short space;
    public int hop;
    public int receiverId;
    public byte[] data;

    @Override
    public String toString() {
        return "MessagePackage{" + "space=" + space + ", hop=" + hop + ", receiverId=" + receiverId + ", data=" +
                new String(data) + '}';
    }
}
