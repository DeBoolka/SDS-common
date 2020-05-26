package ru.mirea.dikanev.nikita.common.server.protocol.pack;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ReconnectPackage implements NetworkPackage {

    public int userId;
    public double posX;
    public double posY;
    public byte[] host;
    public int port;

}
