package org.ulpgc.hazelcast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParallelMatrixMultiplication {
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    public static double[][] multiply(double[][] A, double[][] B) {
        int rows = A.length;
        int cols = B[0].length;
        int common = B.length;
        double[][] result = new double[rows][cols];

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

        for (int i = 0; i < rows; i++) {
            final int row = i;
            executor.execute(() -> {
                for (int j = 0; j < cols; j++) {
                    double sum = 0.0;
                    for (int k = 0; k < common; k++) {
                        sum += A[row][k] * B[k][j];
                    }
                    result[row][j] = sum;
                }
            });
        }

        executor.shutdown();
        // Esperamos hasta que todos los hilos terminen (sin usar TimeUnit)
        while (!executor.isTerminated()) {
            try {
                Thread.sleep(50); // Espera activa simple
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Thread interrupted while waiting for executor shutdown.");
                break;
            }
        }

        return result;
    }
}
