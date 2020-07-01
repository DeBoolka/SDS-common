package ru.mirea.dikanev.nikita.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.IntStream;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import ru.mirea.dikanev.nikita.common.server.protocol.codec.MetricCodec;
import ru.mirea.dikanev.nikita.common.server.protocol.pack.MetricPackage;
import ru.mirea.dikanev.nikita.common.server.receiver.ByteBuilder;

@Log4j2
public class PreparingLogs {

    public static void main(String[] args) throws IOException, InterruptedException {
        List<Metric> metrics = new ArrayList<>();
        metrics.addAll(readMetrics("/Users/nikita/Desktop/Документы/Универ/Диплом/Soft/SDS-common/src/main/resources/logs3/app-2020-06 17-20.41.27.log"));
//        metrics.addAll(readMetrics("/Users/nikita/Desktop/Документы/Универ/Диплом/Soft/SDS-common/src/main/resources/logs2/app-2020-06 16-17.34.53.log"));
//        metrics.addAll(readMetrics("/Users/nikita/Desktop/Документы/Универ/Диплом/Soft/SDS-common/src/main/resources/logs2/app-2020-06 16-18.31.15.log"));
//        metrics.addAll(readMetrics("/Users/nikita/Desktop/Документы/Универ/Диплом/Soft/SDS-common/src/main/resources/logs2/app-2020-06 16-18.31.21.log"));
//        metrics.addAll(readMetrics("/Users/nikita/Desktop/Документы/Универ/Диплом/Soft/SDS-common/src/main/resources/logs2/app-2020-06 16-18.31.57.log"));
//        metrics.addAll(readMetrics("/Users/nikita/Desktop/Документы/Универ/Диплом/Soft/SDS-common/src/main/resources/logs2/app-2020-06 16-18.32.16.log"));
//        metrics.addAll(readMetrics("/Users/nikita/Desktop/Документы/Универ/Диплом/Soft/SDS-common/src/main/resources/logs2/app-2020-06 16-18.33.28.log"));
//        metrics.addAll(readMetrics("/Users/nikita/Desktop/Документы/Универ/Диплом/Soft/SDS-common/src/main/resources/logs2/app-2020-06 16-18.33.55.log"));
//        metrics.addAll(readMetrics("/Users/nikita/Desktop/Документы/Универ/Диплом/Soft/SDS-common/src/main/resources/logs2/app-2020-06 16-18.37.12.log"));
        System.out.println("Number of requests: " + metrics.size());

//        metrics.sort(Comparator.comparingLong(Metric::getRead));
        long startedTime = metrics.stream().mapToLong(Metric::getRead).min().getAsLong();
        System.out.println("Min: " + startedTime);
        long finishedTime = metrics.stream().mapToLong(Metric::getRead).max().getAsLong();
        System.out.println("Max: " + finishedTime);
        List<List<Metric>> result = new ArrayList<>();

        IntStream.range(0, ((int) (finishedTime - startedTime) / 1000) + 1).forEach(i -> result.add(new ArrayList<>()));
        for (Metric metric : metrics) {
            result.get((int) (metric.read - startedTime) / 1000).add(metric);
        }

        result.forEach(r -> System.out.println(r.size()));
        System.out.println("Number of seconds: " + result.stream().filter(f -> f.size() > 0).count());
        System.out.println("Max requests of a second: " + result.stream().mapToLong(List::size).max().getAsLong());

        writeResult(result,
                "/Users/nikita/Desktop/Документы/Универ/Диплом/Soft/SDS-common/src/main/resources/logs3/result2.log");
    }

    private static void writeResult(List<List<Metric>> result, String path) throws IOException, InterruptedException {
        result.sort(Comparator.comparingLong(List::size));
        File file = new File(path);
        file.createNewFile();

        System.out.println("Write");
        PrintWriter writer = new PrintWriter(new FileWriter(file));
        for (int i = 0; i < result.size(); i++) {
            writer.print(i + "\t");
            writer.print(result.get(i).size() + "\t");
            writer.print(result.get(i).stream().mapToDouble(t -> ((double) t.write - t.read) / 2).average().getAsDouble() + "\t");
            writer.print(result.get(i).stream().mapToDouble(t -> ((double) t.process - t.read) / 2).average().getAsDouble() + "\t");
            writer.println(result.get(i).stream().mapToDouble(t -> ((double) t.write - t.process) / 2).average().getAsDouble());
        }

        writer.flush();
        System.out.println("END");
    }

    private static Collection<? extends Metric> readMetrics(String path) throws FileNotFoundException {
        File file = new File(path);

        Scanner scanner = new Scanner(file);
        List<Metric> metrics = new ArrayList<>();
        while (scanner.hasNext()) {
            metrics.add(new Metric(Long.parseLong(scanner.next()),
                    Long.parseLong(scanner.next()),
                    Long.parseLong(scanner.next())));
        }

        return metrics;
    }

    @Data
    @AllArgsConstructor
    static class Metric {
        long read;
        long process;
        long write;
    }

}
