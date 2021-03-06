package ru.mirea.dikanev.nikita.common.balance.voronoi.graph;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import ru.mirea.dikanev.nikita.common.balance.voronoi.event.VertexEvent.Circle;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class Graph {

    private final BisectorMap edges = new BisectorMap();
    private final Set<VoronoiPoint> sites = new HashSet<>();

    public void addEdge(Edge e) {
        edges.put(e.getSite1(), e.getSite2(), e);
    }

    /**
     * Gets the edge that bisects the space between the two sites if there is one.
     *
     * @param a point of the first site
     * @param b point of the second site
     * @return an Optional containing the bisecting edge or an empty Optional if none exist
     */
    public Optional<Edge> getEdgeBetweenSites(VoronoiPoint a, VoronoiPoint b) {
        return Optional.ofNullable(edges.get(a, b));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Graph[");
        edges.values().stream().map(e -> String.format("(%s,%s),\n", e.getA(), e.getB())).sorted().forEachOrdered(sb::append);
        sb.append("]");
        return sb.toString();
    }

    public void addSite(VoronoiPoint newSite) {
        sites.add(newSite);
    }

    public Set<VoronoiPoint> getSitePoints() {
        return Collections.unmodifiableSet(sites);
    }

    public Stream<Edge> edgeStream() {
        return edges.stream();
    }

    public Stream<Circle> circleStream(){
        return edgeStream().flatMap(edge -> Stream.of(edge.getA(), edge.getB()))
                .filter(Objects::nonNull)
                .map(Vertex::getCircle)
                .distinct();
    }

}
