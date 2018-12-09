package com.pengcit.jorfelag.pengolahancitra.face_detection;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.util.Log;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

public class FaceCandidateProcessor {

    private final static int[] X_TRANSLATION = {0, 1, 1, 1, 0, -1, -1, -1};
    private final static int[] Y_TRANSLATION = {-1, -1, 0, 1, 1, 1, 0, -1};

    private Point[] bounds;
    private int width;
    private int height;
    private Bitmap bitmap;
    private int[] pixels;
    private Point[][] featuresBoundary;
    private Point[][] controlPoints;
    private boolean isFace;

    FaceCandidateProcessor(Bitmap sobelBitmap, Point[] bounds) {
        this.bounds = bounds;

        width = bounds[1].x - bounds[0].x + 1;
        height = bounds[1].y - bounds[0].y + 1;

        Log.d("BOUNDS", String.format("%d %d | %d %d -> %d %d", bounds[0].x, bounds[0].y, bounds[1].x, bounds[1].y, width, height));

        bitmap = Bitmap.createBitmap(
                sobelBitmap,
                bounds[0].x,
                bounds[0].y,
                width,
                height
        );
        pixels = new int[height * width];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        featuresBoundary = new Point[7][];
        controlPoints = null;
        isFace = false;
    }

    public boolean isFace() {
        return isFace;
    }

    Point[][] getControlPoints() {
        return controlPoints;
    }

    public Point[][] getFeaturesBoundary() {
        return featuresBoundary;
    }

    void process() {
        clean();
        dilate();
        erode();

        double minArea = height * width * 0.0002;
        int[] processedPixels = Arrays.copyOf(pixels, pixels.length);
        for (int i = 0; i < pixels.length; i++) {
            if (processedPixels[i] == Color.WHITE) {
                Point start = new Point(i % width, i / width);
                int area = floodFillCountArea(processedPixels, start);
                if (area < minArea) {
                    floodFillClean(start);
                }
            }
        }
        extractFeaturesBoundary();
        if (isFace) {
            controlPoints = new Point[7][];
            for (int i = 0; i < featuresBoundary.length; ++i) {
                if (featuresBoundary[i] != null) {
                    Point min = new Point(featuresBoundary[i][0].x, featuresBoundary[i][0].y);
                    Point max = new Point(featuresBoundary[i][1].x, featuresBoundary[i][1].y);

                    min.x -= bounds[0].x;
                    max.x -= bounds[0].x;
                    min.y -= bounds[0].y;
                    max.y -= bounds[0].y;

                    List<Point> cp = findControlPoints(min, max, 10);
                    controlPoints[i] = new Point[cp.size()];
                    for (int j = 0; j < controlPoints[i].length; ++j) {
                        controlPoints[i][j] = new Point(
                                cp.get(j).x + bounds[0].x,
                                cp.get(j).y + bounds[0].y
                        );
                        Log.d("CP", String.format("%d %d %d %d", i, j, controlPoints[i][j].x, controlPoints[i][j].y));
                    }
                } else {
                    controlPoints[i] = null;
                }
            }
        }
    }

    private void dilate() {
        int[] dilated = new int[pixels.length];
        for (int i = 0; i < pixels.length; ++i) {
            int y = i / width;
            int x = i % width;

            int roi = Integer.MIN_VALUE;
            for (int j = Math.max(0, y - 2); j < Math.min(height, y + 3); ++j) {
                for (int k = Math.max(0, x - 2); k < Math.min(width, x + 3); ++k) {
                    int pixel = pixels[j * width + k];
                    if (pixel > roi) {
                        roi = pixel;
                    }
                }
            }
            dilated[i] = roi;
        }
        pixels = dilated;
    }

    private void erode() {
        int[] eroded = new int[pixels.length];
        for (int i = 0; i < pixels.length; ++i) {
            int y = i / width;
            int x = i % width;

            int roi = Integer.MAX_VALUE;
            for (int j = Math.max(0, y - 2); j < Math.min(height, y + 3); ++j) {
                for (int k = Math.max(0, x - 2); k < Math.min(width, x + 3); ++k) {
                    int pixel = pixels[j * width + k];
                    if (pixel < roi) {
                        roi = pixel;
                    }
                }
            }
            eroded[i] = roi;
        }
        pixels = eroded;
    }

