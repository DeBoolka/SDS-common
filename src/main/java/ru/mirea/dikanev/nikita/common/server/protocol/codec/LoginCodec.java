package ru.mirea.dikanev.nikita.common.server.protocol.codec;

import java.nio.ByteBuffer;

import ru.mirea.dikanev.nikita.common.server.protocol.pack.LoginPackage;

public class LoginCodec extends Codec<LoginPackage> {

    private static LoginCodec loginCodec = new LoginCodec();

    @Override
    public ByteBuffer encode(ByteBuffer buffer, LoginPackage loginPackage) {
        putByteString(buffer, loginPackage.login);
        putByteString(buffer, loginPackage.password);

        return buffer;
    }

    @Override
    public LoginPackage decode(ByteBuffer buffer) {
        byte[] login = readByteString(buffer);
        byte[] password = readByteString(buffer);

        return new LoginPackage(login, password);
    }

    public static int size(LoginPackage loginPackage) {
        return stringBytes(loginPackage.login) + stringBytes(loginPackage.password);
    }

    public static byte[] newLoginPack(String login, String password) {
        LoginPackage loginPackage = new LoginPackage(login.getBytes(), password.getBytes());
        ByteBuffer buffer = ByteBuffer.allocate(size(loginPackage));

        return loginCodec.encode(buffer, loginPackage).array();
    }
}
