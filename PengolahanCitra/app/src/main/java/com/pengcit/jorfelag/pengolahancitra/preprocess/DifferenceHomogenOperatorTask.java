package com.pengcit.jorfelag.pengolahancitra.preprocess;

import android.graphics.Bitmap;

import com.pengcit.jorfelag.pengolahancitra.util.LoopBody;
import com.pengcit.jorfelag.pengolahancitra.util.Parallel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class DifferenceHomogenOperatorTask extends BaseFilterTask {

    private final static int[] X_TRANSLATION = {0, 1, 1, 1, 0, -1, -1, -1};
    private final static int[] Y_TRANSLATION = {-1, -1, 0, 1, 1, 1, 0, -1};

    private final static int RED = 2;
    private final static int GREEN = 1;
    private final static int BLUE = 0;

    public DifferenceHomogenOperatorTask(PreprocessOperatorFragment fr) {
        super(fr);
    }

    @Override
    protected Bitmap doInBackground(Bitmap... args) {
        final Bitmap originalBitmap = args[0].copy(Bitmap.Config.ARGB_8888, false);

        final int height = originalBitmap.getHeight();
        final int width = originalBitmap.getWidth();

        final Bitmap processedBitmap = args[0].copy(Bitmap.Config.ARGB_8888, true);

        Parallel.For(0, height, new LoopBody<Integer>() {
            @Override
            public void run(Integer y) {
                // Placeholder
                int[] curr_pixel = new int[3];
                int[] neighbour_pixel = new int[3];
                int pixel;

                // Traverse width
                for (int x = 0; x < width; ++x) {
                    pixel = originalBitmap.getPixel(x, y);
                    curr_pixel[RED] = (pixel & 0x00FF0000) >> 16;
                    curr_pixel[GREEN] = (pixel & 0x0000FF00) >> 8;
                    curr_pixel[BLUE] = (pixel & 0x000000FF);
                    int[] max = new int[3];
                    int neighbour_x;
                    int neighbour_y;

                    for (int i = 0; i < 8; i++) {
                        neighbour_x = x + X_TRANSLATION[i];
                        neighbour_y = y + Y_TRANSLATION[i];
                        try {
                            pixel = originalBitmap.getPixel(neighbour_x, neighbour_y);
                            neighbour_pixel[RED] = (pixel & 0x00FF0000) >> 16;
                            neighbour_pixel[GREEN] = (pixel & 0x0000FF00) >> 8;
                            neighbour_pixel[BLUE] = (pixel & 0x000000FF);

                            for (int c = 0; c < 3; c++) {
                                max[c] = Math.max(max[c], Math.abs(curr_pixel[c] - neighbour_pixel[c]));
                            }
                        } catch (IllegalArgumentException e) {

                        }
                    }

                    int newColor = (0xFF << 24) | (max[RED] << 16) | (max[GREEN] << 8) | max[BLUE];
                    processedBitmap.setPixel(x, y, newColor);
                }
            }
        });

        return processedBitmap;
    }
}
