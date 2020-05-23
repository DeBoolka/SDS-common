package ru.mirea.dikanev.nikita.common.server.secure;

import ru.mirea.dikanev.nikita.common.server.entity.client.Client;

public class AuthenticationClient extends Client {

    public AuthenticationClient(int id, Credentials credentials) {
        super(id, credentials);
    }

    public AuthenticationClient(int id) {
        super(id);
    }
}
