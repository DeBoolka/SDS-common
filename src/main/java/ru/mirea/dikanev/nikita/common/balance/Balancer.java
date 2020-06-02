package ru.mirea.dikanev.nikita.common.balance;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.math3.ml.clustering.Cluster;
import ru.mirea.dikanev.nikita.common.balance.voronoi.Clusterer;
import ru.mirea.dikanev.nikita.common.balance.voronoi.Voronoi;
import ru.mirea.dikanev.nikita.common.balance.voronoi.graph.Graph;
import ru.mirea.dikanev.nikita.common.balance.voronoi.graph.VoronoiPoint;

@Log4j2
public class Balancer {

    private static final int M = 3;
    private static final float AVERAGE_ERROR = 1.2f;

    private final List<VoronoiPoint> points;
    private List<List<VoronoiPoint>> clusters;

    private Graph graph;

    public Balancer(List<VoronoiPoint> points) {
        this.points = Objects.requireNonNull(points);
    }

    public Balancer cluster(int countClusters) {
        if (countClusters > points.size()) {
            throw new IllegalArgumentException("The number of clusters is greater than the players");
        }

        double eps = calculateEps();
        log.info("Average distance between players: {}", eps);

        Clusterer<VoronoiPoint> clusterer = new Clusterer<>(eps, M);
        this.clusters = clusterer.cluster(points).stream().map(Cluster::getPoints).collect(Collectors.toList());

        log.info("Clusterer found {} clusters", clusters.size());
        if (countClusters > 0) {
            reduce(countClusters);
            increase(countClusters);
            if (clusters.size() > countClusters) {
                reduceTo(countClusters);
            } else if (clusters.size() < countClusters) {
                increaseTo(countClusters);
            }
        }

        return this;
    }

    public List<List<VoronoiPoint>> get() {
        return clusters;
    }

    public Graph graph() {
        return graph;
    }

    private double calculateEps() {
        log.info("Diagram Voronoi is building");
        Voronoi voronoi = new Voronoi(points);
        graph = voronoi.getGraph();
        return voronoi.getGraph()
                .edgeStream()
                .mapToDouble(edge -> Math.sqrt(
                        (edge.getSite2().x() - edge.getSite1().x()) * (edge.getSite2().x() - edge.getSite1().x()) +
                                (edge.getSite2().y() - edge.getSite1().y()) *
                                        (edge.getSite2().y() - edge.getSite1().y())))
                .average()
                .getAsDouble();
    }

    private void reduce(int countClusters) {
        log.info("Clusters are reducing");
        final int averageClusterSize = (int) (points.size() / countClusters * AVERAGE_ERROR);

        for (int i = 0; i < clusters.size(); i++) {
            if (clusters.get(i).size() <= averageClusterSize) {
                continue;
            }

            List<VoronoiPoint> reducedCluster = clusters.get(i);
            List<VoronoiPoint> newCluster = new ArrayList<>(averageClusterSize);
            IntStream.range(0,
                    reducedCluster.size() < averageClusterSize * 2 ?
                            reducedCluster.size() - averageClusterSize : averageClusterSize)
                    .forEach(j -> newCluster.add(reducedCluster.remove(reducedCluster.size() - 1)));
            clusters.add(newCluster);
            if (reducedCluster.size() > averageClusterSize) {
                i--;
            }
        }

        log.info("Clusters were reduced to {}", clusters.size());
    }

    private void reduceTo(int countClusters) {
        log.info("Clusters are reducing to {}", countClusters);
        final int averageClusterSize = (int) (points.size() / countClusters * AVERAGE_ERROR);
        int countReducedClusters = clusters.size() - countClusters;

        IntStream.range(0, countReducedClusters).forEach(i -> {
            List<VoronoiPoint> reducedCluster = clusters.remove(clusters.size() - 1);

            for (int j = countClusters - 1; !reducedCluster.isEmpty(); j--) {
                List<VoronoiPoint> increaseCluster = clusters.get(j);
                while (increaseCluster.size() < averageClusterSize && !reducedCluster.isEmpty()) {
                    increaseCluster.add(reducedCluster.remove(reducedCluster.size() - 1));
                }
            }
        });
    }

    private void increase(int countClusters) {
        log.info("Clusters are increasing");
        final int averageClusterSize = (int) (points.size() / countClusters * AVERAGE_ERROR);
        Comparator<List<VoronoiPoint>> comparator = Comparator.comparingInt(List::size);
        clusters.sort(comparator.reversed());

        for (int i = 0; i < clusters.size(); i++) {
            int currentClusterSize = clusters.get(i).size();
            if (currentClusterSize >= averageClusterSize ||
                    currentClusterSize + lastCluster().size() > averageClusterSize) {
                continue;
            }

            clusters.get(i).addAll(clusters.remove(clusters.size() - 1));
            i--;
        }

        log.info("Clusters were increased to {}", clusters.size());
    }

    private void increaseTo(int countClusters) {
        log.info("Clusters are increasing to {}", countClusters);
        final int averageClusterSize = points.size() / countClusters;
        final int countMissingClusters = countClusters - clusters.size();
        final int countReducedClusters =clusters.size();

        int reducedClusterIndex = 0;
        for (int i = 0; i < countMissingClusters; i++) {
            List<VoronoiPoint> increasedCluster = new ArrayList<>(averageClusterSize);
            List<VoronoiPoint> reducedCluster = clusters.get(reducedClusterIndex);

            while (increasedCluster.size() < averageClusterSize) {
                increasedCluster.add(reducedCluster.remove(reducedCluster.size() - 1));
                if (reducedCluster.size() <= averageClusterSize) {
                    reducedClusterIndex++;
                    if (reducedClusterIndex >= countReducedClusters) {
                        break;
                    }
                    reducedCluster = clusters.get(reducedClusterIndex);
                }
            }
            clusters.add(increasedCluster);
        }
    }

    private List<VoronoiPoint> lastCluster() {
        return clusters.get(clusters.size() - 1);
    }

}
