package ru.mirea.dikanev.nikita.common.server.service.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ru.mirea.dikanev.nikita.common.server.entity.client.Client;

public class SimpleReconnectService implements ReconnectService{

    private Map<Integer, Client> awaitingReconnect = new ConcurrentHashMap<>();

    @Override
    public void push(int id, Client client) {
        awaitingReconnect.put(id, client);
    }

    @Override
    public Client pop(int id) {
        return awaitingReconnect.remove(id);
    }
}
