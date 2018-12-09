package com.pengcit.jorfelag.pengolahancitra.face_detection;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import com.pengcit.jorfelag.pengolahancitra.util.LoopBody;
import com.pengcit.jorfelag.pengolahancitra.util.Parallel;

import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

public class FaceDetector {

    public static int MAX_WIDTH = 300;
    public static int MAX_HEIGHT = 400;

    public static int MIN_AREA = 10;

    private final static int[] X_TRANSLATION = {0, 1, 1, 1, 0, -1, -1, -1};
    private final static int[] Y_TRANSLATION = {-1, -1, 0, 1, 1, 1, 0, -1};

    private final static float[] SOBEL_X = {-1, 0, 1, -2, 0, 2, -1, 0, 1};
    private final static float[] SOBEL_Y = {-1, -2, -1, 0, 0, 0, 1, 2, 1};

    private Bitmap originalBitmap;
    private Bitmap processedBitmap;
    private int[] bitmap;
    private int width, height;

    public FaceDetector(Bitmap inputBitmap) {
        originalBitmap = inputBitmap.copy(inputBitmap.getConfig(), true);

        height = originalBitmap.getHeight();
        width = originalBitmap.getWidth();

        bitmap = new int[height * width];
        Parallel.For(0, height, new LoopBody<Integer>() {
            @Override
            public void run(Integer y) {
                final int[] resultPixels = new int[width];
                originalBitmap.getPixels(resultPixels, 0, width, 0, y, width, 1);

                for (int x = 0; x < width; ++x) {
                    bitmap[y * width + x] = isSkinColor(resultPixels[x]) ? Color.WHITE : Color.BLACK;
                }
            }
        });

        processedBitmap = Bitmap.createBitmap(bitmap, width, height, originalBitmap.getConfig());
        Bitmap sobelBitmap = new Sobel(originalBitmap).process();

        Canvas canvas = new Canvas(originalBitmap);
        Paint framePaint = new Paint();
        framePaint.setColor(Color.RED);
        framePaint.setStyle(Paint.Style.STROKE);
        Paint pointsPaint = new Paint();
        pointsPaint.setColor(Color.GREEN);
        pointsPaint.setStyle(Paint.Style.STROKE);

        ArrayList<Point[]> faceCandidateBounds =
                new CandidateFaceDetector(bitmap, width, height).process();

        for (Point[] bound: faceCandidateBounds) {
//            bound = faceCandidateBounds.get(0);
            FaceCandidate fc = new FaceCandidate(processedBitmap, sobelBitmap, bound);
            fc.process();
            for (Point[] featureBoundary :fc.getFeaturesBoundary()) {
                Rect r = new Rect(featureBoundary[0].x, featureBoundary[0].y, featureBoundary[1].x, featureBoundary[1].y);
                canvas.drawRect(r, framePaint);
            }
//            originalBitmap = fc.bitmap;
        }
//
//            Point[][] controlPoints = fc.getControlPoints();

//            ArrayList<Float> f = new ArrayList<>();
//            for (Point[] component: controlPoints) {
//                for (Point controlPoint: component) {
//                    f.add((float) controlPoint.x);
//                    f.add((float) controlPoint.y);
//                }
//            }
//            float[] f2 = new float[f.size()];
//            for (int i = 0; i < f2.length; ++i) {
//                f2[i] = f.get(i);
//            }
//            canvas.drawPoints(f2, pointsPaint);

//            for (Point[] component: controlPoints) {
//                r = new Rect(component[0].x, component[0].y, component[1].x, component[1].y);
//                canvas.drawRect(r, pointsPaint);
//            }
//        }
    }

    public Bitmap getBitmap() {
        return originalBitmap;
    }

    private int[] getRGB(int pixel) {
        int[] RGB = new int[3];

        RGB[0] = (pixel & 0x00FF0000) >> 16;
        RGB[1] = (pixel & 0x0000FF00) >> 8;
        RGB[2] = (pixel & 0x000000FF);

        return RGB;
    }

