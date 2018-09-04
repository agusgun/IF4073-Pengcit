package com.pengcit.jorfelag.pengolahancitra.contrast.enhancement;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.pengcit.jorfelag.pengolahancitra.R;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class ContrastEnhancementActivity extends AppCompatActivity {

    Bitmap imageBitmapOrigin;
    Uri imageBitmapOriginURI;
    ImageView originalImageView, resultImageView;
    Spinner methodListSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_constrast_enhancement);

        originalImageView = (ImageView) findViewById(R.id.contrast_enhancement_imageview_origin);
        resultImageView = (ImageView) findViewById(R.id.contrast_enhancement_imageview_result);

        // Get Original Image
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

        // Spinner
        methodListSpinner = (Spinner) findViewById(R.id.contrast_enhancement_spinner);
        ArrayAdapter<CharSequence> methodListAdapter = ArrayAdapter.createFromResource(this,
                R.array.contrast_enhancement_method, R.layout.support_simple_spinner_dropdown_item);
        methodListAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        methodListSpinner.setAdapter(methodListAdapter);
        methodListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    //Todo jordhy: Metode 1
                    new LinearStretchingTask(ContrastEnhancementActivity.this, resultImageView).execute(ContrastEnhancementActivity.this.imageBitmapOrigin);
                } else if (position == 1) {
                    //Todo jordhy: Metode 2
                    new HistogramEqualizationTask(ContrastEnhancementActivity.this, resultImageView).execute(ContrastEnhancementActivity.this.imageBitmapOrigin);
                } else if (position == 2){
                    //Todo jordhy: Metode 3
                    new LogTransformationTask(ContrastEnhancementActivity.this, resultImageView).execute(ContrastEnhancementActivity.this.imageBitmapOrigin);
                } else if (position == 3) {
                    new PowerLawTask(ContrastEnhancementActivity.this, resultImageView).execute(ContrastEnhancementActivity.this.imageBitmapOrigin);
                } else if (position == 4) {
                    new LogTransformationTask(ContrastEnhancementActivity.this, resultImageView).execute(ContrastEnhancementActivity.this.imageBitmapOrigin);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

}
