package com.pengcit.jorfelag.pengolahancitra.util.classifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.drexel.cs.jah473.datastructures.KDTree;
import edu.drexel.cs.jah473.distance.DistanceFunction;

public class KNeighborsClassifier<E extends LabeledKDPoint> {

    private int nNeighbors;
    private boolean weighted;
    private DistanceFunction dist;
    private KDTree<E> tree;

    public KNeighborsClassifier(int nNeighbors, boolean weighted) {
        this.nNeighbors = nNeighbors;
        this.weighted = weighted;
    }

    public KNeighborsClassifier(int nNeighbors, boolean weighted, DistanceFunction dist) {
        this.nNeighbors = nNeighbors;
        this.weighted = weighted;
        this.dist = dist;
    }

    public void fit(List<E> data) {
        this.tree = new KDTree<>(data, data.size(), dist);
    }

    public String predict(E data) {
        List<E> neighbors = this.tree.kNN(data, nNeighbors);
        Map<String, Double> keyCount = new HashMap<>();

        double maxC = Double.MIN_VALUE;
        String label = null;
        for (E neighbor: neighbors) {
            double c = 0;
            if (keyCount.containsKey(neighbor.label)) {
                c = keyCount.get(neighbor.label);
            }

            if (weighted) {
                c += 1 / this.dist.distanceBetween(data, neighbor);;
            } else {
                c += 1;
            }
            keyCount.put(neighbor.label, c);

            if (c > maxC) {
                maxC = c;
                label = neighbor.label;
            }
        }

        return label;
    }
}
