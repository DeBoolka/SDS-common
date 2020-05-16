package ru.mirea.dikanev.nikita.common.server.protocol.pack;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MessagePackage implements NetworkPackage {

    public short space;
    public int hop;
    public byte[] data;

}
