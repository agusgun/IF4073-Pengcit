package com.pengcit.jorfelag.feature_generator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class FeatureGenerator {

    public static void convertASCIIFeaturesToCSV() {
        String fname = "ascii_features.csv";
        assert ASCIIFeatures.labels.length == ASCIIFeatures.features_vector.length;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fname))) {
            for (int i = 0; i < ASCIIFeatures.labels.length; ++i) {
                String s = Arrays.toString(ASCIIFeatures.features_vector[i]);
                s = s.substring(1, s.length() - 1);

                s += ", \"" + ASCIIFeatures.labels[i] + "\"\n";
                bw.write(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Open files
//        File dataDir = new File("data");
//        IOFileFilter filter = new IOFileFilter() {
//            private final FileNameExtensionFilter filter =
//                    new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes());
//            public boolean accept(File file) {
//                return filter.accept(file);
//            }
//            public boolean accept(File dir, String name) {
//                File file = new File(dir.getAbsolutePath() + name);
//                return filter.accept(file);
//            }
//        };
//
//        Iterator<File> fileIterator = FileUtils.iterateFilesAndDirs(dataDir, filter, TrueFileFilter.INSTANCE);
//        while (fileIterator.hasNext()) {
//            File f = fileIterator.next();
//
//            System.out.println(f.getAbsolutePath());
//        }
        convertASCIIFeaturesToCSV();
    }
}
