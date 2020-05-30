package ru.mirea.dikanev.nikita.common.server.protocol.pack;

import lombok.AllArgsConstructor;
import lombok.ToString;

@ToString
@AllArgsConstructor
public class PositionPackage implements NetworkPackage {

    public int userId;
    public double x;
    public double y;

}
