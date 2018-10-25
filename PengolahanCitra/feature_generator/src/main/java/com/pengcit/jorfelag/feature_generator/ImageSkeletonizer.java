package com.pengcit.jorfelag.feature_generator;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ImageSkeletonizer {

    private final static int[] X_TRANSLATION = {0, 1, 1, 1, 0, -1, -1, -1};
    private final static int[] Y_TRANSLATION = {-1, -1, 0, 1, 1, 1, 0, -1};

    // template for acute angle emphasis
    private final static int[][][] ACUTE_ANGLE_TEMPLATE = {
            {{0, 0, 255, 0, 0},   {0, 0, 255, 0, 0},   {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {-1, 0, 0, 0, -1}},
            {{0, 255, 255, 0, 0}, {0, 0, 255, 0, 0},   {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {-1, 0, 0, 0, -1}},
            {{0, 0, 255, 255, 0}, {0, 0, 255, 0, 0},   {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {-1, 0, 0, 0, -1}},
            {{0, 255, 255, 0, 0}, {0, 255, 255, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {-1, 0, 0, 0, -1}},
            {{0, 0, 255, 255, 0}, {0, 0, 255, 255, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {-1, 0, 0, 0, -1}}
    };

    private final static int MIN_RESIZED_HEIGHT = 64;
    private final static int MIN_RESIZED_WIDTH = 64;

    private BufferedImage bitmap;
    private int[][] imageMatrix;
    private int threshold;
    private Queue<Point> blackPixels;

    private ArrayList<Point> endPoints;
    private ArrayList<Point> intersections_3;
    private ArrayList<Point> intersections_4;
    private ArrayList<Point> singlePoints;
    private int numOfComponents;
    private double [] directionCodeFrequency;
    private double[] verticalFeatures;
    private double[] horizontalFeatures;

    private boolean [][] visited;

    public ImageSkeletonizer(BufferedImage bitmap, int threshold) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int resizedWidth, resizedHeight;

        if (width >= height) {   // Landscape
            resizedHeight = Math.min(height, MIN_RESIZED_HEIGHT);
            resizedWidth = Math.min(width, resizedHeight * width / height);
        } else {    // Portrait
            resizedWidth = Math.min(width, MIN_RESIZED_WIDTH);
            resizedHeight = Math.min(height, resizedWidth * height / width);
        }

        this.bitmap = Util.resize(bitmap, resizedHeight, resizedWidth);
        this.threshold = threshold;

        this.blackPixels = new ConcurrentLinkedQueue<>();
        this.endPoints = new ArrayList<>();
        this.intersections_3 = new ArrayList<>();
        this.intersections_4 = new ArrayList<>();
        this.singlePoints = new ArrayList<>();

        this.directionCodeFrequency = new double[8];

        this.imageMatrix = convertToGrayMatrix();
    }

    public double[] process(int distanceThreshold, int counterThreshold) {
        // Pre-process
        smoothImage();
        emphasizeAcuteAngles();

        // Process
        skeletonize();

        // Post-process
        removeStaircase();
        pruneSkeleton(distanceThreshold, counterThreshold);
        extractGeometricProperty();

        List<Integer> boundary = findBoundary();
        double[][] diagonalFeatures = calculateDiagonalFeatures(boundary.get(0), boundary.get(1), boundary.get(2), boundary.get(3));
        Pair<double[], double[]> vhFeatures = calculateVerticalHorizontalFeatures(diagonalFeatures);
        verticalFeatures = vhFeatures.first;
        horizontalFeatures = vhFeatures.second;

        double[] features = new double[28];
        features[0] = numOfComponents;
        features[1] = singlePoints.size();
        features[2] = endPoints.size();
        features[3] = intersections_3.size();
        features[4] = intersections_4.size();
        System.arraycopy(directionCodeFrequency, 0, features, 5, 8);
        System.arraycopy(verticalFeatures, 0, features, 13, 9);
        System.arraycopy(horizontalFeatures, 0, features, 22, 6);

        return features;
    }

    public BufferedImage getBufferedImage() {
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

        final int[][] grayImageMatrix = new int[height][];
        final int[][] imageMatrix = Util.convertToIntMatrix(bitmap);

        Parallel.For(0, height, new LoopBody<Integer>() {
            @Override
            public void run(Integer y) {
                grayImageMatrix[y] = new int[width];

                for (int x = 0; x < width; ++x) {
                    int pixel = imageMatrix[y][x];
                    if(getGrayLevel(pixel) > threshold) {
                        grayImageMatrix[y][x] = 255;
                    } else {
                        grayImageMatrix[y][x] = 0;
                        blackPixels.add(new Point(x, y));
                    }
                }
            }
        });

        return grayImageMatrix;
    }

    private static int getGrayLevel(int pixel) {
        int red = (pixel & 0x00FF0000) >> 16;
        int green = (pixel & 0x0000FF00) >> 8;
        int blue = (pixel & 0x000000FF);

        return (int) (0.2989 * red + 0.5870 * green + 0.1140 * blue);
    }

    /**
     * Skeletonize using Zhang-Suen algorithm
     */
    private void skeletonize() {
        boolean firstStep = false;
        boolean hasChanged = false;

        Queue<Point> toClear = new LinkedList<>();
        Queue<Point> step2 = new LinkedList<>();
        Queue<Point> temp;
        Point p;

        do {
            firstStep = !firstStep;
            if (firstStep) {
                hasChanged = false;
            }

            if (firstStep) {
                temp = blackPixels;
            } else {
                temp = step2;
            }

            while (!temp.isEmpty()) {
                p = temp.remove();
                int[] neighbors = getNeighbors(p.x, p.y);
                int blackNeighbors = countBlackNeighbors(neighbors);
                int transitions = getTransitions(neighbors);

                if (blackNeighbors >= 2 && blackNeighbors <= 6 && transitions == 1 && atLeastOneIsWhite(neighbors, firstStep)) {
                    toClear.add(p);
                    hasChanged = true;
                } else {
                    if (firstStep) {
                        step2.add(p);
                    } else {
                        blackPixels.add(p);
                    }
                }
            }

            while(!toClear.isEmpty()) {
                p = toClear.remove();
                imageMatrix[p.y][p.x] = 255;
            }
        } while (firstStep || hasChanged);
    }

    private void smoothImage() {
        Queue<Point> blackPixels = new LinkedList<>(this.blackPixels);

        while (!blackPixels.isEmpty()) {
            Point p = blackPixels.remove();
            int[] neighbors = getNeighbors(p.x, p.y);
            int blackNeighbors = countBlackNeighbors(neighbors);
            int transitions = getTransitions(neighbors);

            if (blackNeighbors <= 2 && transitions < 2) {
                imageMatrix[p.y][p.x] = 255;
            }
        }
    }

    private void emphasizeAcuteAngles() {
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

    private void removeStaircase() {
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
                }
            } catch (Exception e) {
                this.blackPixels.add(p);
            }
        }
    }

    private void extractGeometricProperty() {
        Queue<Point> blackPixels = new LinkedList<>(this.blackPixels);

        while (!blackPixels.isEmpty()) {
            Point p = blackPixels.remove();

            int[] neighbors = getNeighbors(p.x, p.y);
            int blackNeighbors = countBlackNeighbors(neighbors);
            int transitions = getTransitions(neighbors);

            if (blackNeighbors == 0) {
                singlePoints.add(p);
//                try {
//                    bitmap.setPixel(p.x, p.y, Color.RED);
//                    bitmap.setPixel(p.x + 1, p.y, Color.RED);
//                    bitmap.setPixel(p.x - 1, p.y, Color.RED);
//                    bitmap.setPixel(p.x, p.y + 1, Color.RED);
//                    bitmap.setPixel(p.x, p.y - 1, Color.RED);
//                }catch (Exception e) {
//
//                }
            } else if (blackNeighbors == 1) {
                endPoints.add(p);
//
//                bitmap.setPixel(p.x, p.y, Color.GREEN);
//                bitmap.setPixel(p.x + 1, p.y, Color.GREEN);
//                bitmap.setPixel(p.x - 1, p.y, Color.GREEN);
//                bitmap.setPixel(p.x, p.y + 1, Color.GREEN);
//                bitmap.setPixel(p.x, p.y - 1, Color.GREEN);
            } else if (blackNeighbors == 3 && transitions == 3) {
                intersections_3.add(p);

//                bitmap.setPixel(p.x, p.y, Color.RED);
//                bitmap.setPixel(p.x + 1, p.y, Color.RED);
//                bitmap.setPixel(p.x - 1, p.y, Color.RED);
//                bitmap.setPixel(p.x, p.y + 1, Color.RED);
//                bitmap.setPixel(p.x, p.y - 1, Color.RED);
            } else if (blackNeighbors == 4 && transitions == 4) {
                intersections_4.add(p);
            }
        }
        this.numOfComponents = getNumOfComponents();

        if (this.numOfComponents != 0) {
            double sum = 0;
            for (int i = 0; i < 8; i++) {
                sum += directionCodeFrequency[i];
            }
            for (int i = 0; i < 8; i++) {
                directionCodeFrequency[i] = (sum == 0) ? 0.0 : directionCodeFrequency[i] / sum;
            }
        }
    }

    private void pruneSkeleton(int distanceThreshold, int counterThreshold) {
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
//                Log.d("ENDPOINTS", Integer.toString(p.x) + " "  + Integer.toString(p.y));
                endPoints.add(p);
            } else if (blackNeighbors >= 3) {
//                Log.d("Intersection", Integer.toString(p.x) + " "  + Integer.toString(p.y));
                intersectionPoints.add(p);
            }
        }

        int counter = 0;
        boolean marker = true;
        while (marker && counter < counterThreshold) {
            counter++;
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
//                    Log.d("Distance", Integer.toString(distance));
//                    Log.d("Min Distance", Integer.toString(minDistance));
//                    Log.d("Distance Threshold", Integer.toString(distanceThreshold));
                    if (minDistance > distance) {
                        minDistance = distance;
                    }
//                    Log.d("Comparing", Boolean.toString(distance <= distanceThreshold));
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
//                        Log.d("DELETED", Integer.toString(pEnd.x) + " " + Integer.toString(pEnd.y));
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

//                Log.d("Black Neighbors", Integer.toString(blackNeighbors));
                if (blackNeighbors == 1) {
                    endPoints.add(p);
//                    Log.d("NEW Endpoint", Integer.toString(p.x) + " " + Integer.toString(p.y));
                }
            }
        }

        // Add All Black Pixels
        while (!blackPixels.isEmpty()) {
            p = blackPixels.remove();
            this.blackPixels.add(p);
        }
    }

    private int getNumOfComponents() {
        int numOfComponents = 0;
        visited = new boolean[bitmap.getHeight()][bitmap.getWidth()];
        Queue<Point> blackPixels = new LinkedList<>(this.blackPixels);

        // Sort by increasing y value
        Collections.sort((List) blackPixels, new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                int y = Integer.compare(o1.y, o2.y);
                if (y != 0)
                    return y;
                return Integer.compare(o1.x, o2.x);
            }
        });

        while (!blackPixels.isEmpty()) {
            Point p = blackPixels.remove();
            if (!visited[p.y][p.x]) {
                visited[p.y][p.x] = true;
                numOfComponents++;
                dfs(p);
            }
        }
        return numOfComponents;

    }

    private void dfs(Point p) {
        Stack<Point> stack = new Stack<>();
        stack.push(p);

        while (!stack.empty()) {
            Point curr = stack.pop();

            for (int i = 0; i < 8; ++i) {
                Point next = new Point(curr.x + X_TRANSLATION[i], curr.y + Y_TRANSLATION[i]);
                try {
                    if (imageMatrix[next.y][next.x] == 0 && !visited[next.y][next.x]) {
                        directionCodeFrequency[i]++;
                        visited[next.y][next.x] = true;
                        stack.push(next);
                    }
                } catch (ArrayIndexOutOfBoundsException ignored) {
                }
            }
        }
    }

    public ArrayList<Integer> findBoundary() {
        ArrayList<Integer> boundary = new ArrayList<>();
        int minX = 9999, minY = 9999;
        int maxX = -9999, maxY = -9999;

        // Assumption: 1 Image
        for (int i = 0; i < imageMatrix.length; i++) {
            for (int j = 0; j < imageMatrix[0].length; j++) {
                if (imageMatrix[i][j] == 0) {
                    if (j < minX) {
                        minX = j;
                    }
                    if (j > maxX) {
                        maxX = j;
                    }
                    if (i < minY) {
                        minY = i;
                    }
                    if (i > maxY) {
                        maxY = i;
                    }
                }
            }
        }

        boundary.add(minX);
        boundary.add(maxX);
        boundary.add(minY);
        boundary.add(maxY);
        return boundary;
    }

    public float calculateFeature(int[][] matrix) {
        float result = 0;
        int matrixSize = 10;

        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < i; j++) {
                if (matrix[j][i - j] == 0) {
                    result += 1;
                }
            }
        }
        for (int i = 0; i < matrixSize - 1; i++) {
            for (int j = 0; j < matrixSize - 1 - i; j++) {
                if (matrix[j + i + 1][matrixSize - j - 1] == 0) {
                    result += 1;
                }
            }
        }
        result /= 19.0;
        return result;
    }

    public double[][] calculateDiagonalFeatures(int minX, int maxX, int minY, int maxY) {
        double[][] diagonalFeatures = new double[9][6];
        int counterY = 0;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 6; j++) {
                diagonalFeatures[i][j] = 0;
            }
        }
        for (int i = minY; i < maxY + 1; i+= 10) {
            counterY++;
            if (i + 10 > maxY || counterY > 9) break;

            int counterX = 0;
            for (int j = minX; j < maxX + 1; j += 10) {
                counterX++;
                if (j + 10 > maxX || counterX > 6) break;
                int[][] diagonalMatrix = new int[10][10];
                for (int k = 0; k < 10; k++) {
                    for (int l = 0; l < 10; l++) {
                        diagonalMatrix[k][l] = imageMatrix[i + k][j + l];
                    }
                }
                diagonalFeatures[i][j] = calculateFeature(diagonalMatrix);
            }
        }
        return diagonalFeatures;
    }

    public Pair<double[], double[]> calculateVerticalHorizontalFeatures(double[][] diagonalFeatures) {
        double[] verticalFeatures = new double[9];
        double[] horizontalFeatures = new double[6];

        for (int i = 0; i < 9; i++) {
            double horizontal = 0;
            for (int j = 0; j < 6; j++) {
                horizontal += diagonalFeatures[i][j];
            }
            horizontal /= 6.0;
            verticalFeatures[i] = horizontal;
        }

        for (int j = 0; j < 6; j++) {
            double vertical = 0;
            for (int i = 0; i < 9; i++) {
                vertical += diagonalFeatures[i][j];
            }
            vertical /= 9.0;
            horizontalFeatures[j] = vertical;
        }
        return new Pair<>(verticalFeatures, horizontalFeatures);
    }

    private double calculateDissimilarity(double[] v1, double[] v2) {
        double result = 0;
        double error;
        for (int i = 0; i < v1.length; i++) {
            error = v1[i] - v2[i];
            result += (error * error);
        }
        return result;
    }

    public class Pair<F, S> {
        private F first;
        private S second;

        Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }
    }
}

