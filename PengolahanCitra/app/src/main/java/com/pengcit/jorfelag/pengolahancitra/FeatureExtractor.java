package com.pengcit.jorfelag.pengolahancitra;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class FeatureExtractor {

    ArrayList<Point> endPoints;
    ArrayList<Point> intersectionPoints;
    ArrayList<Point> pertigaanPoints;
    ArrayList<Point> perempatanPoints;

    int[][] imageMatrix;
    int[][] tempImageMatrix;

    Bitmap bitmap;

    // Constructor
    public FeatureExtractor(ArrayList<Point> endPoints, ArrayList<Point> intersectionPoints, int[][] imageMatrix, Bitmap bitmap, ArrayList<Point> pertigaan, ArrayList<Point> perempatan) {
        this.endPoints = new ArrayList<>(endPoints);
        this.intersectionPoints = new ArrayList<>(intersectionPoints);
        this.imageMatrix = imageMatrix;
        this.tempImageMatrix = new int[imageMatrix.length][imageMatrix[0].length];

        this.bitmap = bitmap;

        this.pertigaanPoints = new ArrayList<>(pertigaan);
        this.perempatanPoints = new ArrayList<>(perempatan);

        System.out.println("HEHEHE");
        for (int i = 0; i < imageMatrix.length; i++) {
            for (int j = 0; j < imageMatrix[i].length; j++) {
                if (imageMatrix[i][j] != 0) {
                    System.out.print(1);
                } else {
                    System.out.print(0);
                }
            }
            System.out.println();
        }
    }

    // Get Number of Endpoint
    public int getNumberOfEndpoints() {
        return this.endPoints.size();
    }

    // Get Number of Pertigaan
    public int getNumberOfPertigaan() {
        return this.pertigaanPoints.size();
    }

    // Get Number of Perempatan
    public int getNumberOfPerempatan() {
        return this.perempatanPoints.size();
    }

    // Get Number of Intersection
    public int getNumberOfIntersection() {
        return this.intersectionPoints.size();
    }

    // Get Number of Component
    public int getNumberOfComponent() {
        boolean[][] used = new boolean[imageMatrix.length][imageMatrix[0].length];
        for (int i = 0; i < imageMatrix.length; i++) {
            for (int j = 0; j < imageMatrix[0].length; j++) {
                used[i][j] = false;
            }
        }

        int numberOfComponent = 0;
        for (int i = 0; i < imageMatrix.length; i++) {
            for (int j = 0; j < imageMatrix[0].length; j++) {
                if (imageMatrix[i][j] == 0 && !used[i][j]) {
                    numberOfComponent += 1;
                    fillComponent(imageMatrix, used, j, i);
                }
            }
        }

        return numberOfComponent;
    }


    private void fillComponent(int[][] imageMatrix, boolean[][] used, int x, int y) {
        int[] dx = {0, 1, 1, 1, 0, -1, -1, -1};
        int[] dy = {-1, -1, 0, 1, 1, 1, 0, -1};

        used[y][x] = true;
        for (int i = 0; i < dx.length; i++) {
            if (imageMatrix[y+dy[i]][x+dx[i]] == 0 && !used[y+dy[i]][x+dx[i]]) {
                fillComponent(imageMatrix, used, x+dx[i], y+dy[i]);
            }
        }
    }

    // Get First Pixel
    public Point getFirstPixel() {
        Point ans = new Point(-1, -1);
        for (int i = 0; i < imageMatrix.length; i++) {
            for (int j = 0; j < imageMatrix[0].length; j++) {
                if (imageMatrix[i][j] == 0) {
                    ans.set(j, i);
                    break;
                }
            }
            if (ans.x != -1) break;
        }
        return ans;
    }

    // Get Last Pixel
    public Point getLastPixel() {
        Point ans = new Point();
        for (int i = 0; i < imageMatrix.length; i++) {
            for (int j = 0; j < imageMatrix[0].length; j++) {
                if (imageMatrix[i][j] == 0) {
                    ans.set(j, i);
                }
            }
        }
        return ans;
    }

    // Get Number of Loop
    public int getNumberOfLoop() {
        return 0;
    }

    // Fill The Loop
    private void fillLoop(int[][] imageMatrix, boolean[][] used, int x, int y) {

    }

    // Get Number of Teluk
    public int getNumberOfTeluk() {
        boolean[][] used = new boolean[imageMatrix.length][imageMatrix[0].length];
        for (int i = 0; i < imageMatrix.length; i++) {
            for (int j = 0; j < imageMatrix[0].length; j++) {
                used[i][j] = false;
            }
        }

        for (int i = 0; i < imageMatrix.length; i++) {
            boolean blackBefore = false;
            for (int j = 0; j < imageMatrix[0].length; j++) {
                if (imageMatrix[i][j] == 0) {
                    blackBefore = true;
                } else {
                    if (blackBefore && !used[i][j] && imageMatrix[i][j] == 255) {
                        fillTeluk(imageMatrix, used, j, i);
                    }
                }
            }
        }
        return 0;
    }

    // Fill The Teluk
    public void fillTeluk(int[][] imageMatrix, boolean[][] used, int x, int y) {
        int[] dx = {0, 1, 1, 1, 0, -1, -1, -1};
        int[] dy = {-1, -1, 0, 1, 1, 1, 0, -1};

        used[y][x] = true;
        for (int i = 0; i < dx.length; i++) {
            if (imageMatrix[y+dy[i]][x+dx[i]] == 255 && !used[y+dy[i]][x+dx[i]]) {
                fillTeluk(imageMatrix, used, x+dx[i], y+dy[i]);
            }
        }
    }

}
