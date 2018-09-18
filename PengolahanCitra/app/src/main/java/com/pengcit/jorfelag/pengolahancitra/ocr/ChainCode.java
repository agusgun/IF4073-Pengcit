package com.pengcit.jorfelag.pengolahancitra.ocr;

import android.graphics.Bitmap;
import android.util.Log;

import static com.pengcit.jorfelag.pengolahancitra.util.MonochromeBitmap.BLACK;
import static com.pengcit.jorfelag.pengolahancitra.util.MonochromeBitmap.WHITE;

public class ChainCode {

    private String label;
    private String code;
    private double[] histogram;

    /**
     * Directions:
     * |5|6|7|
     * |4| |0|
     * |3|2|1|
     */
    private static final int[] X_TRANSLATOR = {1, 1, 0, -1, -1, -1, 0, 1};
    private static final int[] Y_TRANSLATOR = {0, 1, 1, 1, 0, -1, -1, -1};


    /**
     * Create the chain code for the image based on the starting position.
     * @param image The image in monochrome color (black and white).
     * @param x The starting row position.
     * @param y The starting column position.
     * @param label The label for the created chain code.
     */
    public ChainCode(Bitmap image, int x, int y, String label) {
        this.label = label;
        histogram = new double[8];

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
                            histogram[dir] += 1;
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

       for (i = 0; i < 8; i++) {
            histogram[i] /= code.length();
        }
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

    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Calculate dissimilarity of c1 to c2.
     * @param c1 The chain code to compare.
     * @param c2 The chain code to be compared.
     * @return Dissimilarity value.
     */
    public static double calculateDissimilarity(ChainCode c1, ChainCode c2) {
        double dissimilarity = 0;
        for (int i = 0; i < 8; i++) {
            dissimilarity += (Math.abs(c2.histogram[i] - c1.histogram[i]));
        }
        return dissimilarity;
    }
}
