package com.pengcit.jorfelag.pengolahancitra.util.dataset;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Reads CSV file without headers.
 * Every column is numeric, except label in last column.
 *
 * Source: https://github.com/Rabrg/jknn/blob/master/src/main/java/me/rabrg/jknn/dataset/impl/CsvDataset.java
 */
public class CsvDataset extends Dataset {

    @Override
    public Dataset load(final InputStream... inputStream) throws IOException {
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream[0]))) {
            String line;
            while ((line = reader.readLine()) != null) {
                final String[] split = line.split(",");
                final double[] features = new double[split.length - 1];
                for (int i = 0; i < split.length - 1; i++)
                    features[i] = Double.parseDouble(split[i]);
                final String label = split[features.length];
                addEntry(features, label);
            }
        }
        return this;
    }
}