package com.pengcit.jorfelag.pengolahancitra.preprocess;

import android.graphics.Bitmap;
import android.util.Log;

import com.pengcit.jorfelag.pengolahancitra.util.LoopBody;
import com.pengcit.jorfelag.pengolahancitra.util.Parallel;

import java.util.Arrays;

public class SingleKernelTask extends BaseFilterTask {

    private final static int RED = 2;
    private final static int GREEN = 1;
    private final static int BLUE = 0;

    private double[][] kernel;

    public SingleKernelTask(PreprocessOperatorFragment fr, double[][] kernel) {
        super(fr);
        Log.d("KERNEL", Arrays.deepToString(kernel));

        this.kernel = kernel;
        this.kernelSize = kernel.length;
        this.offset = (kernelSize - 1) / 2;
    }

    @Override
    protected Bitmap doInBackground(Bitmap... bitmaps) {
        final Bitmap originalBitmap = resizeBitmap(bitmaps[0]);

        final int height = originalBitmap.getHeight();
        final int width = originalBitmap.getWidth();
        final int paddedWidth = width + 2 * offset;

        final Bitmap processedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        final int[] processedPixels = getProcessedPixels(originalBitmap);

        Parallel.For(0, height, new LoopBody<Integer>() {
            @Override
            public void run(Integer y) {
                // Placeholder
                final int[] resultPixels = new int[width];
                int[] pixel = new int[3];
                int[] g = new int[3];
                int[] s = new int[3];

                // Traverse width
                for (int x = 0; x < width; ++x) {
                    // Convolve
                    Arrays.fill(g, 0);

                    for (int i = 0; i < kernelSize; ++i) {
                        for (int j = 0; j < kernelSize; ++j) {
                            pixel[RED] = (processedPixels[(y + i) * paddedWidth + x + j] & 0x00FF0000) >> 16;
                            pixel[GREEN] = (processedPixels[(y + i) * paddedWidth + x + j] & 0x0000FF00) >> 8;
                            pixel[BLUE] = (processedPixels[(y + i) * paddedWidth + x + j] & 0x000000FF);

                            for (int c = 0; c < 3; ++c) {
                                g[c] += kernel[i][j] * pixel[c];
                            }
                        }
                    }

                    for (int c = 0; c < 3; ++c) {
                        s[c] = Math.max(0, Math.min(255, g[c]));
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
