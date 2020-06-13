package ru.mirea.dikanev.nikita.common.balance.voronoi.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JFrame;

import ru.mirea.dikanev.nikita.common.balance.Balancer;
import ru.mirea.dikanev.nikita.common.balance.voronoi.graph.VoronoiPoint;

import static java.util.Collections.swap;

public class RenderVoronoi extends JFrame {

    private static final int size = 512;

    private static final double POINT_SIZE = 5.0;
    private final Balancer balancer;

    public RenderVoronoi(Balancer balancer) {
        this.balancer = balancer;
    }

    public void paint(Graphics g) {
        System.out.println("Numbers of clusters: " + balancer.clusters().size());

        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.BLUE);
        for (int i = 0; i < balancer.clusters().size(); i++) {
            Random r = new Random(i);
            g2.setColor(new Color(r.nextFloat(), r.nextFloat(), r.nextFloat()));

            swap(balancer.clusters(), 0, 2);
            for (VoronoiPoint point : balancer.clusters().get(i)) {
                //Draw input points
                g2.fillOval((int) Math.round(point.x() - POINT_SIZE / 2),
                        size - (int) Math.round(point.y() + POINT_SIZE / 2),
                        (int) POINT_SIZE,
                        (int) POINT_SIZE);

                //Draw number of cluster
                g2.drawString(String.valueOf(i),
                        (int) Math.round(point.x() - POINT_SIZE / 2 + 3),
                        size - (int) Math.round(point.y() + POINT_SIZE / 2 + 3));
            }

            System.out.println("Cluster " + i + " size: " + balancer.clusters().get(i).size());
        }

        //Draw triangulation of Delona
//        balancer.graph().edgeStream().filter(e -> e.getA() != null && e.getB() != null).forEach(e -> {
//                        VoronoiPoint a = e.getSite1();
//                        VoronoiPoint b = e.getSite2();
//            //            g2.drawLine((int)a.x, size - (int)a.y + 32, (int)b.x, size - (int)b.y + 32);
//                        g2.drawLine((int)a.x(), size - (int) a.y(), (int)b.x(), size - (int) b.y());
//                    });
////        g2.setColor(new Color(0.7F, 0.7F, 0.7F));
//        balancer.clusters().stream().flatMap(Collection::stream).forEach(p -> {
//            g2.fillOval((int) Math.round(p.x() - POINT_SIZE / 2),
//                                            size - (int) Math.round(p.y() + POINT_SIZE / 2),
//                                            (int) POINT_SIZE,
//                                            (int) POINT_SIZE);
//        });

//        g2.drawLine((int)395, size - (int) 355, (int)471, size - (int) 226);
//        g2.drawLine((int)441, size - (int) 114, (int)471, size - (int) 226);

        //Draw diagram Voronoi
//        for (VoronoiPoint site : balancer.graph().getSitePoints()) {
//            g2.fillOval((int) Math.round(site.x()-POINT_SIZE/2), size - (int) Math.round(site.y()+POINT_SIZE/2), (int)POINT_SIZE, (int)POINT_SIZE);
////            g2.drawString(String.format("%d,%d", (int)site.x, (int)site.y), (int) site.x, size - (int)site.y + 32);
//        }
//
//        balancer.graph().edgeStream().filter(e -> e.getA() != null && e.getB() != null).forEach(e -> {
//            VoronoiPoint a = e.getA().getLocation();
//            VoronoiPoint b = e.getB().getLocation();
////            g2.drawLine((int)a.x, size - (int)a.y + 32, (int)b.x, size - (int)b.y + 32);
//            g2.drawLine((int)a.x(), size - (int) a.y(), (int)b.x(), size - (int) b.y());
//        });
    }

    public static void main(String[] args) {
        Random r = new Random(92355638567L);
        Stream<VoronoiPoint> genOld = Stream.generate(() -> new VoronoiPoint(r.nextDouble() * size, r.nextDouble() * size, r.nextInt()));
        Stream<VoronoiPoint> gen = Stream.generate(() -> new VoronoiPoint(r.nextDouble() * 100 + 30, r.nextDouble() * 100 + 30, r.nextInt()));
        Stream<VoronoiPoint> gen2 = Stream.generate(() -> new VoronoiPoint(r.nextDouble() * 100 + 300, r.nextDouble() * 100 + 300, r.nextInt()));
        Stream<VoronoiPoint> gen3 = Stream.generate(() -> new VoronoiPoint(r.nextDouble() * 100 + 70, r.nextDouble() * 100 + 255, r.nextInt()));
        /*Voronoi diagram = new Voronoi(Arrays.asList(new Point(0, 0),
                new Point(40, 0),
                new Point(20, 20),
                new Point(0, 40),
                new Point(70, 70),
                new Point(40, 40)));*/

//        List<VoronoiPoint> vps = List.of(
//                new VoronoiPoint(50, 70),
//                new VoronoiPoint(65, 40),
//                new VoronoiPoint(78, 60),
//                new VoronoiPoint(45, 50),
//                new VoronoiPoint(30, 99),
//                new VoronoiPoint(30, 66),
//                new VoronoiPoint(75, 77),
//                new VoronoiPoint(80, 100),
//                new VoronoiPoint(90, 90),
//                new VoronoiPoint(400, 350),
//                new VoronoiPoint(422, 423),
//                new VoronoiPoint(423, 399)
//        );

//        Balancer balancer = new Balancer(vps);
        List<VoronoiPoint> vpLoser = gen.limit(13).collect(Collectors.toList());
        List<VoronoiPoint> vpEtalon = gen2.limit(13).collect(Collectors.toList());
        List<VoronoiPoint> vp = new ArrayList<>();
        vp.addAll(vpEtalon);
        vp.addAll(vpEtalon.stream()
                .map(p -> new VoronoiPoint(p.x() - 270, p.y() - 270))
                .collect(Collectors.toList()));
        vp.addAll(vpEtalon.stream()
                .map(p -> new VoronoiPoint(p.x() - 230, p.y() - 5))
                .collect(Collectors.toList()));
        vp.addAll(genOld.limit(20).collect(Collectors.toList()));
        Balancer balancer = new Balancer(vp);
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
