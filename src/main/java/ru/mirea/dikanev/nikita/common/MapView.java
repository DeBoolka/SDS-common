package ru.mirea.dikanev.nikita.common;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JFrame;

import lombok.SneakyThrows;
import ru.mirea.dikanev.nikita.common.balance.Balancer;
import ru.mirea.dikanev.nikita.common.balance.voronoi.graph.VoronoiPoint;
import ru.mirea.dikanev.nikita.common.server.protocol.pack.PositionPackage;

import static java.util.Collections.swap;

public class MapView extends JFrame implements Runnable {

    private static final int size = 512;
    private static final long REMOVE_TIME = 2000;
    private int shiftX = 0;
    private int shiftY = 0;

    private static final double POINT_SIZE = 15.0;
    private Graphics2D g2;

    private boolean flag = false;

    private final Map<Integer, Info> players = new ConcurrentHashMap<>();

    public MapView(int shiftX, int shiftY) {
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setSize(size, size);
        this.setVisible(true);
        this.shiftX = shiftX;
        this.shiftY = shiftY;

        new Thread(this).start();
    }

    public void paint(Graphics g) {
        super.paint(g);
        this.g2 = (Graphics2D) g;
//        this.g2.clearRect(0, 0, size, size);

        players.values()
                .stream()
                .filter(info -> System.currentTimeMillis() - info.lastUpdate > REMOVE_TIME)
                .map(info -> info.posPack.userId)
                .collect(Collectors.toList())
                .forEach(players::remove);

        players.values().forEach(info -> {
            PositionPackage pos = info.posPack;
            int x = (int) Math.round((pos.x - shiftX) * 51.2f - POINT_SIZE / 2);
            x = Math.max(x, (int) POINT_SIZE);
            x = Math.min(x, size - (int) POINT_SIZE);
            int y = size - (int) Math.round((pos.y - shiftY) * 51.2f + POINT_SIZE / 2);
            y = Math.max(y, (int) POINT_SIZE);
            y = Math.min(y, size - (int) POINT_SIZE);

            g2.fillOval(x, y, (int) POINT_SIZE, (int) POINT_SIZE);
        });
    }

    public void movePlayer(int userId, int x, int y) {
        players.putIfAbsent(userId, new Info(new PositionPackage(userId, x, y)));
        Info info = players.get(userId);
        PositionPackage posPack = info.posPack;
        posPack.x = x;
        posPack.y = y;
        info.updateTime();
        flag = true;
    }

    public void removePlayer(int userId) {
        players.remove(userId);
        flag = true;
    }

    @SneakyThrows
    @Override
    public void run() {
        while (true) {
            Thread.sleep(500);
            if (flag) {
                repaint();
                flag = false;
            }
        }
    }

    class Info{

        public Info(PositionPackage posPack) {
            this.posPack = posPack;
            updateTime();
        }

        PositionPackage posPack;
        long lastUpdate;

        public void updateTime() {
            lastUpdate = System.currentTimeMillis();
        }
    }
}
