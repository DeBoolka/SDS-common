package ru.mirea.dikanev.nikita.common.balance.voronoi;

import java.util.Arrays;

import ru.mirea.dikanev.nikita.common.balance.voronoi.graph.Point;

public class App {

    public static void main(String[] args) {

        Voronoi vor = new Voronoi(Arrays.asList(new Point(0.1, 0.1), new Point(0.5, 0.7), new Point(0.7, 0.8)));
//        vor.applyBoundingBox(0, 0, 1, 1);
        vor.getGraph().edgeStream().forEach(edge -> System.out.println(edge));

    }

}
