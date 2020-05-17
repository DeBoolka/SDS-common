package ru.mirea.dikanev.nikita.common.server.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;

@Data
@AllArgsConstructor
public class Entity {

    private int id;
    private ChannelConnector channel;
}
