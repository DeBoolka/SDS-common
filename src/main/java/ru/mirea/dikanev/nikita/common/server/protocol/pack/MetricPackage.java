package ru.mirea.dikanev.nikita.common.server.protocol.pack;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MetricPackage implements NetworkPackage {

    public static final long TIME = System.currentTimeMillis() / 10000000 * 10000000;

    public int read;
    public int process;
    public int write;

    @Override
    public String toString() {
        return (TIME + read) + "\t" + (TIME + process) + "\t" + (TIME + write);
    }
}
