package com.pengcit.jorfelag.pengolahancitra.preprocess;

import android.graphics.Bitmap;

public class BlurTask {

    public final static int BOX = 0;
    public final static int GAUSSIAN = 1;

    private SingleKernelTask task;

    public BlurTask(PreprocessOperatorFragment fr, int kernel, int kernelSize) {
        double[][] kernelMatrix = getKernel(kernel, kernelSize);
        task = new SingleKernelTask(fr, kernelMatrix);
    }

    public void execute(Bitmap... bitmaps) {
        task.execute(bitmaps);
    }

    private double[][] getKernel(int kernel, int size) {
        switch (kernel) {
            case GAUSSIAN:
                return generateGaussianKernel(size);
            default:    // BOX
                return generateBoxKernel(size);

        }
    }

    private double[][] generateGaussianKernel(int size) {
        double totalSize = 1 << ((size - 1) << 1);
        double[] row = getPascalTriangle(size);
        double[][] kernel = new double[size][];
        for (int i = 0; i < size; ++i) {
            kernel[i] = new double[size];
            for (int j = 0; j < size; ++j) {
                kernel[i][j] = row[i] * row[j] / totalSize;
            }
        }
        return kernel;
    }

    private double[][] generateBoxKernel(int size) {
        double totalSize = size * size;
        double[][] kernel = new double[size][];
        for (int i = 0; i < size; ++i) {
            kernel[i] = new double[size];
            for (int j = 0; j < size; ++j) {
                kernel[i][j] = 1 / totalSize;
            }
        }
        return kernel;
    }

    private double[] getPascalTriangle(int n) {
        double[] line = new double[n];
        line[0] = 1;

        for (int i = 1; i < n; ++i) {
            line[i] = line[i - 1] * (n - i) / (i);
        }
        return line;
    }
}
