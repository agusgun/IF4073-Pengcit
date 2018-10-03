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

    // template for acute angle emphasis
    private final static int[][][] ACUTE_ANGLE_TEMPLATE = {
            {
                {0, 0, 255, 0, 0}, {0, 0, 255, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {-1, 0, 0, 0, -1}
            },
            {
                {0, 255, 255, 0, 0}, {0, 0, 255, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {-1, 0, 0, 0, -1}
            },
            {
                {0, 0, 255, 255, 0}, {0, 0, 255, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {-1, 0, 0, 0, -1}
            },
            {
                {0, 255, 255, 0, 0}, {0, 255, 255, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {-1, 0, 0, 0, -1}
            },
            {
                {0, 0, 255, 255, 0}, {0, 0, 255, 255, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {-1, 0, 0, 0, -1}
            }
    };


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
        //Pre process
        smoothing();
        acuteAngleEmphasis();

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
                int transitions = getTransitions(neighbors);
                if (blackNeighbors >= 2 && blackNeighbors <= 6 && transitions == 1 && atLeastOneIsWhite(neighbors, firstStep.value)) {
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

        //post processing
        staircaseRemoval();
        pruning();
        extractGeometricProperty();
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
                neighbors[i] = 255;
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

    private void smoothing() {
        Queue<Point> blackPixels = new LinkedList<>(this.blackPixels);
        Point p;
        while (!blackPixels.isEmpty()) {
            p = blackPixels.remove();
            int[] neighbors = getNeighbors(p.x, p.y);
            int blackNeighbors = countBlackNeighbors(neighbors);
            int transitions = getTransitions(neighbors);
            if (blackNeighbors <= 2 && transitions < 2) {
                imageMatrix[p.y][p.x] = 255;
                bitmap.setPixel(p.x, p.y, Color.WHITE);
            }
        }
    }

    private void acuteAngleEmphasis() {
        Queue<Point> toClear = new LinkedList<>();
        for (int i = 0; i < bitmap.getHeight() - 5; i++) {
            for (int j = 0; j < bitmap.getWidth() - 5; j++) {
                for (int k = 0; k < 5; k++) {
                    if (matchTemplate(j, i, k)) {
                        toClear.add(new Point(j + 2, i + 2));
                    }
                }
            }
        }

        boolean deleted = !toClear.isEmpty();
        Point p;
        while(!toClear.isEmpty()) {
            p = toClear.remove();
            imageMatrix[p.y][p.x] = 255;
            bitmap.setPixel(p.x, p.y, Color.WHITE);
        }

        if (deleted) {
            for (int i = 0; i < bitmap.getHeight() - 5; i++) {
                for (int j = 0; j < bitmap.getWidth() - 5; j++) {
                    for (int k = 0; k < 3; k++) {
                        if (matchTemplate(j, i, k)) {
                            toClear.add(new Point(j + 2, i + 2));
                        }
                    }
                }
            }
        }

        deleted = !toClear.isEmpty();
        while(!toClear.isEmpty()) {
            p = toClear.remove();
            imageMatrix[p.y][p.x] = 255;
            bitmap.setPixel(p.x, p.y, Color.WHITE);
        }

        if (deleted) {
            for (int i = 0; i < bitmap.getHeight() - 5; i++) {
                for (int j = 0; j < bitmap.getWidth() - 5; j++) {
                    if (matchTemplate(j, i, 0)) {
                        toClear.add(new Point(j + 2, i + 2));
                    }
                }
            }
        }

        while(!toClear.isEmpty()) {
            p = toClear.remove();
            imageMatrix[p.y][p.x] = 255;
            bitmap.setPixel(p.x, p.y, Color.WHITE);
        }
    }

    private boolean matchTemplate(int start_x, int start_y, int template_id) {
        int i = 0;
        boolean matched1 = true;
        while (i < 5 && matched1) {
            int j = 0;
            while (j < 5 && matched1) {
                if (ACUTE_ANGLE_TEMPLATE[template_id][i][j] == -1) {
                    j++;
                    continue;
                }
                if (imageMatrix[start_y + i][start_x + j] != ACUTE_ANGLE_TEMPLATE[template_id][i][j]) {
                    matched1 = false;
                } else {
                    j++;
                }
            }
            if (matched1) {
                i++;
            }
        }

        i = 0;
        boolean matched2 = true;
        while (i < 5 && matched2) {
            int j = 0;
            while (j < 5 && matched2) {
                if (ACUTE_ANGLE_TEMPLATE[template_id][4 - i][j] == -1) {
                    j++;
                    continue;
                }
                if (imageMatrix[start_y + i][start_x + j] != ACUTE_ANGLE_TEMPLATE[template_id][4 - i][j]) {
                    matched2 = false;
                } else {
                    j++;
                }
            }
            if (matched2) {
                i++;
            }
        }
        return matched1 || matched2;
    }

    private void staircaseRemoval() {
        Queue<Point> blackPixels = new LinkedList<>(this.blackPixels);
        this.blackPixels = new LinkedList<>();
        Point p;
        while (!blackPixels.isEmpty()) {
            p = blackPixels.remove();
            try {
                boolean vN = imageMatrix[p.y - 1][p.x] == 0;
                boolean vE = imageMatrix[p.y][p.x + 1] == 0;
                boolean vNE = imageMatrix[p.y - 1][p.x + 1] == 0;
                boolean vSW = imageMatrix[p.y + 1][p.x - 1] == 0;
                boolean vW = imageMatrix[p.y][p.x - 1] == 0;
                boolean vS = imageMatrix[p.y + 1][p.x] == 0;
                boolean vNW = imageMatrix[p.y - 1][p.x - 1] == 0;
                boolean vSE = imageMatrix[p.y + 1][p.x + 1] == 0;

                if (!(vN && ((vE && !vNE && !vSW && (!vW || !vS) || (vW && !vNW && !vSE && (!vE || !vS))))) &&
                        !(vS && ((vE && !vNW && !vSE && (!vW || !vN) || (vW && !vNE && !vSW && (!vE || !vN)))))) {
                    this.blackPixels.add(p);
                } else {
                    imageMatrix[p.y][p.x] = 255;
                    bitmap.setPixel(p.x, p.y, Color.WHITE);
                }
            } catch (Exception e) {
                this.blackPixels.add(p);
            }
        }
    }

    private void extractGeometricProperty() {
        Queue<Point> blackPixels = new LinkedList<>(this.blackPixels);
        this.blackPixels = new LinkedList<>();
        Point p;
        int count = 0;
        while (!blackPixels.isEmpty()) {
            p = blackPixels.remove();
            int[] neighbors = getNeighbors(p.x, p.y);
            int blackNeighbors = countBlackNeighbors(neighbors);
            if (blackNeighbors == 1) {
                bitmap.setPixel(p.x, p.y, Color.GREEN);
                bitmap.setPixel(p.x + 1, p.y, Color.GREEN);
                bitmap.setPixel(p.x - 1, p.y, Color.GREEN);
                bitmap.setPixel(p.x, p.y + 1, Color.GREEN);
                bitmap.setPixel(p.x, p.y - 1, Color.GREEN);
                count++;
            } else if (blackNeighbors >= 3) {
                bitmap.setPixel(p.x, p.y, Color.RED);
                bitmap.setPixel(p.x + 1, p.y, Color.RED);
                bitmap.setPixel(p.x - 1, p.y, Color.RED);
                bitmap.setPixel(p.x, p.y + 1, Color.RED);
                bitmap.setPixel(p.x, p.y - 1, Color.RED);
            }
        }
    }

    private void pruning() {
        int distanceThreshold = 10;

        Queue<Point> intersectionPoints = new LinkedList<>();
        Queue<Point> endPoints = new LinkedList<>();

        Queue<Point> blackPixels = new LinkedList<>(this.blackPixels);
        this.blackPixels = new LinkedList<>();

        // First Add EndPoint and Intersection
        Point p;

        Queue<Point> tempBlackPixels = new LinkedList<>(blackPixels);
        while (!tempBlackPixels.isEmpty()) {
            p = tempBlackPixels.remove();
            int[] neighbors = getNeighbors(p.x, p.y);
            int blackNeighbors = countBlackNeighbors(neighbors);

            if (blackNeighbors == 1) {
                Log.d("ENDPOINTS", Integer.toString(p.x) + " "  + Integer.toString(p.y));
                endPoints.add(p);
            } else if (blackNeighbors >= 3) {
                Log.d("Intersection", Integer.toString(p.x) + " "  + Integer.toString(p.y));
                intersectionPoints.add(p);
            }
            Log.d("HEHEHEHE", Integer.toString(p.x) + " " + Integer.toString(p.y));
        }

        int counter = 0;
        boolean marker = true;
        while (marker) {
            counter++;
            Log.d("HEHEHEHE", Integer.toString(counter));
            int minDistance = 255;
            Point pEnd, pIntersect;
            while (!endPoints.isEmpty()) {
                pEnd = endPoints.remove();

                // Clone Intersection Point
                Queue<Point> tempIntersection = new LinkedList<>(intersectionPoints);

                // Count and Delete
                while (!tempIntersection.isEmpty()) {
                    pIntersect = tempIntersection.remove();
                    int distance = Math.abs(pIntersect.x - pEnd.x) + Math.abs(pIntersect.y - pEnd.y);
                    Log.d("Distance", Integer.toString(distance));
                    Log.d("Min Distance", Integer.toString(minDistance));
                    Log.d("Distance Threshold", Integer.toString(distanceThreshold));
                    if (minDistance > distance) {
                        minDistance = distance;
                    }
                    Log.d("Comparing", Boolean.toString(distance <= distanceThreshold));
                    if (distance <= distanceThreshold && pEnd.x != pIntersect.x && pEnd.y != pIntersect.y) {
                        // Delete in Black Pixel Queue
                        tempBlackPixels = new LinkedList<>(blackPixels);
                        blackPixels = new LinkedList<>();
                        while (!tempBlackPixels.isEmpty()) {
                            p = tempBlackPixels.remove();
                            if (p.x == pEnd.x && p.y == pEnd.y) {
                                // do nothing
                            } else {
                                blackPixels.add(p);
                            }
                        }
                        Log.d("DELETED", Integer.toString(pEnd.x) + " " + Integer.toString(pEnd.y));
                        bitmap.setPixel(pEnd.x, pEnd.y, Color.WHITE);
                        imageMatrix[pEnd.y][pEnd.x] = 255;
                    }
                }
            }

            if (minDistance > distanceThreshold) {
                marker = false;
            }

            // Find New Endpoint
            tempBlackPixels = new LinkedList<>(blackPixels);
            while (!tempBlackPixels.isEmpty()) {
                p = tempBlackPixels.remove();
                int[] neighbors = getNeighbors(p.x, p.y);
                int blackNeighbors = countBlackNeighbors(neighbors);

                Log.d("Black Neighbors", Integer.toString(blackNeighbors));
                if (blackNeighbors == 1) {
                    endPoints.add(p);
                    Log.d("NEW Endpoint", Integer.toString(p.x) + " " + Integer.toString(p.y));
                }
            }
        }

        // Add ALl Black Pixels
        while (!blackPixels.isEmpty()) {
            p = blackPixels.remove();
            this.blackPixels.add(p);
        }
    }
}
