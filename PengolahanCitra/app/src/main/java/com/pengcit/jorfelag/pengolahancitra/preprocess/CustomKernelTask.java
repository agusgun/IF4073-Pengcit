package com.pengcit.jorfelag.pengolahancitra.preprocess;

import android.graphics.Bitmap;

import com.pengcit.jorfelag.pengolahancitra.util.LoopBody;
import com.pengcit.jorfelag.pengolahancitra.util.Parallel;

import java.util.Arrays;

public class CustomKernelTask extends BaseFilterTask {

    private final static int RED = 2;
    private final static int GREEN = 1;
    private final static int BLUE = 0;

    private double[][] kernel;
    private int kernelSize;
    private int offset;

    public CustomKernelTask(PreprocessOperatorFragment fr, double[][] kernel) {
        super(fr);

        this.kernel = kernel;
        this.kernelSize = kernel.length;
        this.offset = (kernelSize - 1) / 2;
    }

    @Override
    protected Bitmap doInBackground(Bitmap... bitmaps) {
        final Bitmap originalBitmap = resizeBitmap(bitmaps[0]);

        final int height = originalBitmap.getHeight();
        final int width = originalBitmap.getWidth();

        final Bitmap processedBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);

        final int paddedWidth = width + 2 * offset;
        final int paddedHeight = height + 2 * offset;
        final int startOffset = offset + offset * paddedWidth;

        final int[] processedPixels = new int[paddedHeight * paddedWidth];
        originalBitmap.getPixels(processedPixels, startOffset, width + 2 * offset,
                0, 0, width, height);

        // Left and right padding
        for (int i = 0; i < paddedHeight; ++i) {
            for (int j = 0; j < offset; ++j) {
                processedPixels[i * paddedWidth + j] = processedPixels[i * paddedWidth + offset];
            }
            for (int j = width + offset; j < paddedWidth; ++j) {
                processedPixels[i * paddedWidth + j] = processedPixels[i * paddedWidth + width + offset - 1];
            }
        }

        // Top and bottom padding
        for (int i = 0; i < offset; ++i) {
            System.arraycopy(processedPixels, offset * paddedWidth,
                    processedPixels, i * paddedWidth, paddedWidth);
        }
        for (int i = height + offset; i < paddedHeight; ++i) {
            System.arraycopy(processedPixels, (height + offset - 1) * paddedWidth,
                    processedPixels, i * paddedWidth, paddedWidth);
        }

        Parallel.For(0, height, new LoopBody<Integer>() {
            @Override
            public void run(Integer y) {
                // Placeholder
                final int[] resultPixels = new int[width];
                int[] pixel = new int[3];
                int[] s = new int[3];

                // Traverse width
                for (int x = 0; x < width; ++x) {
                    // Convolve
                    Arrays.fill(s, 0);

                    for (int i = 0; i < kernelSize; ++i) {
                        for (int j = 0; j < kernelSize; ++j) {
                            pixel[RED] = (processedPixels[(y + i) * paddedWidth + x + j] & 0x00FF0000) >> 16;
                            pixel[GREEN] = (processedPixels[(y + i) * paddedWidth + x + j] & 0x0000FF00) >> 8;
                            pixel[BLUE] = (processedPixels[(y + i) * paddedWidth + x + j] & 0x000000FF);

                            for (int c = 0; c < 3; ++c) {
                                s[c] += (int) (kernel[i][j] * pixel[c]);
                            }
                        }
                    }

                    for (int c = 0; c < 3; ++c) {
                        s[c] = Math.max(0, Math.min(255, s[c]));
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
