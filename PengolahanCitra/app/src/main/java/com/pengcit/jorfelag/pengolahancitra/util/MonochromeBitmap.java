package com.pengcit.jorfelag.pengolahancitra.util;

import android.graphics.Bitmap;

public class MonochromeBitmap {

    private static final int THRESHOLD = 127;
    public static final int WHITE = 255;
    public static final int BLACK = 0;

    private static final int[] X_TRANSLATOR = {1, 1, 0, -1, -1, -1, 0, 1};
    private static final int[] Y_TRANSLATOR = {0, 1, 1, 1, 0, -1, -1, -1};

    /**
     * Create a black and white (monochrome) Bitmap from the given Bitmap.
     * @param bitmap The bitmap to be changed to black and white (monochrome).
     * @return The black and white (monochrome) bitmap.
     */
    public static Bitmap createMonochromeBitmap (Bitmap bitmap) {
        final Bitmap processedBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        final int width = processedBitmap.getWidth();
        final int height = processedBitmap.getHeight();

        Parallel.For(0, height, new LoopBody<Integer>() {
            @Override
            public void run(Integer i) {
                int[] processedPixels = new int[width];
                processedBitmap.getPixels(processedPixels, 0, width, 0, i, width, 1);

                for (int j = 0; j < width; ++j) {
                    int pixelColor = processedPixels[j];

                    int red = (pixelColor & 0x00FF0000) >> 16;
                    int green = (pixelColor & 0x0000FF00) >> 8;
                    int blue = (pixelColor & 0x000000FF);
                    int gray = (int) (0.2989 * red + 0.5870 * green + 0.1140 * blue);

                    if (gray > THRESHOLD)
                        gray = WHITE;
                    else
                        gray = BLACK;

                    processedPixels[j] = (0xFF<<24) | (gray << 16) | (gray << 8) | gray;
                }

                processedBitmap.setPixels(processedPixels, 0, width, 0, i, width, 1);
            }
        });

        return processedBitmap;
    }

    /**
     * Remove an object in the image given the starting position using anti-flooding.
     * @param monochromeBitmap The image in black and white (monochrome).
     * @param x The starting pixel row.
     * @param y The starting pixel column.
     */
    public static void removeObject(Bitmap monochromeBitmap, int x, int y) {
        monochromeBitmap.setPixel(x, y, (0xFF<<24) | (WHITE << 16) | (WHITE << 8) | WHITE);

        // Check neighbours in 8 directions.
        int next_x;
        int next_y;
        int next_pixel_color;
        for (int i = 0; i < 8; i++) {
            next_x = x + X_TRANSLATOR[i];
            next_y = y + Y_TRANSLATOR[i];
            try {
                next_pixel_color = monochromeBitmap.getPixel(next_x, next_y) & 0x000000FF;
                if (next_pixel_color == BLACK) {
                    removeObject(monochromeBitmap, next_x, next_y);
                }
            } catch (IllegalArgumentException e) {
                // do nothing
            }
        }
    }
}
