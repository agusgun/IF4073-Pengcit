package com.pengcit.jorfelag.pengolahancitra.preprocess;

import android.graphics.Bitmap;

import com.pengcit.jorfelag.pengolahancitra.util.LoopBody;
import com.pengcit.jorfelag.pengolahancitra.util.Median;
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
                int[][] processedPixels = new int[kernelSize][];
                for (int i = 0; i < kernelSize; ++i) {
                    processedPixels[i] = new int[width + 2 * offset];
                }

                // Get anchor pixels
                originalBitmap.getPixels(processedPixels[offset], offset, width, 0, y, width, 1);
                for (int i = offset - 1; i >= 0; --i) {
                    processedPixels[offset][i] = processedPixels[offset][offset];
                }
                for (int i = width + offset; i < width + 2 * offset; ++i) {
                    processedPixels[offset][i] = processedPixels[offset][width + offset - 1];
                }

                // Get neighbor pixels
                for (int i = offset - 1; i >= 0; --i) {
                    int yOffset = offset - i;
                    try {
                        originalBitmap.getPixels(processedPixels[i], offset, width, 0, y - yOffset, width, 1);
                    } catch (IllegalArgumentException ignored) {
                        // At border, duplicate border pixels
                        System.arraycopy(processedPixels[i + 1], 0, processedPixels[i], 0, width + 2 * offset);
                    }
                }
                for (int i = offset + 1; i < kernelSize; ++i) {
                    int yOffset = i - offset;
                    try {
                        originalBitmap.getPixels(processedPixels[i], offset, width, 0, y - yOffset, width, 1);
                    } catch (IllegalArgumentException ignored) {
                        // At border, duplicate border pixels
                        System.arraycopy(processedPixels[i + 1], 0, processedPixels[i], 0, width + 2 * offset);
                    }
                }

                // Placeholders
                int[] resultPixels = new int[width];

                // Traverse width
                for (int x = 0; x < width; ++x) {
                    // Separate channels
                    int[] red = new int[kernelSizeSq];
                    int[] green = new int[kernelSizeSq];
                    int[] blue = new int[kernelSizeSq];

                    for (int i = 0; i < kernelSize; ++i) {
                        for (int j = 0; j < kernelSize; ++j) {
                            int pixel = processedPixels[i][x + j];
                            int k = i * kernelSize + j;

                            red[k] = (pixel & 0x00FF0000) >> 16;
                            green[k] = (pixel & 0x0000FF00) >> 8;
                            blue[k] = (pixel & 0x000000FF);
                        }
                    }

                    // Put median in result
                    resultPixels[x] = (0xFF << 24)
                            | (Median.findMedian(red) << 16)
                            | (Median.findMedian(green) << 8)
                            | Median.findMedian(blue);
                }

                processedBitmap.setPixels(resultPixels, 0, width, 0, y, width, 1);
            }
        });

        return processedBitmap;
    }
}
