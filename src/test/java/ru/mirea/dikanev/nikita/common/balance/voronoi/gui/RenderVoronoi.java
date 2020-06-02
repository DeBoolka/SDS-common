package ru.mirea.dikanev.nikita.common.balance.voronoi.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JFrame;

import ru.mirea.dikanev.nikita.common.balance.Balancer;
import ru.mirea.dikanev.nikita.common.balance.voronoi.graph.VoronoiPoint;

public class RenderVoronoi extends JFrame {

    private static final int size = 1024;

    private static final double POINT_SIZE = 5.0;
    private final Balancer balancer;

    public RenderVoronoi(Balancer balancer) {
        this.balancer = balancer;
    }

    public void paint(Graphics g) {
        System.out.println("Numbers of clusters: " + balancer.clusters().size());

        Graphics2D g2 = (Graphics2D) g;
        for (int i = 0; i < balancer.clusters().size(); i++) {
            Random r = new Random(i);
            g2.setColor(new Color(r.nextFloat(), r.nextFloat(), r.nextFloat()));

            for (VoronoiPoint point : balancer.clusters().get(i)) {
                g2.fillOval((int) Math.round(point.x() - POINT_SIZE / 2),
                        size - (int) Math.round(point.y() + POINT_SIZE / 2),
                        (int) POINT_SIZE,
                        (int) POINT_SIZE);
                g2.drawString(String.valueOf(i),
                        (int) Math.round(point.x() - POINT_SIZE / 2),
                        size - (int) Math.round(point.y() + POINT_SIZE / 2));
            }

            System.out.println("Cluster " + i + " size: " + balancer.clusters().get(i).size());
        }

//        for (VoronoiPoint site : diagram.getGraph().getSitePoints()) {
//            g2.fillOval((int) Math.round(site.x()-POINT_SIZE/2), size - (int) Math.round(site.y()+POINT_SIZE/2), (int)POINT_SIZE, (int)POINT_SIZE);
////            g2.drawString(String.format("%d,%d", (int)site.x, (int)site.y), (int) site.x, size - (int)site.y + 32);
//        }
//
//        diagram.getGraph().edgeStream().filter(e -> e.getA() != null && e.getB() != null).forEach(e -> {
//            VoronoiPoint a = e.getA().getLocation();
//            VoronoiPoint b = e.getB().getLocation();
////            g2.drawLine((int)a.x, size - (int)a.y + 32, (int)b.x, size - (int)b.y + 32);
//            g2.drawLine((int)a.x(), size - (int) a.y(), (int)b.x(), size - (int) b.y());
//        });
    }

    public static void main(String[] args) {
        Random r = new Random(9235563856L);
        Stream<VoronoiPoint> gen = Stream.generate(() -> new VoronoiPoint(r.nextDouble() * size, r.nextDouble() * size, r.nextInt()));
        /*Voronoi diagram = new Voronoi(Arrays.asList(new Point(0, 0),
                new Point(40, 0),
                new Point(20, 20),
                new Point(0, 40),
                new Point(70, 70),
                new Point(40, 40)));*/
        Balancer balancer = new Balancer(gen.limit(10).collect(Collectors.toList()));
//        assert diagram.getGraph().edgeStream().noneMatch(e -> e.getA() == null && e.getB() == null);

        /*diagram.getGraph()
                .circleStream()
                .mapToDouble(circle -> circle.radius)
                .average()
                .ifPresent(System.out::println);*/
        RenderVoronoi frame = new RenderVoronoi(balancer.cluster(3));
        frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        frame.setSize(size, size);
        frame.setVisible(true);
    }
}
