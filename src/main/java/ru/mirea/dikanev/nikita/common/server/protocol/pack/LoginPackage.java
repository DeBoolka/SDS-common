package ru.mirea.dikanev.nikita.common.server.protocol.pack;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LoginPackage implements NetworkPackage {

    public byte[] login;
    public byte[] password;

}
