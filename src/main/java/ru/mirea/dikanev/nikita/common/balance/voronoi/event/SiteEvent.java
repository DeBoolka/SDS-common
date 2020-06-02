package ru.mirea.dikanev.nikita.common.balance.voronoi.event;

import java.util.Collection;

import ru.mirea.dikanev.nikita.common.balance.voronoi.beachline.Beachline;
import ru.mirea.dikanev.nikita.common.balance.voronoi.graph.Edge;
import ru.mirea.dikanev.nikita.common.balance.voronoi.graph.Graph;
import ru.mirea.dikanev.nikita.common.balance.voronoi.graph.VoronoiPoint;
import lombok.val;

public class SiteEvent extends Event {

    public SiteEvent(VoronoiPoint point) {
        super(point);
    }

    @Override
    public void handle(Collection<Event> eventQueue, Beachline beachline, Graph graph) {
        val result = beachline.insertArc(getPoint());
        result.splitLeaf.ifPresent(l -> graph.addEdge(new Edge(l.getSite(), getPoint())));
        result.splitLeaf.ifPresent(l -> l.getSubscribers().forEach(eventQueue::remove));
        result.newLeaf.addCircleEvents(event -> {
            eventQueue.add(event);
        }, getPoint().y());
    }
}
