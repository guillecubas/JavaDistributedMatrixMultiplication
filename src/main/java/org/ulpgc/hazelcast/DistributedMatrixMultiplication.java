package org.ulpgc.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.map.IMap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class DistributedMatrixMultiplication {

    public static double[][] multiply(double[][] A, double[][] B, HazelcastInstance hazelcastInstance) throws Exception {
        int size = A.length;
        String id = java.util.UUID.randomUUID().toString();

        // Guarda las matrices en el mapa distribuido
        IMap<String, double[][]> map = hazelcastInstance.getMap("matrices");
        map.put(id + "_A", A);
        map.put(id + "_B", B);

        // Ejecuta tareas distribuidas
        IExecutorService executor = hazelcastInstance.getExecutorService("exec");

        List<Future<double[]>> futures = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            RowMultiplicationTask task = new RowMultiplicationTask(i, size, id + "_A", id + "_B");
            futures.add(((IExecutorService) executor).submit(task));
        }

        // Recoge resultados
        double[][] result = new double[size][size];
        for (int i = 0; i < size; i++) {
            result[i] = futures.get(i).get();
        }

        return result;
    }
}
