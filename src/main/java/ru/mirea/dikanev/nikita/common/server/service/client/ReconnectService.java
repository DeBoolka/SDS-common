package ru.mirea.dikanev.nikita.common.server.service.client;

import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;
import ru.mirea.dikanev.nikita.common.server.entity.client.Client;

public interface ReconnectService {

    void push(int id, Client client);

    Client pop(int id);

}
