package com.pengcit.jorfelag.pengolahancitra.preprocess;

import android.graphics.Bitmap;

import com.pengcit.jorfelag.pengolahancitra.util.LoopBody;
import com.pengcit.jorfelag.pengolahancitra.util.Parallel;

import java.util.Arrays;

public class MultipleKernelTask extends BaseFilterTask {

    private final static int RED = 2;
    private final static int GREEN = 1;
    private final static int BLUE = 0;

    private double[][][] kernel;
    private int numOfKernels;

    public MultipleKernelTask(PreprocessOperatorFragment fr, double[][][] kernel) {
        super(fr);

        this.kernel = kernel;
        this.kernelSize = kernel[0].length;
        this.numOfKernels = kernel.length;
        this.offset = (kernelSize - 1) / 2;
    }

    @Override
    protected Bitmap doInBackground(Bitmap... bitmaps) {
        final Bitmap originalBitmap = resizeBitmap(bitmaps[0]);
        final Bitmap processedBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);

        final int height = originalBitmap.getHeight();
        final int width = originalBitmap.getWidth();
        final int paddedWidth = width + 2 * offset;

        final int[] processedPixels = getProcessedPixels(originalBitmap);

        Parallel.For(0, height, new LoopBody<Integer>() {
            @Override
            public void run(Integer y) {
                // Placeholder
                final int[] resultPixels = new int[width];
                int[] pixel = new int[3];
                double[][] gDir = new double[numOfKernels][];
                for (int k = 0; k < numOfKernels; ++k) {
                    gDir[k] = new double[3];
                }
                int[] s = new int[3];

                // Traverse width
                for (int x = 0; x < width; ++x) {
                    // Convolve
                    Arrays.fill(s, 0);

                    for (int i = 0; i < kernelSize; ++i) {
                        for (int j = 0; j < kernelSize; ++j) {
                            pixel[RED] = (processedPixels[(y + i) * width + x + j] & 0x00FF0000) >> 16;
                            pixel[GREEN] = (processedPixels[(y + i) * width + x + j] & 0x0000FF00) >> 8;
                            pixel[BLUE] = (processedPixels[(y + i) * width + x + j] & 0x000000FF);

                            for (int c = 0; c < 3; ++c) {
                                for (int k = 0; k < numOfKernels; ++k) {
                                    gDir[k][c] += kernel[k][i][j] * pixel[c];
                                }
                            }
                        }
                    }

                    for (int c = 0; c < 3; ++c) {
                        double gSum = 0;
                        for (int k = 0; k < numOfKernels; ++k) {
                            gSum += gDir[k][c] * gDir[k][c];
                        }
                        s[c] = Math.min(255, (int) Math.sqrt(gSum));
                    }

                    // Put magnitude in result
                    resultPixels[x] = (0xFF << 24)
                            | (s[RED] << 16)
                            | (s[GREEN] << 8)
                            | s[BLUE];
                }

                processedBitmap.setPixels(resultPixels, 0, width, 0, y, width, 1);
            }
        });

        return processedBitmap;
    }
}
