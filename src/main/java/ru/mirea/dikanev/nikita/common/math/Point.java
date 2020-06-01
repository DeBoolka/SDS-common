package ru.mirea.dikanev.nikita.common.math;

import lombok.AllArgsConstructor;
import lombok.ToString;

@ToString
@AllArgsConstructor
public class Point {

    public double x;
    public double y;

    public Point(Point point) {
        this.x = point.x;
        this.y = point.y;
    }
}
