package com.pengcit.jorfelag.pengolahancitra.histogram.specification;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.pengcit.jorfelag.pengolahancitra.R;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class HistogramSpecificationActivity extends AppCompatActivity {
    private final static int NUM_POINTS = 5;

    private SeekBar[] seekBars;
    private TextView[] seekBarLabels;
    private int[] seekBarValues;

    private ImageView originalImageView, resultImageView;

    private Bitmap imageBitmapOrigin;
    private Uri imageBitmapOriginURI;

    private GraphView referencedHistogramView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_histogram_specification);

        seekBars = new SeekBar[]{
                findViewById(R.id.histogram_specification_seekbar_point_1),
                findViewById(R.id.histogram_specification_seekbar_point_2),
                findViewById(R.id.histogram_specification_seekbar_point_3),
                findViewById(R.id.histogram_specification_seekbar_point_4),
                findViewById(R.id.histogram_specification_seekbar_point_5),
        };

        seekBarLabels = new TextView[]{
                findViewById(R.id.histogram_specification_detail_1),
                findViewById(R.id.histogram_specification_detail_2),
                findViewById(R.id.histogram_specification_detail_3),
                findViewById(R.id.histogram_specification_detail_4),
                findViewById(R.id.histogram_specification_detail_5),
        };

        seekBarValues = new int[NUM_POINTS];

        for (int i = 0; i < NUM_POINTS; ++i) {
            final Integer index = i;

            seekBars[i].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                int progressChangedValue = 0;

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    progressChangedValue = progress;
                    seekBarLabels[index].setText(Integer.toString(progress));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    seekBarValues[index] = progressChangedValue;
                }
            });
        }

        originalImageView = findViewById(R.id.histogram_specification_original_image);
        resultImageView = findViewById(R.id.histogram_specification_result_image);

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

        referencedHistogramView = findViewById(R.id.histogram_specification_referenced_histogram);
    }

    public void generateEqualizedImage(View view) {
        referencedHistogramView.removeAllSeries();
        new HistogramSpecificationEqualizedTask(
                HistogramSpecificationActivity.this,
                resultImageView,
                seekBarValues,
                referencedHistogramView
        ).execute(HistogramSpecificationActivity.this.imageBitmapOrigin);
    }
}
