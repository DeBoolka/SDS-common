package ru.mirea.dikanev.nikita.common.server.service;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;
import ru.mirea.dikanev.nikita.common.server.entity.client.Client;
import ru.mirea.dikanev.nikita.common.server.entity.client.UnauthenticatedClient;
import ru.mirea.dikanev.nikita.common.server.exception.AuthenticationException;
import ru.mirea.dikanev.nikita.common.server.secure.AuthenticationClient;
import ru.mirea.dikanev.nikita.common.server.secure.Credentials;

public class SimpleClientService implements ClientService {

    public static final Integer ROOT_USER_ID = 0;

    private Map<Integer, UserInfo> users = new ConcurrentHashMap<>();//TODO: replace with user storage
    private Map<Integer, Client> sessions = new ConcurrentHashMap<>();

    private volatile AtomicInteger lastId = new AtomicInteger(0);

    {
        users.put(ROOT_USER_ID, new UserInfo("root:root", true));
    }

    public static Client getRootClient() {
        return new Client(ROOT_USER_ID, new Credentials("root", "root"));
    }

    @Override
    public Client login(ChannelConnector connector, String login, String password)
            throws AuthenticationException {

        Optional<Integer> id = getClientIdByLoginAndPassword(login, password);
        if (id.isEmpty()) {
            id = Optional.of(lastId.getAndIncrement());
        }

        Client client = createSession(id.get(), login, password);
        client.setChannel(connector);
        return client;
    }

    @Override
    public Client login(ChannelConnector from, Client client) throws AuthenticationException {
        client = createSession(client);
        client.setChannel(from);
        return client;
    }

    @Override
    public void exitClient(Client client) {
        sessions.remove(client.getId());
        if (!ROOT_USER_ID.equals(client.getId())) {
            users.remove(client.getId());
        }
    }

    @Override
    public Optional<Client> getClient(int id) {
        return Optional.ofNullable(sessions.get(id));
    }

    @Override
    public boolean isAuth(Client client) {
        return client != null && sessions.containsKey(client.getId());
    }

    private Client createSession(Client client) throws AuthenticationException {
        if (client == null || client.getCredentials() == null) {
            return new UnauthenticatedClient();
        }

        Credentials credentials = client.getCredentials();
        return createSession(client.getId(), credentials.getLogin(), credentials.getPassword());
    }

    private Client createSession(int id, String login, String password) throws AuthenticationException {
        Credentials credentials = new Credentials(login, password);
        if (!authentication(id, credentials)) {
            throw new AuthenticationException("Authentication failed");
        }

        Client client = new AuthenticationClient(id);
        sessions.put(id, client);
        return client;
    }

    private Optional<Integer> getClientIdByLoginAndPassword(String login, String password) {
        if ("root".equals(login) && "root".equals(password)) {
            return Optional.of(ROOT_USER_ID);
        }

        String logPass = login + ":" + password;
        return users.entrySet()
                .stream()
                .filter(entry -> logPass.equals(entry.getValue().getLoginAndPassword()))
                .findFirst()
                .map(Entry::getKey);
    }

    private boolean authentication(int clientId, Credentials credentials) {
        UserInfo info = users.computeIfAbsent(clientId, k -> new UserInfo("admin:admin", false));
        if (info.isServer) {
            return true;
        }
        return info.getLoginAndPassword().equals(credentials.getLogin() + ":" + credentials.getPassword());
    }

    @Data
    @AllArgsConstructor
    public static class UserInfo {

        private String loginAndPassword;
        private boolean isServer;

    }

}
