package org.ulpgc.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class DistributedMatrixMultiplication {

    public static double[][] multiply(double[][] A, double[][] B, HazelcastInstance hazelcastInstance) {
        int rows = A.length;
        int cols = B[0].length;
        int shared = A[0].length;

        double[][] result = new double[rows][cols];

        IExecutorService executor = hazelcastInstance.getExecutorService("matrix-executor");
        List<Future<RowResult>> futures = new ArrayList<>();

        for (int i = 0; i < rows; i++) {
            double[] rowA = A[i];
            futures.add(executor.submit(new RowMultiplicationTask(rowA, B, i)));
        }

        for (Future<RowResult> future : futures) {
            try {
                RowResult rowResult = future.get();
                result[rowResult.rowIndex] = rowResult.rowValues;
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public static class RowMultiplicationTask implements Callable<RowResult>, java.io.Serializable {
        private final double[] rowA;
        private final double[][] B;
        private final int rowIndex;

        public RowMultiplicationTask(double[] rowA, double[][] B, int rowIndex) {
            this.rowA = rowA;
            this.B = B;
            this.rowIndex = rowIndex;
        }

        @Override
        public RowResult call() {
            int cols = B[0].length;
            double[] resultRow = new double[cols];

            for (int j = 0; j < cols; j++) {
                double sum = 0;
                for (int k = 0; k < rowA.length; k++) {
                    sum += rowA[k] * B[k][j];
                }
                resultRow[j] = sum;
            }
            return new RowResult(rowIndex, resultRow);
        }
    }

    public static class RowResult implements java.io.Serializable {
        public final int rowIndex;
        public final double[] rowValues;

        public RowResult(int rowIndex, double[] rowValues) {
            this.rowIndex = rowIndex;
            this.rowValues = rowValues;
        }
    }
}
