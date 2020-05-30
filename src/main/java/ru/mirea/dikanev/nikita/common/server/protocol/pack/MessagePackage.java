package ru.mirea.dikanev.nikita.common.server.protocol.pack;

import lombok.AllArgsConstructor;
import lombok.ToString;

@ToString
@AllArgsConstructor
public class MessagePackage implements NetworkPackage {

    public static final short WORLD = 1;
    public static final short CELL_SPACE = 2;

    public short space;
    public int hop;
    public int receiverId;
    public byte[] data;

}
