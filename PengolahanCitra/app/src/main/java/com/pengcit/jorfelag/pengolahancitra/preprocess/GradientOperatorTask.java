package com.pengcit.jorfelag.pengolahancitra.preprocess;

import android.graphics.Bitmap;

import com.pengcit.jorfelag.pengolahancitra.util.LoopBody;
import com.pengcit.jorfelag.pengolahancitra.util.Parallel;

import java.util.Arrays;

public class GradientOperatorTask extends BaseFilterTask {

    private final static int SOBEL = 0;
    private final static int SCHARR = 1;

    private final static double SQRT_2 = Math.sqrt(2);

    private final static double[][][][] KERNEL = {
            {   // SOBEL
                    {   // X
                            {1, 0, -1},
                            {2, 0, -2},
                            {1, 0, -1}
                    },
                    {   // Y
                            { 1,  2,  1},
                            { 0,  0,  0},
                            {-1, -2, -1}
                    }
            },
            {   // SCHARR
                    {   // X
                            {3,  0,  -3},
                            {10, 0, -10},
                            {3,  0,  -3}
                    },
                    {   // Y
                            { 3,  10,  3},
                            { 0,   0,  0},
                            {-3, -10, -3}
                    }
            },
            {   // PREWITT
                    {   // X
                            {1, 0, -1},
                            {1, 0, -1},
                            {1, 0, -1}
                    },
                    {   // Y
                            { 1,  1,  1},
                            { 0,  0,  0},
                            {-1, -1, -1}
                    }
            },
            {   // ROBERTS (Padded)
                    {   // X
                            {1,  0, 0},
                            {0, -1, 0},
                            {0,  0, 0}
                    },
                    {   // Y
                            { 0, 1, 0},
                            {-1, 0, 0},
                            { 0, 0, 0}
                    }
            },
            {   // FREI-CHEN
                    {   // G1
                            { 1,  SQRT_2,  1},
                            { 0,       0,  0},
                            {-1, -SQRT_2, -1}
                    },
                    {   // G2
                            {     1, 0,      -1},
                            {SQRT_2, 0, -SQRT_2},
                            {     1, 0,      -1}
                    },
                    {   // G3
                            {      0, -1, SQRT_2},
                            {      1,  0,     -1},
                            {-SQRT_2,  1,      0}
                    },
                    {   // G4
                            {SQRT_2, -1,       0},
                            {    -1,  0,       1},
                            {     0,  1, -SQRT_2}
                    },
                    {   // G5
                            { 0,   1/6,  0},
                            {-1/6, 0,   -1/6},
                            { 0,   1/6,  0}
                    },
                    {   // G6
                            {-1/6, 0,  1/6},
                            {   0, 0,    0},
                            { 1/6, 0, -1/6}
                    },
                    {   // G7
                            { 1/6, -2/6,  1/6},
                            {-2/6,  4/6, -2/6},
                            { 1/6, -2/6,  1/6}
                    },
                    {   // G8
                            {-2, 1, -2},
                            { 1, 4,  1},
                            {-2, 1, -2}
                    },
                    {   // G9
                            {1/9, 1/9, 1/9},
                            {1/9, 1/9, 1/9},
                            {1/9, 1/9, 1/9}
                    }

            }
    };

    private final static int RED = 2;
    private final static int GREEN = 1;
    private final static int BLUE = 0;

    private int kernel;
    private int kernelSize;
    private int numOfKernels;
    private int offset;

    public GradientOperatorTask(PreprocessOperatorFragment fr, int kernel) {
        super(fr);

        this.kernel = kernel;
        this.kernelSize = 3;
        this.numOfKernels = KERNEL[kernel].length;
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
                double[][] gDir = new double[numOfKernels][];
                for (int k = 0; k < numOfKernels; ++k) {
                    gDir[k] = new double[3];
                }
                int[] s = new int[3];

                // Traverse width
                for (int x = 0; x < width; ++x) {
                    // Convolve
                    for (int k = 0; k < numOfKernels; ++k) {
                        Arrays.fill(gDir[k], 0);
                    }

                    for (int i = 0; i < kernelSize; ++i) {
                        for (int j = 0; j < kernelSize; ++j) {
                            pixel[RED] = (processedPixels[i * paddedWidth + x + j] & 0x00FF0000) >> 16;
                            pixel[GREEN] = (processedPixels[i * paddedWidth + x + j] & 0x0000FF00) >> 8;
                            pixel[BLUE] = (processedPixels[i * paddedWidth + x + j] & 0x000000FF);

                            for (int c = 0; c < 3; ++c) {
                                for (int k = 0; k < numOfKernels; ++k) {
                                    gDir[k][c] += KERNEL[kernel][k][i][j] * pixel[c];
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
