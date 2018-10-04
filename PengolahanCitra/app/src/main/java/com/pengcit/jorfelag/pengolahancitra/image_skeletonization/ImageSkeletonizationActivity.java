package com.pengcit.jorfelag.pengolahancitra.image_skeletonization;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.pengcit.jorfelag.pengolahancitra.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ImageSkeletonizationActivity extends AppCompatActivity {

    private ImageView originalImageView, resultImageView;
    private TextView resultTextView;

    private Bitmap originalImageBitmap;
    private Uri imageBitmapOriginURI;

    private SeekBar seekBar;
    private int seekBarValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_skeletonization);

        originalImageView = findViewById(R.id.skeletonization_original_image);
        resultImageView = findViewById(R.id.skeletonization_result_image);

        Intent intent = getIntent();
        imageBitmapOriginURI = Uri.parse(intent.getExtras().getString("BitmapImageURI"));
        InputStream imageStream = null;
        try {
            imageStream = getContentResolver().openInputStream(imageBitmapOriginURI);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        originalImageBitmap = BitmapFactory.decodeStream(imageStream);
        originalImageView.setImageBitmap(originalImageBitmap);

        SeekBar seekBar = findViewById(R.id.skeletonization_seekbar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                seekBarValue = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(getApplicationContext(), Integer.toString(seekBarValue), Toast.LENGTH_SHORT).show();
            }
        });

        resultTextView = findViewById(R.id.skeletonization_text_view);
    }

    public void skeletonizeImage(View view) {
        new ImageSkeletonizationTask(this, resultImageView, resultTextView, seekBarValue).execute(originalImageBitmap);
    }

}
