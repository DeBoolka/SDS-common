package ru.mirea.dikanev.nikita.common.balance.voronoi.graph;

import ru.mirea.dikanev.nikita.common.balance.voronoi.event.VertexEvent.Circle;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Vertex {

    @Getter
    private final Circle circle;

    @Override
    public String toString() {
        return location().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vertex vertex = (Vertex) o;

        return location().equals(vertex.location());
    }

    @Override
    public int hashCode() {
        return location().hashCode();
    }

    public VoronoiPoint location() {
        return circle.center;
    }

    public VoronoiPoint getLocation() {
        return location();
    }
}
