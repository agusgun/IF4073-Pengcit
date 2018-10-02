package com.pengcit.jorfelag.pengolahancitra.image_skeletonization;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.util.Log;
import android.util.MutableBoolean;

import com.pengcit.jorfelag.pengolahancitra.util.LoopBody;
import com.pengcit.jorfelag.pengolahancitra.util.Parallel;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ImageSkeletonizer {

    private final static int[] X_TRANSLATION = {0, 1, 1, 1, 0, -1, -1, -1};
    private final static int[] Y_TRANSLATION = {-1, -1, 0, 1, 1, 1, 0, -1};

    private Bitmap bitmap;
    private int[][] imageMatrix;
    private int threshold;
    private Queue<Point> blackPixels;

    public ImageSkeletonizer(Bitmap bitmap, int threshold) {
        this.bitmap = bitmap.copy(bitmap.getConfig(), true);
        this.threshold = threshold;
        blackPixels = new ConcurrentLinkedQueue<>();
        this.imageMatrix = convertToGrayMatrix();
    }

    public void process() {
        final MutableBoolean firstStep = new MutableBoolean(false);
        final MutableBoolean hasChanged = new MutableBoolean(false);

        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();

        Queue<Point> toClear = new LinkedList<>();
        Queue<Point> step2 = new LinkedList<>();
        Queue<Point> temp;
        Point p;

        do {
            firstStep.value = !firstStep.value;
            if (firstStep.value) {
                hasChanged.value = false;
            }

            if (firstStep.value) {
                temp = blackPixels;
            } else {
                temp = step2;
            }

            while (!temp.isEmpty()) {
                p = temp.remove();
                int[] neighbors = getNeighbors(p.x, p.y);
                int blackNeighbors = countBlackNeighbors(neighbors);
                if (blackNeighbors >= 2 && blackNeighbors <= 6 && getTransitions(neighbors) == 1 && atLeastOneIsWhite(neighbors, firstStep.value)) {
                    toClear.add(p);
                    hasChanged.value = true;
                } else {
                    if (firstStep.value) {
                        step2.add(p);
                    } else {
                        blackPixels.add(p);
                    }
                }
            }

            while(!toClear.isEmpty()) {
                p = toClear.remove();
                imageMatrix[p.y][p.x] = 255;
                bitmap.setPixel(p.x, p.y, Color.WHITE);
            }

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
            try {
                neighbors[i] = imageMatrix[y + Y_TRANSLATION[i]][x + X_TRANSLATION[i]];
            } catch (ArrayIndexOutOfBoundsException ignored) {
            }
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
            if (neighbors[i] > threshold && neighbors[(i + 1) % neighbors.length] <= threshold) {
                transitions++;
            }
        }
        return transitions;
    }

    private int countBlackNeighbors(int[] neighbors) {
        int count = 0;
        for (int neighbor: neighbors) {
            if (neighbor <= threshold) {
                count++;
            }
        }
        return count;
    }

    private boolean atLeastOneIsWhite(int[] neighbors, boolean firstStep) {
        boolean P2 = neighbors[0] > threshold;
        boolean P4 = neighbors[2] > threshold;
        boolean P6 = neighbors[4] > threshold;
        boolean P8 = neighbors[6] > threshold;

        if (firstStep) {
            return (P2 || P4 || P6) && (P4 || P6 || P8);
        } else {
            return (P2 || P4 || P8) && (P2 || P6 || P8);
        }
    }

    private int[][] convertToGrayMatrix() {
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();

        final int[][] imageMatrix = new int[height][];

        Parallel.For(0, height, new LoopBody<Integer>() {
            @Override
            public void run(Integer i) {
                imageMatrix[i] = new int[width];

                int[] processedPixels = new int[width];
                bitmap.getPixels(processedPixels, 0, width, 0, i, width, 1);

                for (int j = 0; j < width; ++j) {
                    int pixel = processedPixels[j];
                    if(getGrayLevel(pixel) > threshold) {
                        imageMatrix[i][j] = 255;
                        bitmap.setPixel(j, i, Color.WHITE);
                    } else {
                        imageMatrix[i][j] = 0;
                        bitmap.setPixel(j, i, Color.BLACK);
                        blackPixels.add(new Point(j, i));
                    }
                }
            }
        });

        return imageMatrix;
    }

    private static int getGrayLevel(int pixel) {
        int red = (pixel & 0x00FF0000) >> 16;
        int green = (pixel & 0x0000FF00) >> 8;
        int blue = (pixel & 0x000000FF);

        return (int) (0.2989 * red + 0.5870 * green + 0.1140 * blue);
    }
}
