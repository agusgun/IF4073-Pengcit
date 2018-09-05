package com.pengcit.jorfelag.pengolahancitra.histogram.specification;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.pengcit.jorfelag.pengolahancitra.R;
import com.pengcit.jorfelag.pengolahancitra.contrast.enhancement.ContrastEnhancementActivity;
import com.pengcit.jorfelag.pengolahancitra.contrast.enhancement.PowerLawTask;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class HistogramSpecificationActivity extends AppCompatActivity {

    SeekBar pointSeekBar1, pointSeekBar2, pointSeekBar3;
    TextView detailSeekBar1, detailSeekBar2, detailSeekBar3;
    int seekBarValue1, seekBarValue2, seekBarValue3;
    RelativeLayout equalizerLayout;
    Integer[] referencedHistogramValue;

    ImageView originalImageView, resultImageView;

    Bitmap imageBitmapOrigin;
    Uri imageBitmapOriginURI;

    GraphView referencedHistogramView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_histogram_specification);

        pointSeekBar1 = (SeekBar) findViewById(R.id.histogram_specification_seekbar_point_1);
        pointSeekBar2 = (SeekBar) findViewById(R.id.histogram_specification_seekbar_point_2);
        pointSeekBar3 = (SeekBar) findViewById(R.id.histogram_specification_seekbar_point_3);

        detailSeekBar1 = (TextView) findViewById(R.id.histogram_specification_detail_1);
        detailSeekBar2 = (TextView) findViewById(R.id.histogram_specification_detail_2);
        detailSeekBar3 = (TextView) findViewById(R.id.histogram_specification_detail_3);

        pointSeekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progressChangedValue = i;
                detailSeekBar1.setText(Integer.toString(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBarValue1 = progressChangedValue;
            }
        });

        pointSeekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progressChangedValue = i;
                detailSeekBar2.setText(Integer.toString(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBarValue2 = progressChangedValue;
            }
        });

        pointSeekBar3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progressChangedValue = i;
                detailSeekBar3.setText(Integer.toString(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBarValue3 = progressChangedValue;
            }
        });

        originalImageView = (ImageView) findViewById(R.id.histogram_specification_original_image);
        resultImageView = (ImageView) findViewById(R.id.histogram_specification_result_image);

        Intent intent = getIntent();
        imageBitmapOriginURI = Uri.parse(intent.getExtras().getString("BitmapImageURI"));
        InputStream imageStream = null;
        try {
            imageStream = getContentResolver().openInputStream(imageBitmapOriginURI);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        imageBitmapOrigin = BitmapFactory.decodeStream(imageStream);
        originalImageView.setImageBitmap(imageBitmapOrigin);

        referencedHistogramView = (GraphView) findViewById(R.id.histogram_specification_referenced_histogram);
    }

    public void generateEqualizedImage(View view) {
        referencedHistogramView.removeAllSeries();
        new HistogramSpecificationEqualizedTask(HistogramSpecificationActivity.this, resultImageView, seekBarValue1, seekBarValue2, seekBarValue3, referencedHistogramView).execute(HistogramSpecificationActivity.this.imageBitmapOrigin);
    }
}
