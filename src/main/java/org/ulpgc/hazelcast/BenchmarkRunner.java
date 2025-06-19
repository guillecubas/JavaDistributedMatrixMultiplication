package org.ulpgc.hazelcast;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.util.Locale;
import java.util.UUID;

public class BenchmarkRunner {
    public static void main(String[] args) {
        HazelcastInstance hazelcastInstance = null;

        try {
            hazelcastInstance = Hazelcast.newHazelcastInstance();

            Runtime runtime = Runtime.getRuntime();
            OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

            int[] sizes = {256, 512, 1024, 2048};
            String[] headers = {
                    "Matrix Size", "Version", "Execution Time (ms)", "Memory Used (MB)",
                    "CPU Usage (%)", "Nodes Used", "Transfer Time (ms)"
            };
            String outputPath = "output/results_all_versions.csv";

            CSVWriterUtility.writeHeadersIfNotExists(outputPath, headers);

            for (int size : sizes) {
                System.out.println("\n===> Benchmark for size: " + size + "x" + size);

                double[][] A = MatrixGenerator.generate(size, size);
                double[][] B = MatrixGenerator.generate(size, size);

                // --- Basic ---
                System.out.println("→ Running Basic Multiplication");
                runtime.gc();
                long memBefore = runtime.totalMemory() - runtime.freeMemory();
                double cpuBefore = getSafeCpuLoad(osBean);

                long start = System.currentTimeMillis();
                double[][] basicResult = BasicMatrixMultiplication.multiply(A, B);
                long end = System.currentTimeMillis();
                long execTime = end - start;

                long memAfter = runtime.totalMemory() - runtime.freeMemory();
                double memUsedMB = (memAfter - memBefore) / (1024.0 * 1024);
                double cpuUsed = getSafeCpuLoad(osBean) * 100;

                CSVWriterUtility.appendBenchmark(outputPath, new String[]{
                        String.valueOf(size), "basic", String.valueOf(execTime),
                        String.format(Locale.US, "%.2f", memUsedMB),
                        String.format(Locale.US, "%.2f", cpuUsed),
                        "1", "0"
                });

                // --- Parallel ---
                System.out.println("→ Running Parallel Multiplication");
                runtime.gc();
                memBefore = runtime.totalMemory() - runtime.freeMemory();

                start = System.currentTimeMillis();
                double[][] parallelResult = ParallelMatrixMultiplication.multiply(A, B);
                end = System.currentTimeMillis();
                execTime = end - start;

                memAfter = runtime.totalMemory() - runtime.freeMemory();
                memUsedMB = (memAfter - memBefore) / (1024.0 * 1024);
                cpuUsed = getSafeCpuLoad(osBean) * 100;

                CSVWriterUtility.appendBenchmark(outputPath, new String[]{
                        String.valueOf(size), "parallel", String.valueOf(execTime),
                        String.format(Locale.US, "%.2f", memUsedMB),
                        String.format(Locale.US, "%.2f", cpuUsed),
                        "1", "0"
                });

                // --- Distributed ---
                System.out.println("→ Running Distributed Multiplication");
                runtime.gc();
                memBefore = runtime.totalMemory() - runtime.freeMemory();

                IMap<String, double[][]> map = hazelcastInstance.getMap("matrices");
                String id = UUID.randomUUID().toString();

                long transferStart = System.currentTimeMillis();
                map.put(id + "_A", A);
                map.put(id + "_B", B);
                long transferEnd = System.currentTimeMillis();
                long transferTime = transferEnd - transferStart;

                int nodesUsed = hazelcastInstance.getCluster().getMembers().size();

                start = System.currentTimeMillis();
                double[][] distResult = DistributedMatrixMultiplication.multiply(A, B, hazelcastInstance);
                end = System.currentTimeMillis();
                execTime = end - start;

                memAfter = runtime.totalMemory() - runtime.freeMemory();
                memUsedMB = (memAfter - memBefore) / (1024.0 * 1024);
                cpuUsed = getSafeCpuLoad(osBean) * 100;

                CSVWriterUtility.appendBenchmark(outputPath, new String[]{
                        String.valueOf(size), "distributed", String.valueOf(execTime),
                        String.format(Locale.US, "%.2f", memUsedMB),
                        String.format(Locale.US, "%.2f", cpuUsed),
                        String.valueOf(nodesUsed), String.valueOf(transferTime)
                });
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

    // Retry until valid CPU load is obtained (avoid -1.0)
    private static double getSafeCpuLoad(OperatingSystemMXBean osBean) {
        double load = osBean.getSystemCpuLoad();
        int retries = 0;
        while ((load < 0 || Double.isNaN(load)) && retries++ < 5) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {}
            load = osBean.getSystemCpuLoad();
        }
        return Math.max(load, 0);
    }
}
