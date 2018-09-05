package com.pengcit.jorfelag.pengolahancitra;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class HistogramSpecificationActivity extends AppCompatActivity {

    SeekBar pointSeekBar1, pointSeekBar2, pointSeekBar3;
    TextView detailSeekBar1, detailSeekBar2, detailSeekBar3;
    int seekBarValue1, seekBarValue2, seekBarValue3;
    RelativeLayout equalizerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_histogram_specification);

        equalizerLayout = (RelativeLayout) findViewById(R.id.equalizer_layout);
        equalizerLayout.setVisibility(View.GONE);

        pointSeekBar1 = (SeekBar) findViewById(R.id.histogram_spesification_seekbar_point_1);
        pointSeekBar2 = (SeekBar) findViewById(R.id.histogram_spesification_seekbar_point_2);
        pointSeekBar3 = (SeekBar) findViewById(R.id.histogram_spesification_seekbar_point_3);

        detailSeekBar1 = (TextView) findViewById(R.id.histogram_spesification_detail_1);
        detailSeekBar2 = (TextView) findViewById(R.id.histogram_spesification_detail_2);
        detailSeekBar3 = (TextView) findViewById(R.id.histogram_spesification_detail_3);

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
    }
}
