package ru.mirea.dikanev.nikita.common.server.protocol.pack;

import lombok.AllArgsConstructor;
import lombok.ToString;

@ToString
@AllArgsConstructor
public class LoginPackage implements NetworkPackage {

    public byte[] login;
    public byte[] password;

}
