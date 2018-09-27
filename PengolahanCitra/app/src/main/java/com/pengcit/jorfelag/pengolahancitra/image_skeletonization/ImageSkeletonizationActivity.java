package com.pengcit.jorfelag.pengolahancitra.image_skeletonization;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.pengcit.jorfelag.pengolahancitra.R;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class ImageSkeletonizationActivity extends AppCompatActivity {

    private ImageView originalImageView, resultImageView;

    private Bitmap originalImageBitmap;
    private Uri imageBitmapOriginURI;

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
    }

    public void skeletonizeImage(View view) {
        new ImageSkeletonizationTask(this, resultImageView).execute(originalImageBitmap);
    }
}
