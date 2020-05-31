package ru.mirea.dikanev.nikita.common.server.protocol.pack;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
public class LoginPackage implements NetworkPackage {

    public byte[] login;
    public byte[] password;

    @Override
    public String toString() {
        return "LoginPackage{" + "login=" + new String(login) + ", password=" + new String(password) + '}';
    }
}
