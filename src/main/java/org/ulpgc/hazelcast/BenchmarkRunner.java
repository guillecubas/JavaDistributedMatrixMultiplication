package org.ulpgc.hazelcast;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;

public class BenchmarkRunner {
    public static void main(String[] args) {
        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance();
        Runtime runtime = Runtime.getRuntime();
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

        int[] sizes = {2048, 4096, 8192};
        String[] headers = {"Matrix Size", "Execution Time (ms)", "Memory Used (MB)", "CPU Usage (%)", "Nodes Used", "Transfer Time (ms)"};
        String outputPath = "results_distributed.csv";

        CSVWriterUtility.writeHeadersIfNotExists(outputPath, headers);

        for (int size : sizes) {
            System.out.println("Multiplying matrices of size " + size + "x" + size);

            double[][] A = MatrixGenerator.generate(size, size);
            double[][] B = MatrixGenerator.generate(size, size);

            runtime.gc();
            long memBefore = runtime.totalMemory() - runtime.freeMemory();
            double cpuBefore = osBean.getSystemCpuLoad();

            long transferStart = System.currentTimeMillis();
            int nodesUsed = hazelcastInstance.getCluster().getMembers().size();
            long transferEnd = System.currentTimeMillis();
            long transferTime = transferEnd - transferStart;

            long start = System.currentTimeMillis();
            double[][] result = DistributedMatrixMultiplication.multiply(A, B, hazelcastInstance);
            long end = System.currentTimeMillis();
            long execTime = end - start;

            long memAfter = runtime.totalMemory() - runtime.freeMemory();
            double memUsedMB = (memAfter - memBefore) / (1024.0 * 1024);
            double cpuUsed = osBean.getSystemCpuLoad() * 100;

            String[] row = {
                    String.valueOf(size),
                    String.valueOf(execTime),
                    String.format("%.2f", memUsedMB),
                    String.format("%.2f", cpuUsed),
                    String.valueOf(nodesUsed),
                    String.valueOf(transferTime)
            };
            CSVWriterUtility.appendBenchmark(outputPath, row);

            System.out.printf("✔ %dx%d completed: %d ms | %.2f MB | %.2f%% CPU | %d nodes | %d ms transfer\n",
                    size, size, execTime, memUsedMB, cpuUsed, nodesUsed, transferTime);
        }

        hazelcastInstance.shutdown();
    }
}
