package com.pengcit.jorfelag.pengolahancitra.preprocess;

import android.graphics.Bitmap;

import com.pengcit.jorfelag.pengolahancitra.util.LoopBody;
import com.pengcit.jorfelag.pengolahancitra.util.Parallel;

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

                // Init histogram
                final int[] red = new int[256];
                final int[] green = new int[256];
                final int[] blue = new int[256];

                int pixel;
                for (int i = 0; i < kernelSize; ++i) {
                    for (int j = 0; j < kernelSize; ++j) {
                        pixel = processedPixels[i * paddedWidth + j];
                        red[(pixel & 0x00FF0000) >> 16]++;
                        green[(pixel & 0x0000FF00) >> 8]++;
                        blue[(pixel & 0x000000FF)]++;
                    }
                }

                int[][] cache = new int[kernelSize][];
                for (int i = 0; i < kernelSize; ++i) {
                    cache[i] = new int[kernelSize];
                    for (int j = 0; j < kernelSize; j++) {
                        cache[i][j] = processedPixels[j * paddedWidth + i];
                    }
                }

                // Traverse width
                for (int x = 0; x < width; ++x) {
                    // Put median in result
                    resultPixels[x] = (0xFF << 24)
                            | (medianOfHistogram(red) << 16)
                            | (medianOfHistogram(green) << 8)
                            | medianOfHistogram(blue);

                    // Slide histogram
                    if (x == width - 1) {
                        break;
                    }

                    // Slide histogram
                    for (int i = 0; i < kernelSize; ++i) {
                        pixel = cache[x % kernelSize][i];
                        red[(pixel & 0x00FF0000) >> 16]--;
                        green[(pixel & 0x0000FF00) >> 8]--;
                        blue[(pixel & 0x000000FF)]--;
                    }
                    for (int i = 0; i < kernelSize; ++i) {
                        pixel = processedPixels[i * paddedWidth + x + kernelSize];
                        red[(pixel & 0x00FF0000) >> 16]++;
                        green[(pixel & 0x0000FF00) >> 8]++;
                        blue[(pixel & 0x000000FF)]++;
                        cache[x % kernelSize][i] = pixel;
                    }
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
