package ru.mirea.dikanev.nikita.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import lombok.SneakyThrows;
import ru.mirea.dikanev.nikita.common.balance.Balancer;
import ru.mirea.dikanev.nikita.common.balance.voronoi.graph.VoronoiPoint;
import ru.mirea.dikanev.nikita.common.server.handler.CellHandler;
import ru.mirea.dikanev.nikita.common.server.processor.CellMessageProcessor;
import ru.mirea.dikanev.nikita.common.server.protocol.pack.PositionPackage;

import static java.util.Collections.swap;

public class MapView extends JFrame implements Runnable {

    private static final int size = 512;
    private static final long REMOVE_TIME = 2000;
    private int shiftX = 0;
    private int shiftY = 0;

    private static final double POINT_SIZE = 15.0;
    private Graphics2D g2;
    private JLabel numberOfSectors;

    private boolean flag = false;

    private CellMessageProcessor processor;

    private final Map<Integer, Info> players = new ConcurrentHashMap<>();

    public MapView(CellMessageProcessor processor, int shiftX, int shiftY) {
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setSize(size, size);
        this.processor = processor;
        this.shiftX = shiftX;
        this.shiftY = shiftY;

        addContent();

        new Thread(this).start();

        this.setVisible(true);
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

            Random r = new Random(info.cluster + 1);
            g2.setColor(new Color(r.nextFloat(), r.nextFloat(), r.nextFloat()));

            g2.fillOval(x, y, (int) POINT_SIZE, (int) POINT_SIZE);
            g2.drawString(String.valueOf(info.cluster), x + 9, y + 9);
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

    public void setNumberOfSectors(int numberOfSectors) {
        this.numberOfSectors.setText("Number of sectors: " + numberOfSectors);
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

    private void addContent() {
        numberOfSectors = new JLabel("Number of sectors: 0");
        JButton button = new JButton("Cluster");
        button.setSize(40, 20);
        button.addActionListener(actionEvent -> balance());

        JPanel grid = new JPanel(new GridLayout(2, 1, 0, 0) );
        grid.add(numberOfSectors);
        grid.add (button);

        JPanel flow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        flow.add(grid);
        Container container = getContentPane();
        container.add(flow, BorderLayout.SOUTH);

    }

    private void balance() {
        Balancer balancer = processor.balanceAction((CellHandler) processor.getServer().getMessageHandlers().get(0),
                null);

        List<List<VoronoiPoint>> clusters = balancer.clusters();
        IntStream.range(0, clusters.size())
                .forEach(i -> balancer.clusters()
                        .get(i)
                        .forEach(point -> Optional.ofNullable(players.get(point.playerId))
                                .ifPresent(info -> info.cluster = i)));
    }

    class Info{

        PositionPackage posPack;
        long lastUpdate;
        int cluster = 0;

        public Info(PositionPackage posPack) {
            this.posPack = posPack;
            updateTime();
        }

        public void updateTime() {
            lastUpdate = System.currentTimeMillis();
        }
    }
}