    private int[] getYCbCr(int pixel) {
        int[] YCbCr = new int[3];

        int[] RGB = getRGB(pixel);
        YCbCr[0] = (int) (0.299 * RGB[0] + 0.587 * RGB[1] + 0.114 * RGB[2]);
        YCbCr[1] = (int) (128 - 0.169 * RGB[0] - 0.331 * RGB[1] + 0.5 * RGB[2]);
        YCbCr[2] = (int) (128 + 0.5 * RGB[0] - 0.419 * RGB[1] - 0.081 * RGB[2]);
        return YCbCr;
    }

    private boolean isSkinColor(int pixel) {
        int[] YCbCr = getYCbCr(pixel);
        return (YCbCr[0] > 80 && YCbCr[1] > 85 && YCbCr[1] < 135 &&
                YCbCr[2] > 135 && YCbCr[2] < 180);
    }

    private class FaceCandidate {

        private Point[] bounds;
        private int width;
        private int height;
        private Bitmap bitmap;
        private int[] pixels;
        private int[] sobelPixels;
        private List<Point[]> featuresBoundary;
        private Point[][] controlPoints;

        FaceCandidate(Bitmap processedBitmap, Bitmap sobelBitmap, Point[] bounds) {
            this.bounds = bounds;

            width = bounds[1].x - bounds[0].x + 1;
            height = bounds[1].y - bounds[0].y + 1;

            Log.d("BOUNDS", String.format("%d %d | %d %d -> %d %d", bounds[0].x, bounds[0].y, bounds[1].x, bounds[1].y, width, height));

//            bitmap = Bitmap.createBitmap(
//                    processedBitmap,
//                    bounds[0].x,
//                    bounds[0].y,
//                    width,
//                    height
//            );
//            pixels = new int[height * width];
//            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

            bitmap = Bitmap.createBitmap(
                    sobelBitmap,
                    bounds[0].x,
                    bounds[0].y,
                    width,
                    height
            );
            pixels = new int[height * width];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
            featuresBoundary = new ArrayList<>();
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
//                    Log.d("AREA", "area " + area + " " + minArea);
                    if (area < minArea) {
                        floodFillClean(start);
                    }
                }
            }
//            bitmap = Bitmap.createBitmap(pixels, width, height, bitmap.getConfig());
            extractFeaturesBoundary();

//
//            controlPoints = boundaries;
//            for (int i = 0; i < controlPoints.length; ++i) {
//                for (int j = 0; j < controlPoints[i].length; ++j) {
//                    controlPoints[i][j].x += bounds[0].x;
//                    controlPoints[i][j].y += bounds[0].y;
//                }
//            }


//            controlPoints = new Point[5][];
//            for (int i = 0; i < boundaries.length; ++i) {
//                Point min = boundaries[i][0];
//                Point max = boundaries[i][1];
//
//                if (min.x == Integer.MAX_VALUE || min.y == Integer.MAX_VALUE
//                        || max.x == Integer.MIN_VALUE || max.y == Integer.MIN_VALUE) {
//                    continue;
//                }
//
//                ArrayList<Point> cp = findControlPoints(findPoints(min, max, 10), 20);
//                controlPoints[i] = new Point[cp.size()];
//                for (int j = 0; j < controlPoints[i].length; ++j) {
//                    controlPoints[i][j] = new Point(
//                            cp.get(j).x + bounds[0].x,
//                            cp.get(j).y + bounds[0].y
//                    );
//                    Log.d("CP", String.format("%d %d", controlPoints[i][j].x, controlPoints[i][j].y));
//                }
//            }
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
               if (eyeBrowsBoundary != null) {
                   for (Point[] eyeBrowBoundary : eyeBrowsBoundary) {
                       featuresBoundary.add(translateBoundary(eyeBrowBoundary));
                   }
               }

