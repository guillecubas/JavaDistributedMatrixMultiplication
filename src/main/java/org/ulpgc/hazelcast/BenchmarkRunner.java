package org.ulpgc.hazelcast;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.util.UUID;

public class BenchmarkRunner {
    public static void main(String[] args) {
        HazelcastInstance hazelcastInstance = null;

        try {
            // ⚙️ Crea nodo embebido (no cliente)
            hazelcastInstance = Hazelcast.newHazelcastInstance();

            Runtime runtime = Runtime.getRuntime();
            OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

            int[] sizes = {256, 512, 1024};  // Puedes ampliar si lo deseas
            String[] headers = {
                    "Matrix Size", "Execution Time (ms)", "Memory Used (MB)",
                    "CPU Usage (%)", "Nodes Used", "Transfer Time (ms)"
            };
            String outputPath = "output/results_distributed.csv";

            CSVWriterUtility.writeHeadersIfNotExists(outputPath, headers);

            for (int size : sizes) {
                System.out.println("Multiplying matrices of size " + size + "x" + size);

                double[][] A = MatrixGenerator.generate(size, size);
                double[][] B = MatrixGenerator.generate(size, size);

                runtime.gc();
                long memBefore = runtime.totalMemory() - runtime.freeMemory();
                double cpuBefore = osBean.getSystemCpuLoad();

                IMap<String, double[][]> map = hazelcastInstance.getMap("matrices");

                String id = UUID.randomUUID().toString();
                long transferStart = System.currentTimeMillis();
                map.put(id + "_A", A);
                map.put(id + "_B", B);
                long transferEnd = System.currentTimeMillis();
                long transferTime = transferEnd - transferStart;

                int nodesUsed = hazelcastInstance.getCluster().getMembers().size();

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
        } catch (Exception e) {
            System.err.println("❌ Error during benchmark: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (hazelcastInstance != null) {
                hazelcastInstance.shutdown();
            }
        }
    }
}
