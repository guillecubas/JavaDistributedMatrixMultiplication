package org.ulpgc.hazelcast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CSVWriterUtility {
    public static void writeHeadersIfNotExists(String path, String[] headers) {
        File file = new File(path);
        if (!file.exists()) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(String.join(",", headers) + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void appendBenchmark(String path, String[] row) {
        try (FileWriter writer = new FileWriter(path, true)) {
            writer.write(String.join(",", row) + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
