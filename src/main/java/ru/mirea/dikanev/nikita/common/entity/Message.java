package ru.mirea.dikanev.nikita.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.mirea.dikanev.nikita.common.server.handler.MessageHandler;
import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Message {

    public static final int NEAR = 0;
    public static final int CELL = 1;
    public static final int WORLD = 2;

    private MessageHandler handler = null;
    private ChannelConnector from;
    private int space = NEAR;
    private byte[] message;

}
