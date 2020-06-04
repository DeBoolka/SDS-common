package ru.mirea.dikanev.nikita.common.server.entity.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;
import ru.mirea.dikanev.nikita.common.server.secure.Credentials;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Client {

    private int id = -1;
    private ChannelConnector channel;
    private Credentials credentials;

    public Client(int id, Credentials credentials) {
        this.id = id;
        this.credentials = credentials;
    }

    public Client(int id) {
        this.id = id;
    }

    public Client(ChannelConnector channel) {
        this.channel = channel;
    }
}
