package com.pengcit.jorfelag.feature_generator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FeatureGenerator {

    public static void main(String[] args) {
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
        while (fileIterator.hasNext()) {
            File f = fileIterator.next();

            System.out.println(f.getAbsolutePath());
        }
    }
}
