package ru.mirea.dikanev.nikita.common.server.protocol.pack;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PositionPackage implements NetworkPackage {

    public double x;
    public double y;

}
