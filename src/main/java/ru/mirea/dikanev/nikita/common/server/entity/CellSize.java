package ru.mirea.dikanev.nikita.common.server.entity;

import lombok.AllArgsConstructor;

public class CellSize {

    public Point upperLeftCorner;
    public Point bottomRightCorner;


    @AllArgsConstructor
    public static class Point{
        public int x;
        public int y;
    }

}
