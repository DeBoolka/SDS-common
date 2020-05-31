package ru.mirea.dikanev.nikita.common.server.protocol.pack;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AddressPackage implements NetworkPackage {

    public byte[] host;
    public int port;

}
