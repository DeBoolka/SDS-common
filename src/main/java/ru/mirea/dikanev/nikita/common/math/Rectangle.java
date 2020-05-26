package ru.mirea.dikanev.nikita.common.math;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Rectangle {

    public Point upperLeftCorner;
    public Point bottomRightCorner;

    public Rectangle(int x1, int y1, int x2, int y2) {
        upperLeftCorner = new Point(x1, y1);
        bottomRightCorner = new Point(x2, y2);
    }

    public boolean isIntersectionBufferZone(double bufferZoneNearBorders, Point point) {
        return point.x >= bottomRightCorner.x
                || point.y <= bottomRightCorner.y
                || point.x <= upperLeftCorner.x
                || point.y >= upperLeftCorner.y;
    }
}
