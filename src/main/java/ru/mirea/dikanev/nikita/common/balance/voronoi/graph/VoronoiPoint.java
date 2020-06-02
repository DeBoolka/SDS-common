package ru.mirea.dikanev.nikita.common.balance.voronoi.graph;

import org.apache.commons.math3.ml.clustering.Clusterable;
import ru.mirea.dikanev.nikita.common.math.Point;

import static ru.mirea.dikanev.nikita.common.balance.voronoi.Math.EPSILON;
import static ru.mirea.dikanev.nikita.common.balance.voronoi.Math.PRECISION;
import static java.lang.Math.abs;

public class VoronoiPoint implements Clusterable {

    public final double[] points = new double[2];
    public int playerId;

    public VoronoiPoint(double x, double y) {
        this(x, y, -1);
    }

    public VoronoiPoint(double x, double y, int playerId) {
        setX(x);
        setY(y);
        this.playerId = playerId;
    }

    public VoronoiPoint(Point position, int userId) {
        this(position.x, position.y, userId);
    }

    public double x() {
        return points[0];
    }

    public double y() {
        return points[1];
    }

    private void setX(double x) {
        points[0] = x;
    }

    private void setY(double y) {
        points[1] = y;
    }

    @Override
    public double[] getPoint() {
        return points;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VoronoiPoint point = (VoronoiPoint) o;
        return abs(x() - point.x()) <= EPSILON && abs(y() - point.y()) <= EPSILON;
    }

    @Override
    public int hashCode() {
        return (int) (x() * PRECISION * 31) + (int) (y() * PRECISION);
    }

    @Override
    public String toString() {
        return String.format("(%.2f;%.2f)", x(), y());
    }
}
