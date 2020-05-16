package ru.mirea.dikanev.nikita.common.server.entity;

import java.nio.ByteBuffer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Message {

    private ChannelConnector from;
    private ByteBuffer data;

    public Message(byte[] data) {
        this(null, data);
    }

    public Message(ChannelConnector from, byte[] data) {
        this.from = from;
        this.data = ByteBuffer.wrap(data);
    }
}