               Point[] mouthBoundary = findMouthBoundary(starty);
               if (mouthBoundary != null && (minx < mouthBoundary[0].x && mouthBoundary[1].x < maxx)) {
                   List<Point[]> nosesBoundary = findNoseBoundary(starty,
                           (int) (mouthBoundary[0].y - (0.05 * height)),
                           mouthBoundary[0].x, mouthBoundary[1].x);
                   if (nosesBoundary != null) {
                       for (Point[] noseBoundary : nosesBoundary) {
                           featuresBoundary.add(translateBoundary(noseBoundary));
                       }
                       featuresBoundary.add(translateBoundary(mouthBoundary));
                       for (Point[] eyeBoundary : eyesBoundary) {
                           featuresBoundary.add(translateBoundary(eyeBoundary));
                       }
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

        Point[][] getControlPoints() {
            return controlPoints;
        }

        public List<Point[]> getFeaturesBoundary() {
            return featuresBoundary;
        }

        private Point[] findEyeCenter(int startY) {
            for (int i = startY * width; i < pixels.length; i += width) {
                boolean foundBlack = false;
                boolean foundWhite = false;

                int foundFirst = -1;
                int foundSecond = -1;

                for (int j = i; j < i + width; j++) {
                    if (pixels[j] == Color.BLACK) {
                        if (!foundBlack) {
                            foundBlack = true;
                            foundFirst = j;
                        } else {
                            if (foundWhite && (j - foundFirst) % width >= width / 5) {
                                foundSecond = j;
                            }
                        }
                    } else if (foundBlack) {
                        if (pixels[j] == Color.WHITE) {
                            foundWhite = true;
                        }
                    }
                }

                if (foundSecond != -1) {
                    return new Point[] {
                            new Point(foundFirst % width, foundFirst / width),
                            new Point(foundSecond % width, foundSecond / width)
                    };
                }
            }

            return null;
        }

//        private Point findMouth() {
//            int[] processedPixels = Arrays.copyOf(pixels, pixels.length);
//            for (int i = pixels.length / 2; i < pixels.length; ++i) {
//                int x = i % width;
//                if (x < width / 3 || x > width * 2 / 3) {
//                    continue;
//                }
//
//                if (pixels[i] != Color.WHITE) {
//                    Point p = new Point(i % width, i / width);
//                    Point[] bounds = floodFill(processedPixels, p);
//                    if (bounds[0].x < width / 2 && bounds[1].x > width / 2) {
//                        return p;
//                    }
//                }
//            }
//            return null;
//        }

        private ArrayList<Point> findPoints(Point min, Point max, int threshold) {
            ArrayList<Point> result = new ArrayList<>();

            topLeft:
            for (int y = min.y; y < max.y; ++y) {
                for (int x = min.x; x < max.x; ++x) {
                    int i = y * width + x;
                    if (sobelPixels[i] >= threshold) {
                        result.add(new Point(x, y));
                        break topLeft;
                    }
                }
            }

            topRight:
            for (int y = min.y; y < max.y; ++y) {
                for (int x = max.x - 1; x >= min.x; --x) {
                    int i = y * width + x;
                    if (sobelPixels[i] >= threshold) {
                        result.add(new Point(x, y));
                        break topRight;
                    }
                }
            }

            bottomLeft:
            for (int y = max.y - 1; y >= min.y; --y) {
                for (int x = min.x; x < max.x; ++x) {
                    int i = y * width + x;
                    if (sobelPixels[i] >= threshold) {
                        result.add(new Point(x, y));
                        break bottomLeft;
                    }
                }
            }

            bottomRight:
            for (int y = max.y - 1; y >= min.y; --y) {
                for (int x = max.x - 1; x >= min.x; --x) {
                    int i = y * width + x;
                    if (sobelPixels[i] >= threshold) {
                        result.add(new Point(x, y));
                        break bottomRight;
                    }
                }
            }

            return result;
        }

        private ArrayList<Point> findControlPoints(ArrayList<Point> points, int num) {
            ArrayList<Point> result = new ArrayList<>();

            int size = points.size();
            if (size == 0) {
                return result;
            }

            int increment = size / num;
            for (int i = 0; i < num; i += increment) {
                result.add(points.get(i));
            }
            return result;
        }
    }

    private class Sobel {

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
}
