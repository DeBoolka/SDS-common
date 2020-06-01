package ru.mirea.dikanev.nikita.common.math;

import lombok.AllArgsConstructor;
import lombok.ToString;

@ToString
@AllArgsConstructor
public class Rectangle {

    public Point upperLeftCorner;
    public Point bottomRightCorner;

    public Rectangle(double x1, double y1, double x2, double y2) {
        upperLeftCorner = new Point(x1, y1);
        bottomRightCorner = new Point(x2, y2);
    }

    public boolean isIntersectionBufferZone(double bufferZoneNearBorders, Point point) {
        return contains(point.x, point.y);
    }

    public boolean contains(double x, double y) {
        return !(x >= bottomRightCorner.x || y <= bottomRightCorner.y || x <= upperLeftCorner.x || y >= upperLeftCorner.y);
    }

}
