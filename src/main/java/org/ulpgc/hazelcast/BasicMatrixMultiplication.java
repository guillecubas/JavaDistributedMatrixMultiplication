package org.ulpgc.hazelcast;

public class BasicMatrixMultiplication {
    public static double[][] multiply(double[][] A, double[][] B) {
        int rows = A.length;
        int cols = B[0].length;
        int common = B.length;
        double[][] result = new double[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double sum = 0.0;
                for (int k = 0; k < common; k++) {
                    sum += A[i][k] * B[k][j];
                }
                result[i][j] = sum;
            }
        }

        return result;
    }
}
