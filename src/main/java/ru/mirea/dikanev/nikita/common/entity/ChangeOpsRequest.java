package ru.mirea.dikanev.nikita.common.entity;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ChangeOpsRequest {

    public static final int CHANGE_OPS = 1;

    public static final int OP_READ_WRITE = SelectionKey.OP_READ | SelectionKey.OP_WRITE;

    public SelectableChannel channel;
    public int type;
    public int ops;

}
