package ru.mirea.dikanev.nikita.common.balance.voronoi;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ru.mirea.dikanev.nikita.common.balance.voronoi.beachline.Beachline;
import ru.mirea.dikanev.nikita.common.balance.voronoi.event.Event;
import ru.mirea.dikanev.nikita.common.balance.voronoi.event.SiteEvent;
import ru.mirea.dikanev.nikita.common.balance.voronoi.graph.Edge;
import ru.mirea.dikanev.nikita.common.balance.voronoi.graph.Graph;
import ru.mirea.dikanev.nikita.common.balance.voronoi.graph.VoronoiPoint;
import ru.mirea.dikanev.nikita.common.balance.voronoi.graph.Vertex;
import lombok.Getter;
import lombok.val;

/**
 * The main class and entry point of voronoi-java. Constructing a new instance of this class will generate the diagram in the form of a {@link ru.mirea.dikanev.nikita.common.balance.voronoi.graph.Graph}.
 *
 */
public class Voronoi {

    @Getter
    private final Graph graph = new Graph();


    /**
     * Builds an unbounded voronoi diagram based on the given site points.
     *
     * @param points the site points
     */
    public Voronoi(Collection<VoronoiPoint> points) {
        val queue = new PriorityQueue<Event>();
        points.stream().map(SiteEvent::new).forEach(queue::offer);
        points.forEach(graph::addSite);

        val beachline = new Beachline();
        double sweep = Double.MAX_VALUE;
        while (!queue.isEmpty()) {
            val e = queue.peek();
            assert e.getPoint().y() <= sweep;
            e.handle(queue, beachline, graph);
            queue.remove(e);
            sweep = e.getPoint().y();
        }
    }


    /**
     * Applies the bounding box with the specified coordinates to a copy of this voronoi diagram. If this voronoi diagram is already unbounded an identical copy is returned.
     *
     * @param x the x coordinate of the bounding box
     * @param y the y coordinate of the bounding box
     * @param width the width of the bounding box
     * @param height the height of the bounding box
     *
     * @return a copy of this voronoi digram bounded by the given bounding box
     * @throws IllegalArgumentException if any site point of this diagram lies outside the given bounding box
     */
    public Voronoi applyBoundingBox(double x, double y, double width, double height) {
        getGraph().getSitePoints().stream()
                .filter(p -> p.x() < x || p.x() > x + width || p.y() < y || p.y() > y + height).findAny().ifPresent(p -> {
            throw new IllegalArgumentException("Site " + p + " lies outside the bounding box.");
        });
        throw new UnsupportedOperationException("Not implemented.");//TODO
    }

    /**
     * Computes a lloyd relaxation of this voronoi diagram. The original diagram remains unchanged.
     * @see <a href="https://en.wikipedia.org/wiki/Lloyd's_algorithm">Lloyd's algorithm on wikipedia</a>
     *
     * @return a new voronoi diagram representing the lloyd relaxation of this one
     */
    public Voronoi relax() {
        Map<VoronoiPoint, Set<Edge>> edges = new HashMap<>();
        graph.getSitePoints().forEach(p -> edges.put(p, new HashSet<>()));
        graph.edgeStream().forEach(e -> {
            edges.get(e.getSite1()).add(e);
            edges.get(e.getSite2()).add(e);
        });
        List<VoronoiPoint> newPoints = graph.getSitePoints().stream().map(site -> {
            Set<Vertex> vertices = Stream.concat(edges.get(site).stream().map(Edge::getA), edges.get(site).stream().map(Edge::getB)).collect(
                    Collectors.toSet());
            if (vertices.isEmpty() || vertices.contains(null)) {
                return site;
            } else {
                double avgX = vertices.stream().mapToDouble(v -> v.getLocation().x()).average().getAsDouble();
                double avgY = vertices.stream().mapToDouble(v -> v.getLocation().y()).average().getAsDouble();
                return new VoronoiPoint(avgX, avgY);
            }
        }).collect(Collectors.toList());
        return new Voronoi(newPoints);
    }

}
