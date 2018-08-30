package com.pengcit.jorfelag.pengolahancitra;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.util.HashMap;

public class HistogramActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_histogram);

        Intent intent = getIntent();
        HashMap<String, Integer[]> hashMap = (HashMap<String, Integer[]>)intent.getSerializableExtra("colorMap");
        GraphView redHistogramView = (GraphView) findViewById(R.id.red_histogram);
        BarGraphSeries series = new BarGraphSeries<>(generateData(hashMap.get("red")));
        series.setColor(Color.RED);

        redHistogramView.addSeries(series);
        redHistogramView.setTitle("Red Histogram");
        redHistogramView.getViewport().setXAxisBoundsManual(true);
        redHistogramView.getViewport().setMinX(0);
        redHistogramView.getViewport().setMaxX(255);

        // enable scaling and scrolling
        redHistogramView.getViewport().setScalable(true);
        redHistogramView.getViewport().setScalableY(true);


        GraphView greenHistogramView = (GraphView) findViewById(R.id.green_histogram);
        BarGraphSeries series2 = new BarGraphSeries<>(generateData(hashMap.get("green")));
        series2.setColor(Color.GREEN);

        greenHistogramView.addSeries(series2);
        greenHistogramView.setTitle("Green Histogram");
        greenHistogramView.getViewport().setXAxisBoundsManual(true);
        greenHistogramView.getViewport().setMinX(0);
        greenHistogramView.getViewport().setMaxX(255);

        // enable scaling and scrolling
        greenHistogramView.getViewport().setScalable(true);
        greenHistogramView.getViewport().setScalableY(true);

        GraphView blueHistogramView = (GraphView) findViewById(R.id.blue_histogram);
        BarGraphSeries series3 = new BarGraphSeries<>(generateData(hashMap.get("blue")));
        series3.setColor(Color.BLUE);

        blueHistogramView.addSeries(series3);
        blueHistogramView.setTitle("Blue Histogram");
        blueHistogramView.getViewport().setXAxisBoundsManual(true);
        blueHistogramView.getViewport().setMinX(0);
        blueHistogramView.getViewport().setMaxX(255);

        // enable scaling and scrolling
        blueHistogramView.getViewport().setScalable(true);
        blueHistogramView.getViewport().setScalableY(true);


        GraphView grayHistogramView = (GraphView) findViewById(R.id.gray_histogram);
        BarGraphSeries series4 = new BarGraphSeries<>(generateData(hashMap.get("gray")));
        series4.setColor(Color.GRAY);
        grayHistogramView.addSeries(series4);

        grayHistogramView.setTitle("Grayscale Histogram");
        grayHistogramView.getViewport().setXAxisBoundsManual(true);
        grayHistogramView.getViewport().setMinX(0);
        grayHistogramView.getViewport().setMaxX(255);

        // enable scaling and scrolling
        grayHistogramView.getViewport().setScalable(true);
        grayHistogramView.getViewport().setScalableY(true);
    }

    private DataPoint[] generateData(Integer[] colorValuesFrequencies) {
        DataPoint[] values = new DataPoint[colorValuesFrequencies.length];
        for (int i=0; i < values.length; i++) {
            DataPoint v = new DataPoint(i, colorValuesFrequencies[i]);
            values[i] = v;
        }
        return values;
    }
}
