package ru.mirea.dikanev.nikita.common.server.protocol.pack;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
public class ReconnectPackage implements NetworkPackage {

    public int userId;
    public int posX;
    public int posY;
    public byte[] host;
    public int port;

    @Override
    public String toString() {
        return "ReconnectPackage{" + "userId=" + userId + ", posX=" + posX + ", posY=" + posY + ", host=" +
                new String(host) + ", port=" + port + '}';
    }
}
