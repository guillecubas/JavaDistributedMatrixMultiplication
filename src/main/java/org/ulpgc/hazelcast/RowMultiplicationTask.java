package org.ulpgc.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import java.io.Serializable;
import java.util.concurrent.Callable;

public class RowMultiplicationTask implements Callable<double[]>, Serializable {
    private final int rowIndex;
    private final int size;
    private final String matrixAKey;
    private final String matrixBKey;

    public RowMultiplicationTask(int rowIndex, int size, String matrixAKey, String matrixBKey) {
        this.rowIndex = rowIndex;
        this.size = size;
        this.matrixAKey = matrixAKey;
        this.matrixBKey = matrixBKey;
    }

    @Override
    public double[] call() {
        HazelcastInstance hazelcastInstance = HazelcastContext.getInstance();
        IMap<String, double[][]> matrixMap = hazelcastInstance.getMap("matrices");

        double[][] A = matrixMap.get(matrixAKey);
        double[][] B = matrixMap.get(matrixBKey);

        if (A == null || B == null) {
            throw new IllegalStateException("Matrix A or B is null in RowMultiplicationTask");
        }

        double[] resultRow = new double[size];
        for (int j = 0; j < size; j++) {
            double sum = 0;
            for (int k = 0; k < size; k++) {
                sum += A[rowIndex][k] * B[k][j];
            }
            resultRow[j] = sum;
        }
        return resultRow;
    }
}
