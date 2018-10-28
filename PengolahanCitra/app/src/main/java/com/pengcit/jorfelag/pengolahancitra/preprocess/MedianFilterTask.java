package com.pengcit.jorfelag.pengolahancitra.preprocess;

import android.graphics.Bitmap;

import com.pengcit.jorfelag.pengolahancitra.util.LoopBody;
import com.pengcit.jorfelag.pengolahancitra.util.Parallel;

import java.util.Arrays;

public class MedianFilterTask extends BaseFilterTask {

    private int kernelSize;
    private int kernelSizeSq;
    private int offset;

    public MedianFilterTask(PreprocessOperatorFragment fr, int kernelSize) {
        super(fr);

        if (kernelSize % 2 == 0 || kernelSize <= 1) {
            throw new IllegalArgumentException("Kernel size must be an odd number larger than 1");
        }

        this.kernelSize = kernelSize;
        this.kernelSizeSq = kernelSize * kernelSize;
        this.offset = (kernelSize - 1) / 2;
    }

    @Override
    protected Bitmap doInBackground(Bitmap... bitmaps) {
        final Bitmap originalBitmap = bitmaps[0].copy(Bitmap.Config.ARGB_8888, false);

        final int height = originalBitmap.getHeight();
        final int width = originalBitmap.getWidth();

        final Bitmap processedBitmap = bitmaps[0].copy(Bitmap.Config.ARGB_8888, true);

        Parallel.For(0, height, new LoopBody<Integer>() {
            @Override
            public void run(Integer y) {
                final int paddedWidth = width + 2 * offset;
                final int[] processedPixels = new int[kernelSize * paddedWidth];

                final int yUpper = Math.max(0, y - offset);
                final int yLower = Math.min(height - 1, y + offset);

                final int startOffset = offset + Math.max(0, offset - y) * paddedWidth;

                originalBitmap.getPixels(processedPixels, startOffset, width + 2 * offset,
                        0, yUpper, width, yLower - yUpper + 1);

                // Left and right padding
                for (int i = 0; i < kernelSize; ++i) {
                    int j;
                    for (j = 0; j < offset; ++j) {
                        processedPixels[i * paddedWidth + j] = processedPixels[i * paddedWidth + offset];
                    }
                    for (j = width + offset; j < paddedWidth; ++j) {
                        processedPixels[i * paddedWidth + j] = processedPixels[i * paddedWidth + width + offset - 1];
                    }
                }

                // Top and bottom padding
                if (yUpper > y - offset) {
                    int yRef = offset - (y - yUpper);
                    for (int i = 0; i < yRef; ++i) {
                        System.arraycopy(processedPixels, yRef * paddedWidth,
                                processedPixels, i * paddedWidth, paddedWidth);
                    }
                }
                if (yLower < y + offset) {
                    int yRef = offset + (yLower - y);
                    for (int i = yRef + 1; i < kernelSize; ++i) {
                        System.arraycopy(processedPixels, yRef * paddedWidth,
                                processedPixels, i * paddedWidth, paddedWidth);
                    }
                }

                // Placeholder
                final int[] resultPixels = new int[width];
                final int[] red = new int[256];
                final int[] green = new int[256];
                final int[] blue = new int[256];

                // Traverse width
                for (int x = 0; x < width; ++x) {
                    // Separate channels
                    Arrays.fill(red, 0);
                    Arrays.fill(green, 0);
                    Arrays.fill(blue, 0);

                    for (int i = 0; i < kernelSize; ++i) {
                        for (int j = 0; j < kernelSize; ++j) {
                            int pixel = processedPixels[i * paddedWidth + x + j];

                            red[(pixel & 0x00FF0000) >> 16]++;
                            green[(pixel & 0x0000FF00) >> 8]++;
                            blue[(pixel & 0x000000FF)]++;
                        }
                    }

                    // Put median in result
                    resultPixels[x] = (0xFF << 24)
                            | (medianOfHistogram(red) << 16)
                            | (medianOfHistogram(green) << 8)
                            | medianOfHistogram(blue);
                }

                processedBitmap.setPixels(resultPixels, 0, width, 0, y, width, 1);
            }
        });

        return processedBitmap;
    }

    private int medianOfHistogram(int[] histogram) {
        int cumulative = 0;
        int i;
        for (i = 0; i < 256; ++i) {
            cumulative += histogram[i];
            if (cumulative >= kernelSizeSq / 2) {
                break;
            }
        }
        return i;
    }
}
