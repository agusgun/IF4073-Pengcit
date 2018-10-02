package com.pengcit.jorfelag.pengolahancitra.ocr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pengcit.jorfelag.pengolahancitra.R;
import com.pengcit.jorfelag.pengolahancitra.histogram.specification.HistogramSpecificationActivity;
import com.pengcit.jorfelag.pengolahancitra.histogram.specification.HistogramSpecificationEqualizedTask;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class OCRActivity extends AppCompatActivity {

    Bitmap imageBitmapOrigin;
    Uri imageBitmapOriginURI;
    ImageView originalImageView;
    TextView digitTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);

        originalImageView = (ImageView) findViewById(R.id.ocr_original_image_view);
        digitTextView = (TextView) findViewById(R.id.ocr_digit_text_view);

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
    }


    public void generateDigit(View view) {
        new OCRDigitTask(
                OCRActivity.this,
                digitTextView
        ).execute(OCRActivity.this.imageBitmapOrigin);
    }
}