    private void clean() {
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < 0.1 * width; ++j) {
                floodFillClean(new Point(j, i));
                floodFillClean(new Point(width - j - 1, i));
            }
        }
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < 0.1 * width; ++j) {
                floodFillClean(new Point(i, j));
                floodFillClean(new Point(i, height - j - 1));
            }
        }
    }

    private void floodFillClean(Point start) {
        Queue<Point> queue = new ArrayDeque<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            Point curr = queue.remove();

            if (curr.x < 0 || curr.x >= width || curr.y < 0 || curr.y >= height) {
                continue;
            }
            int i = curr.y * width + curr.x;

            if (pixels[i] == Color.WHITE) {
                pixels[i] = Color.BLACK;
                for (int j = 0; j < 8; ++j) {
                    queue.add(new Point(curr.x + X_TRANSLATION[j], curr.y + Y_TRANSLATION[j]));
                }
            }
        }
    }

    private int[] floodFill(int[] processedPixels, Point start) {
        Queue<Point> queue = new ArrayDeque<>();
        queue.add(start);
        int area = 0;

        Point min = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
        Point max = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);

        while (!queue.isEmpty()) {
            Point curr = queue.remove();

            if (curr.x < 0 || curr.x >= width || curr.y < 0 || curr.y >= height) {
                continue;
            }
            int i = curr.y * width + curr.x;

            if (processedPixels[i] == Color.WHITE) {
                min.x = Math.min(min.x, curr.x);
                min.y = Math.min(min.y, curr.y);
                max.x = Math.max(max.x, curr.x);
                max.y = Math.max(max.y, curr.y);
                area++;
                processedPixels[i] = Color.BLACK;
                for (int j = 0; j < 8; ++j) {
                    queue.add(new Point(curr.x + X_TRANSLATION[j], curr.y + Y_TRANSLATION[j]));
                }
            }
        }

        return new int[] {min.x, min.y, max.x, max.y, area};
    }

    private int floodFillCountArea(int[] processedPixels, Point start) {
        int area = 0;

        Queue<Point> queue = new ArrayDeque<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            Point curr = queue.remove();

            if (curr.x < 0 || curr.x >= width || curr.y < 0 || curr.y >= height) {
                continue;
            }
            int i = curr.y * width + curr.x;

            if (processedPixels[i] == Color.WHITE) {
                area++;
                processedPixels[i] = Color.BLACK;
                for (int j = 0; j < 8; ++j) {
                    queue.add(new Point(curr.x + X_TRANSLATION[j], curr.y + Y_TRANSLATION[j]));
                }
            }
        }

        return area;
    }

    private void extractFeaturesBoundary() {
        List<Point[]> eyesBoundary = findEyesBoundary(0, height / 2, 0, width);
        if (eyesBoundary != null){
            int starty = 0;
            int minx = Integer.MAX_VALUE;
            int maxx = Integer.MIN_VALUE;
            int miny = Integer.MAX_VALUE;
            for (Point[] eyeBoundary : eyesBoundary) {
                starty = Math.max(starty, eyeBoundary[1].y + 1);
                minx = Math.min(minx, eyeBoundary[0].x);
                maxx = Math.max(maxx, eyeBoundary[1].x);
                miny = Math.min(miny, eyeBoundary[0].y);
            }

            List<Point[]> eyeBrowsBoundary = findEyesBoundary(0, miny, minx, maxx);

            Point[] mouthBoundary = findMouthBoundary(starty);
            if (mouthBoundary != null && (minx < mouthBoundary[0].x && mouthBoundary[1].x < maxx)) {
                List<Point[]> nosesBoundary = findNoseBoundary(starty,
                        (int) (mouthBoundary[0].y - (0.05 * height)),
                        mouthBoundary[0].x, mouthBoundary[1].x);

                if (nosesBoundary != null) {
                    for (int i = 0; i < 7; i++) {
                        featuresBoundary[i] = new Point[2];
                    }
                    int counter = 0;
                    if (eyeBrowsBoundary != null) {
                        for (Point[] eyeBrowBoundary : eyeBrowsBoundary) {
                            featuresBoundary[counter++] = translateBoundary(eyeBrowBoundary);
                        }
                    } else {
                        for (int i = 0; i < 2; i++) {
                            featuresBoundary[counter++] = null;
                        }
                    }

                    for (Point[] eyeBoundary : eyesBoundary) {
                        featuresBoundary[counter++] = translateBoundary(eyeBoundary);
                    }

                    for (Point[] noseBoundary : nosesBoundary) {
                        featuresBoundary[counter++] = translateBoundary(noseBoundary);
                    }
                    if (counter == 5) {
                        for (Point[] noseBoundary : nosesBoundary) {
                            featuresBoundary[counter++] = translateBoundary(noseBoundary);
                        }
                    }

                    featuresBoundary[counter] = translateBoundary(mouthBoundary);
                    isFace = true;
                }
            }
        }
    }

    private Point[] translateBoundary(Point[] boundary) {
        Point[] newBoundary = Arrays.copyOf(boundary, boundary.length);
        for (int i = 0; i < boundary.length; i++) {
            newBoundary[i].x += bounds[0].x;
            newBoundary[i].y += bounds[0].y;
        }
        return newBoundary;
    }

    private List<Point[]> findEyesBoundary(int starty, int endy, int startx, int endx) {
        int[] processedPixels = Arrays.copyOf(pixels, pixels.length);
        int [] leftEye = new int[5];
        int [] rightEye = new int[5];

        // find left and right eye
        for (int y = starty; y < endy; y++) {
            for (int x = startx; x < endx; x++) {
                int i = y * width + x;
                if (processedPixels[i] == Color.WHITE) {
                    if (x < width / 2) {
                        int [] leftEyeCandidate = floodFill(processedPixels, new Point(x, y));
                        if (leftEyeCandidate[4] > leftEye[4]) {
                            leftEye = Arrays.copyOf(leftEyeCandidate, 5);
                        }
                    } else {
                        int [] rightEyeCandidate = floodFill(processedPixels, new Point(x, y));
                        if (rightEyeCandidate[4] > rightEye[4]) {
                            rightEye = Arrays.copyOf(rightEyeCandidate, 5);
                        }
                    }
                }
            }
        }

        //check valid eyes or not
        boolean valid;
        double eyeDistance = rightEye[0] - leftEye[2];

        double leftEyeHeight = leftEye[3] - leftEye[1];
        double leftEyeWidth = leftEye[2] - leftEye[0];
        double leftEyeRatio = leftEyeHeight / leftEyeWidth;

        double rightEyeHeight = rightEye[3] - rightEye[1];
        double rightEyeWidth = rightEye[2] - rightEye[0];
        double rightEyeRatio = rightEyeHeight / rightEyeWidth;

        valid = (eyeDistance > 0) && ((eyeDistance / width) < 0.25) &&
                (leftEyeRatio < 0.6) && (rightEyeRatio < 0.6);

        if (valid) {
            List<Point[]> eyesBoundary = new ArrayList<>();

            Point[] leftEyeBoundary = new Point[2];
            leftEyeBoundary[0] = new Point(leftEye[0], leftEye[1]);
            leftEyeBoundary[1] = new Point(leftEye[2], leftEye[3]);

            Point[] rightEyeBoundary = new Point[2];
            rightEyeBoundary[0] = new Point(rightEye[0], rightEye[1]);
            rightEyeBoundary[1] = new Point(rightEye[2], rightEye[3]);

            eyesBoundary.add(leftEyeBoundary);
            eyesBoundary.add(rightEyeBoundary);

            return eyesBoundary;
        } else {
            return null;
        }
    }

    private Point[] findMouthBoundary(int starty) {
        int[] processedPixels = Arrays.copyOf(pixels, pixels.length);
        int [] mouth = new int[5];

        for (int y = starty; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int i = y * width + x;
                if (processedPixels[i] == Color.WHITE) {
                    int [] mouthCandidate = floodFill(processedPixels, new Point(x, y));
                    if (mouthCandidate[4] > mouth[4]) {
                        mouth = Arrays.copyOf(mouthCandidate, 5);
                    }
                }
            }
        }

        double mouthHeight = mouth[3] - mouth[1];
        double mouthWidth = mouth[2] - mouth[0];
        double mouthRatio = mouthHeight / mouthWidth;

        if (mouthRatio < 0.4) {
            Point[] mouthBoundary = new Point[2];
            mouthBoundary[0] = new Point(mouth[0], mouth[1]);
            mouthBoundary[1] = new Point(mouth[2], mouth[3]);
            return mouthBoundary;
        } else {
            return null;
        }
    }


    private List<Point[]> findNoseBoundary(int starty, int endy, int startx, int endx) {
        int[] processedPixels = Arrays.copyOf(pixels, pixels.length);
        int [] leftNose = new int[5];
        int [] rightNose = new int[5];

        // find left and right nose
        for (int y = starty; y < endy; y++) {
            for (int x = startx; x < endx; x++) {
                int i = y * width + x;
                if (processedPixels[i] == Color.WHITE) {
                    if (x < width / 2) {
                        int [] leftNoseCandidate = floodFill(processedPixels, new Point(x, y));
                        if (leftNoseCandidate[4] > leftNose[4]) {
                            leftNose = Arrays.copyOf(leftNoseCandidate, 5);
                        }
                    } else {
                        int [] rightNoseCandidate = floodFill(processedPixels, new Point(x, y));
                        if (rightNoseCandidate[4] > rightNose[4]) {
                            rightNose = Arrays.copyOf(rightNoseCandidate, 5);
                        }
                    }
                }
            }
        }

        List<Point[]> nosesBoundary = null;

        if (leftNose[4] == 0 && rightNose[4] == 0) {
            return null;
        } else if (leftNose[4] == 0) {
            nosesBoundary = new ArrayList<>();
            Point[] noseBoundary = new Point[2];
            noseBoundary[0] = new Point(rightNose[0], rightNose[1]);
            noseBoundary[1] = new Point(rightNose[2], rightNose[3]);
            nosesBoundary.add(noseBoundary);
        } else if (rightNose[4] == 0) {
            nosesBoundary = new ArrayList<>();
            Point[] noseBoundary = new Point[2];
            noseBoundary[0] = new Point(leftNose[0], leftNose[1]);
            noseBoundary[1] = new Point(leftNose[2], leftNose[3]);
            nosesBoundary.add(noseBoundary);
        } else {
            //check valid nose or not
            double noseDistance = rightNose[0] - leftNose[2];
            if (noseDistance > 0 && (noseDistance / width) < 0.15) {
                nosesBoundary = new ArrayList<>();
                Point[] leftNoseBoundary = new Point[2];
                leftNoseBoundary[0] = new Point(leftNose[0], leftNose[1]);
                leftNoseBoundary[1] = new Point(leftNose[2], leftNose[3]);

                Point[] rightNoseBoundary = new Point[2];
                rightNoseBoundary[0] = new Point(rightNose[0], rightNose[1]);
                rightNoseBoundary[1] = new Point(rightNose[2], rightNose[3]);

                nosesBoundary.add(leftNoseBoundary);
                nosesBoundary.add(rightNoseBoundary);
            }
        }

        return nosesBoundary;
    }

    private List<Point> findControlPoints(Point min, Point max, int num) {
        List<Point> result = new ArrayList<>();

        int mid_num = (num - 2) / 2;
        Point left = new Point();

        for (int y = min.y; y <= max.y; y++) {
            int i = y * width + min.x;
            if (pixels[i] == Color.WHITE) {
                left = new Point(min.x, y);
                break;
            }
        }

        result.add(left);

        List<Point> tops = new ArrayList<>();
        List<Point> bottoms = new ArrayList<>();
        int x = min.x;
        int deltaX = (max.x - min.x + 1) / (mid_num + 1);

        for (int mid = 1; mid <= mid_num; mid++) {
            x += deltaX;

            Point top = new Point();
            for (int y = min.y; y <= max.y; y++) {
                int i = y * width + x;
                if (pixels[i] == Color.WHITE) {
                    top = new Point(x, y);
                    break;
                }
            }

            Point bottom = new Point();
            for (int y = max.y; y >= min.y; y--) {
                int i = y * width + x;
                if (pixels[i] == Color.WHITE) {
                    bottom = new Point(x, y);
                    break;
                }
            }

            tops.add(top);
            bottoms.add(bottom);
        }

        result.addAll(tops);

        Point right = new Point();

        for (int y = min.y; y <= max.y; y++) {
            int i = y * width + max.x;
            if (pixels[i] == Color.WHITE) {
                right = new Point(max.x, y);
                break;
            }
        }

        result.add(right);

        Collections.reverse(bottoms);
        result.addAll(bottoms);

        return result;
    }
}
