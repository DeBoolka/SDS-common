package ru.mirea.dikanev.nikita.common.server.service.client;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.mirea.dikanev.nikita.common.math.Point;
import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;
import ru.mirea.dikanev.nikita.common.server.entity.client.Client;
import ru.mirea.dikanev.nikita.common.server.entity.client.UnauthenticatedClient;
import ru.mirea.dikanev.nikita.common.server.exception.AuthenticationException;
import ru.mirea.dikanev.nikita.common.server.secure.AuthenticationClient;
import ru.mirea.dikanev.nikita.common.server.secure.Credentials;

public class SimpleClientService implements ClientService {

    public static final Point DEFAULT_POSITION = new Point(0, 0);

    private Map<Integer, UserInfo> users = new ConcurrentHashMap<>();//TODO: replace with user storage
    private Map<Integer, SessionInfo> sessions = new ConcurrentHashMap<>();

    private volatile AtomicInteger lastId = new AtomicInteger(1);

    {
        users.put(0, new UserInfo("admin:admin", true));
    }

    public static Client getRootClient() {
        return new UnauthenticatedClient();
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
        /*if (!ROOT_USER_ID.equals(client.getId())) {
            users.remove(client.getId());
        }*/
    }

    @Override
    public Optional<Client> getClient(int id) {
        SessionInfo info = sessions.get(id);
        if (info == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(info.client);
    }

    @Override
    public Optional<Point> getPosition(int id) {
        SessionInfo info = sessions.get(id);
        if (info == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(info.position);
    }

    @Override
    public void setPosition(int id, Point position) {
        SessionInfo info = sessions.get(id);
        if (info == null) {
            return;
        }
        info.position = position;
    }

    @Override
    public boolean isAuth(Client client) {
        return client != null && sessions.containsKey(client.getId());
    }

    @Override
    public Map<Integer, SessionInfo> getClients() {
        return sessions;
    }

    @Override
    public void newSession(Client client, Point point) {
        users.putIfAbsent(client.getId(), new UserInfo("admin:admin", false));
        sessions.put(client.getId(), new SessionInfo(client, point));
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
        client.setCredentials(credentials);
        sessions.put(id, new SessionInfo(client, new Point(DEFAULT_POSITION)));
        return client;
    }

    private Optional<Integer> getClientIdByLoginAndPassword(String login, String password) {
        String logPass = login + ":" + password;
        return users.entrySet()
                .stream()
                .filter(entry -> logPass.equals(entry.getValue().getLoginAndPassword()))
                .findFirst()
                .map(Entry::getKey);
    }

    private boolean authentication(int clientId, Credentials credentials) {
        UserInfo info = users.computeIfAbsent(clientId, k -> new UserInfo(credentials.getLogin() + ":" + credentials.getPassword(), false));
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

    @Data
    @AllArgsConstructor
    @RequiredArgsConstructor
    public static class SessionInfo {
        private Client client;
        private Point position;
    }
}
