package com.pengcit.jorfelag.pengolahancitra.face_detection;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.Arrays;

public class Sobel {

    private final static float[] SOBEL_X = {-1, 0, 1, -2, 0, 2, -1, 0, 1};
    private final static float[] SOBEL_Y = {-1, -2, -1, 0, 0, 0, 1, 2, 1};

    private int width;
    private int height;
    private Bitmap originalBitmap;
    private int[] pixels;

    Sobel(Bitmap bitmap) {
        originalBitmap = bitmap.copy(bitmap.getConfig(), true);
        width = originalBitmap.getWidth();
        height = originalBitmap.getHeight();
        pixels = new int[height * width];
        originalBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
    }

    Bitmap process() {
        int[] resultPixels = new int[pixels.length];
        Arrays.fill(resultPixels, Color.WHITE);
        int maxGradient = -1;

        for (int top = 0, middle = width, bottom = width * 2; bottom < pixels.length; top++, middle++, bottom++) {
            if (top % width == 0 || top % width == (width - 1)) {
                continue;
            }

            int gX = 0;
            int gY = 0;

            int[] idx = new int[] {
                    top - 1, top, top + 1,
                    middle - 1, middle, middle + 1,
                    bottom - 1, bottom, bottom + 1
            };

            for (int j = 0; j < idx.length; ++j) {
                int r = (pixels[idx[j]] & 0x00FF0000) >> 16;
                int g = (pixels[idx[j]] & 0x0000FF00) >> 8;
                int b = (pixels[idx[j]] & 0x000000FF);
                int gray = (int)(0.2126 * r + 0.7152 * g + 0.0722 * b);

                gX += SOBEL_X[j] * gray;
                gY += SOBEL_Y[j] * gray;

            }

            int gradient = (int) (Math.sqrt(gX * gX + gY * gY)) > 70 ? 255 : 0;

            resultPixels[middle] = (0xFF << 24)
                    | (gradient << 16)
                    | (gradient << 8)
                    | gradient;
        }

        return Bitmap.createBitmap(resultPixels, width, height, originalBitmap.getConfig());
    }
}
