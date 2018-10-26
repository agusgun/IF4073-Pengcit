package com.pengcit.jorfelag.feature_generator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileNameExtensionFilter;

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

    public static void generateFeaturesText() {
        // Open files
        File dataDir = new File("data");
        IOFileFilter filter = new IOFileFilter() {
            private final FileNameExtensionFilter filter =
                    new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes());
            public boolean accept(File file) {
                return filter.accept(file);
            }
            public boolean accept(File dir, String name) {
                File file = new File(dir.getAbsolutePath() + name);
                return filter.accept(file);
            }
        };

        Iterator<File> fileIterator = FileUtils.iterateFilesAndDirs(dataDir, filter, TrueFileFilter.INSTANCE);
        StringBuilder features = new StringBuilder().append("FEATURES\n");
        StringBuilder labels = new StringBuilder().append("LABELS\n");

        int n = 0;
        char prevLabel = '\0';
        int k = 0;
        while (fileIterator.hasNext()) {
            try {
                File f = fileIterator.next();
                if (!f.isFile()) {
                    continue;
                }

                char label = f.getParentFile().getName().charAt(0);
                if (label == prevLabel) {
                    if (++k > 5) {
                        continue;
                    }
                } else {
                    prevLabel = label;
                    k = 1;
                }

                labels.append("\'").append(label).append("\', ");

                if (++n % 10 == 0) {
                    labels.append('\n');
                }

                BufferedImage img = ImageIO.read(f);
                ImageSkeletonizer isk = new ImageSkeletonizer(img, 127);
                double[] fe = isk.process(12, 50);
                features.append(Arrays.toString(fe)).append(",\n");
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        String fname = "ascii_features.txt";
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fname))) {
            bw.write(labels.toString());
            bw.write("\n\n");
            bw.write(features.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void generateFeaturesCsv() {
        // Open files
        File dataDir = new File("data");
        IOFileFilter filter = new IOFileFilter() {
            private final FileNameExtensionFilter filter =
                    new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes());
            public boolean accept(File file) {
                return filter.accept(file);
            }
            public boolean accept(File dir, String name) {
                File file = new File(dir.getAbsolutePath() + name);
                return filter.accept(file);
            }
        };

        Iterator<File> fileIterator = FileUtils.iterateFilesAndDirs(dataDir, filter, TrueFileFilter.INSTANCE);
        StringBuilder sb = new StringBuilder();
        StringBuilder features = new StringBuilder().append("FEATURES\n");
        StringBuilder labels = new StringBuilder().append("LABELS\n");

        char prevLabel = '\0';
        int k = 0;
        while (fileIterator.hasNext()) {
            try {
                File f = fileIterator.next();
                if (!f.isFile()) {
                    continue;
                }
                System.out.println(f.getPath());

                char label = f.getParentFile().getName().charAt(0);
                if (label == prevLabel) {
                    if (++k > 10) {
                        continue;
                    }
                } else {
                    prevLabel = label;
                    k = 1;
                }

                BufferedImage img = ImageIO.read(f);
                ImageSkeletonizer isk = new ImageSkeletonizer(img, 127);
                double[] fe = isk.process(12, 50);

                for (double d: fe) {
                    sb.append(d).append(", ");
                }
                sb.append(label).append("\n");

            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        String fname = "ascii_features.csv";
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fname))) {
            bw.write(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        generateFeaturesCsv();
    }
}
