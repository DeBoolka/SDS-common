package ru.mirea.dikanev.nikita.common.server.protocol.pack;

import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
public class PositionPackage implements NetworkPackage {

    public int userId;
    public int x;
    public int y;

    @Override
    public String toString() {
        return "PositionPackage{" + "userId=" + userId + ", x=" + x + ", y=" + y + '}';
    }
}
