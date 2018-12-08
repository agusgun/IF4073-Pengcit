package com.pengcit.jorfelag.pengolahancitra.face_detection;

import android.graphics.Color;
import android.graphics.Point;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;

public class CandidateFaceDetector {

    private final static int[] X_TRANSLATION = {0, 1, 1, 1, 0, -1, -1, -1};
    private final static int[] Y_TRANSLATION = {-1, -1, 0, 1, 1, 1, 0, -1};

    private int[] processedPixels;
    private int width, height;

    private ArrayList<Point[]> candidateFaces;

   CandidateFaceDetector(int[] pixels, int width, int height) {
        this.processedPixels = Arrays.copyOf(pixels, pixels.length);
        this.width = width;
        this.height = height;
        this.candidateFaces = new ArrayList<>();
    }

    ArrayList<Point[]> process() {
        for (int i = 0; i < processedPixels.length; ++i) {
            if (processedPixels[i] == Color.WHITE) {
                int y = i / width;
                int x = i % width;
                Point p = new Point(x, y);

                Point[] bounds = floodFill(p);
                int currWidth = bounds[1].x - bounds[0].x + 1;
                int currHeight = bounds[1].y - bounds[0].y + 1;

                if (currWidth < 100 || currHeight < 100) {
                    continue;
                }

                candidateFaces.add(bounds);
            }
        }

        return candidateFaces;
    }

    private Point[] floodFill(Point start) {
        Queue<Point> queue = new ArrayDeque<>();
        queue.add(start);

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

                processedPixels[i] = Color.BLACK;
                for (int j = 0; j < 8; ++j) {
                    queue.add(new Point(curr.x + X_TRANSLATION[j], curr.y + Y_TRANSLATION[j]));
                }
            }
        }

        return new Point[] {min, max};
    }
}
