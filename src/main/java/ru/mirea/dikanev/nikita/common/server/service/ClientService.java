package ru.mirea.dikanev.nikita.common.server.service;

import java.util.Optional;

import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;
import ru.mirea.dikanev.nikita.common.server.entity.client.Client;
import ru.mirea.dikanev.nikita.common.server.exception.AuthenticationException;

public interface ClientService {

    Client login(ChannelConnector from, String login, String password) throws AuthenticationException;

    Client login(ChannelConnector from, Client client) throws AuthenticationException;

    void exitClient(Client client);

    Optional<Client> getClient(int id);

    boolean isAuth(Client client);
}
