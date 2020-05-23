package ru.mirea.dikanev.nikita.common.balance.voronoi.gui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JFrame;

import ru.mirea.dikanev.nikita.common.balance.voronoi.Voronoi;

public class RenderVoronoi extends JFrame {

    private static final int size = 512;

    private static final double POINT_SIZE = 5.0;
    private final Voronoi diagram;

    public RenderVoronoi(Voronoi diagram) {
        this.diagram = diagram;
    }

    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        for (Point site : diagram.getGraph().getSitePoints()) {
            g2.fillOval((int) Math.round(site.x-POINT_SIZE/2), size - (int) Math.round(site.y+POINT_SIZE/2), (int)POINT_SIZE, (int)POINT_SIZE);
//            g2.drawString(String.format("%d,%d", (int)site.x, (int)site.y), (int) site.x, size - (int)site.y + 32);
        }

        diagram.getGraph().edgeStream().filter(e -> e.getA() != null && e.getB() != null).forEach(e -> {
            Point a = e.getA().getLocation();
            Point b = e.getB().getLocation();
//            g2.drawLine((int)a.x, size - (int)a.y + 32, (int)b.x, size - (int)b.y + 32);
            g2.drawLine((int)a.x, size - (int) a.y, (int)b.x, size - (int) b.y);
        });
    }

    public static void main(String[] args) {
        Random r = new Random(9235563856L);
        Stream<Point> gen = Stream.generate(() -> new Point(r.nextDouble() * size, r.nextDouble() * size));
        /*Voronoi diagram = new Voronoi(Arrays.asList(new Point(0, 0),
                new Point(40, 0),
                new Point(20, 20),
                new Point(0, 40),
                new Point(70, 70),
                new Point(40, 40)));*/
        Voronoi diagram = new Voronoi(gen.limit(1024).collect(Collectors.toList())).relax().relax();
//        assert diagram.getGraph().edgeStream().noneMatch(e -> e.getA() == null && e.getB() == null);

        diagram.getGraph()
                .circleStream()
                .mapToDouble(circle -> circle.radius)
                .average()
                .ifPresent(System.out::println);
        RenderVoronoi frame = new RenderVoronoi(diagram);
        frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        frame.setSize(size, size+32);
        frame.setVisible(true);
    }
}
