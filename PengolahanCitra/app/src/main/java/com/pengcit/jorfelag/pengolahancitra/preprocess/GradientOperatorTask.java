package com.pengcit.jorfelag.pengolahancitra.preprocess;

import android.graphics.Bitmap;

import com.pengcit.jorfelag.pengolahancitra.util.LoopBody;
import com.pengcit.jorfelag.pengolahancitra.util.Parallel;

import java.util.Arrays;

public class GradientOperatorTask extends BaseFilterTask {

    private final static int SOBEL = 0;
    private final static int SCHARR = 1;

    private final static int[][] SOBEL_X = {
            {1, 0, -1},
            {2, 0, -2},
            {1, 0, -1}
    };
    private final static int[][] SOBEL_Y = {
            { 1,  2,  1},
            { 0,  0,  0},
            {-1, -2, -1}
    };

    private final static int[][] SCHARR_X = {
            {3,  0,  -3},
            {10, 0, -10},
            {3,  0,  -3}
    };
    private final static int[][] SCHARR_Y = {
            { 3,  10,  3},
            { 0,   0,  0},
            {-3, -10, -3}
    };

    private final static int RED = 2;
    private final static int GREEN = 1;
    private final static int BLUE = 0;

    private int kernel;
    private int kernelSize;
    private int offset;

    public GradientOperatorTask(PreprocessOperatorFragment fr, int kernel) {
        super(fr);

        this.kernel = kernel;
        this.kernelSize = 3;
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
                int[] pixel = new int[3];
                int[] sx = new int[3];
                int[] sy = new int[3];
                int[] s = new int[3];

                // Traverse width
                for (int x = 0; x < width; ++x) {
                    // Convolve
                    Arrays.fill(sx, 0);
                    Arrays.fill(sy, 0);

                    for (int i = 0; i < kernelSize; ++i) {
                        for (int j = 0; j < kernelSize; ++j) {
                            pixel[RED] = (processedPixels[i * paddedWidth + x + j] & 0x00FF0000) >> 16;
                            pixel[GREEN] = (processedPixels[i * paddedWidth + x + j] & 0x0000FF00) >> 8;
                            pixel[BLUE] = (processedPixels[i * paddedWidth + x + j] & 0x000000FF);

                            for (int c = 0; c < 3; ++c) {
                                switch (kernel) {
                                    case SCHARR:
                                        sx[c] += SCHARR_X[i][j] * pixel[c];
                                        sy[c] += SCHARR_Y[i][j] * pixel[c];
                                        break;
                                    default:    // SOBEL
                                        sx[c] += SOBEL_X[i][j] * pixel[c];
                                        sy[c] += SOBEL_Y[i][j] * pixel[c];
                                        break;
                                }
                            }
                        }
                    }

                    for (int c = 0; c < 3; ++c) {
                        s[c] = Math.min(255, (int) Math.sqrt(sx[c] * sx[c] + sy[c] * sy[c]));
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
