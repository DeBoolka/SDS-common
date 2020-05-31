package ru.mirea.dikanev.nikita.common.server.protocol.pack;

import java.util.Arrays;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AddressPackage implements NetworkPackage {

    public byte[] host;
    public int port;

    @Override
    public String toString() {
        return "AddressPackage{" + "host=" + new String(host) + ", port=" + port + '}';
    }
}
