package com.pengcit.jorfelag.pengolahancitra.image_skeletonization;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.util.MutableBoolean;

import com.pengcit.jorfelag.pengolahancitra.util.LoopBody;
import com.pengcit.jorfelag.pengolahancitra.util.Parallel;

import java.util.ArrayList;

public class ImageSkeletonizer {

    private final static int[] X_TRANSLATION = {0, 1, 1, 1, 0, -1, -1, -1};
    private final static int[] Y_TRANSLATION = {-1, -1, 0, 1, 1, 1, 0, -1};

    private final static int THRESHOLD = 127;

    private Bitmap bitmap;
    private int[][] imageMatrix;

    public ImageSkeletonizer(Bitmap bitmap) {
        this.bitmap = bitmap.copy(bitmap.getConfig(), true);
        this.imageMatrix = convertToGrayMatrix(bitmap);
    }

    public void process() {
        final MutableBoolean firstStep = new MutableBoolean(false);
        final MutableBoolean hasChanged = new MutableBoolean(false);

        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();

        final ArrayList<Point> toClear = new ArrayList<>();

        do {
            hasChanged.value = false;
            firstStep.value = !firstStep.value;

            Parallel.For(0, height, new LoopBody<Integer>() {
                @Override
                public void run(Integer y) {
                    for (int x = 0; x < width; ++x) {
                        if (imageMatrix[y][x] > THRESHOLD)
                            continue;

                        int[] neighbors = getNeighbors(x, y);
                        int blackNeighbors = countBlackNeighbors(neighbors);

                        if (blackNeighbors < 2 || blackNeighbors > 6)
                            continue;

                        if (getTransitions(neighbors) != 1)
                            continue;

                        if (!atLeastOneIsWhite(neighbors, firstStep.value))
                            continue;

                        toClear.add(new Point(x, y));
                        hasChanged.value = true;
                    }
                }
            });

            for (Point p: toClear) {
                bitmap.setPixel(p.x, p.y, Color.WHITE);
                imageMatrix[p.y][p.x] = 255;
            }
            toClear.clear();
        } while (firstStep.value || hasChanged.value);
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    /**
     * Gets the neighbors of a pixel, represented as a grayscale pixel value [0, 255]
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return Neighbors of a pixel
     */
    private int[] getNeighbors(int x, int y) {
        int[] neighbors = new int[8];
        for (int i = 0; i < 8; ++i) {
            neighbors[i] = imageMatrix[y + Y_TRANSLATION[i]][x + X_TRANSLATION[i]];
        }
        return neighbors;
    }

    /**
     * Count the number of transitions from white to black
     * in a sequence of P2, P3, P4, P5, P6, P7, P8, P9, P2
     *
     * @param neighbors List of neighbors
     * @return Number of transitions
     */
    private int getTransitions(int[] neighbors) {
        int transitions = 0;
        for (int i = 0; i < neighbors.length; ++i) {
            if (neighbors[i] > THRESHOLD && neighbors[(i + 1) % neighbors.length] <= THRESHOLD) {
                transitions++;
            }
        }
        return transitions;
    }

    private int countBlackNeighbors(int[] neighbors) {
        int count = 0;
        for (int neighbor: neighbors) {
            if (neighbor <= THRESHOLD) {
                count++;
            }
        }
        return count;
    }

    private boolean atLeastOneIsWhite(int[] neighbors, boolean firstStep) {
        boolean P2 = neighbors[0] > THRESHOLD;
        boolean P4 = neighbors[2] > THRESHOLD;
        boolean P6 = neighbors[4] > THRESHOLD;
        boolean P8 = neighbors[6] > THRESHOLD;

        if (firstStep) {
            return (P2 || P4 || P6) && (P4 || P6 || P8);
        } else {
            return (P2 || P4 || P8) && (P2 || P6 || P8);
        }
    }

    public static int[][] convertToGrayMatrix(final Bitmap bitmap) {
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();

        final int[][] imageMatrix = new int[height][];

        Parallel.For(0, height, new LoopBody<Integer>() {
            @Override
            public void run(Integer i) {
                imageMatrix[i]  = new int[width];

                int[] processedPixels = new int[width];
                bitmap.getPixels(processedPixels, 0, width, 0, i, width, 1);

                for (int j = 0; j < width; ++j) {
                    int pixel = processedPixels[j];
                    imageMatrix[i][j] = getGrayLevel(pixel);
                }
            }
        });

        return imageMatrix;
    }

    public static int getGrayLevel(int pixel) {
        int red = (pixel & 0x00FF0000) >> 16;
        int green = (pixel & 0x0000FF00) >> 8;
        int blue = (pixel & 0x000000FF);

        return (red + green + blue) / 3;
    }
}
