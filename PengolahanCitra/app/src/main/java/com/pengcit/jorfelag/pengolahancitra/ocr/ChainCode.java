package com.pengcit.jorfelag.pengolahancitra.ocr;

import android.graphics.Bitmap;
import android.util.Log;

import static com.pengcit.jorfelag.pengolahancitra.util.MonochromeBitmap.BLACK;
import static com.pengcit.jorfelag.pengolahancitra.util.MonochromeBitmap.WHITE;

public class ChainCode {

    private String label;
    private String code;

    /**
     * Directions:
     * |5|6|7|
     * |4| |0|
     * |3|2|1|
     */
    private static final int[] X_TRANSLATOR = {1, 1, 0, -1, -1, -1, 0, 1};
    private static final int[] Y_TRANSLATOR = {0, 1, 1, 1, 0, -1, -1, -1};

    //First Pixel
    int begin[];

    /**
     * Create the chain code for the image based on the starting position.
     * @param image The image in monochrome color (black and white).
     * @param label The label for the created chain code.
     */
    public ChainCode(Bitmap image, String label) {
        this.label = label;

        begin = getFirstPixel(image);

        Log.d("getFirstPixel", Integer.toString(begin[0]));
        Log.d("getFirstPixel", Integer.toString(begin[1]));

        int x = begin[0];
        int y = begin[1];

        int start_x = x;
        int start_y = y;
        int prev_x = -1;
        int prev_y = -1;
        int i;
        int dir = 0;

        StringBuilder stringBuilder = new StringBuilder();
        do {
            i = 0;
            int next_x;
            int next_y;
            int next_pixel_color;
            int next_dir;
            boolean found = false;
            do {
                next_dir = (dir + i) % 8;
                next_x = x + X_TRANSLATOR[next_dir];
                next_y = y + Y_TRANSLATOR[next_dir];
                if (next_x != prev_x || next_y != prev_y) {
                    try {
                        next_pixel_color = image.getPixel(next_x, next_y) & 0x000000FF;
                        if (next_pixel_color == BLACK && IsBorder(image, next_x, next_y)) {
                            dir = next_dir;
                            stringBuilder.append(dir);
                            Log.d("HEHEHE", "HEHEHE");
                            prev_x = x;
                            prev_y = y;
                            x = next_x;
                            y = next_y;
                            found = true;
                        } else {
                            i++;
                        }
                    } catch (IllegalArgumentException e) {
                        i++;
                    }
                } else {
                    i++;
                }
            } while (!found && i < 8);
        } while (i < 8 && (x != start_x || y != start_y));

        code = stringBuilder.toString();
        Log.d("ChainCode", code);
    }

    /**
     * Check if the given pixel is a border or not.
     * @param image The image that contains the pixel.
     * @param x The pixel row position.
     * @param y The given column position.
     * @return True if the given pixel is a border else false.
     */
    private boolean IsBorder(Bitmap image, int x, int y) {
        int next_x;
        int next_y;
        int next_pixel_color;
        int i = 0;
        while (i < 8) {
            next_x = x + X_TRANSLATOR[i];
            next_y = y + Y_TRANSLATOR[i];
            try {
                next_pixel_color = image.getPixel(next_x, next_y) & 0x000000FF;
                if (next_pixel_color == WHITE) {
                    return true;
                }
            } catch (IllegalArgumentException e) {
                return true;
            }
            i += 2;
        }
        return false;
    }

    public String getLabel() {
        return label;
    }

    public String getCode() {
        return code;
    }

    private int[] getFirstPixel(Bitmap imageBitmap) {
        final Bitmap processedBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);
        final int width = processedBitmap.getWidth();
        final int height = processedBitmap.getHeight();

        //x,y
        int result[] = new int[2];

        //Don't Parallalize
        int i = 0;
        int j = 0;
        boolean flag = false;
        while (i < height && !flag) {
            j = 0;
            while (j < width && !flag) {
                int pixelColor = processedBitmap.getPixel(j, i);

                int red = (pixelColor & 0x00FF0000) >> 16;
                int green = (pixelColor & 0x0000FF00) >> 8;
                int blue = (pixelColor & 0x000000FF);
                int gray = (red + green + blue) / 3;

                // <= 128 then black
                if (gray <= 128) {
                    Log.d("x,y", Integer.toString(j) + " " + Integer.toString(i));
                    result[0] = j;
                    result[1] = i;
                    flag = true;
                }
                j++;
            }
            i++;
        }
        return result;
    }
}
