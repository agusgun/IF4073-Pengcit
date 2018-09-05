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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.pengcit.jorfelag.pengolahancitra.MainActivity;
import com.pengcit.jorfelag.pengolahancitra.R;

import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class ContrastEnhancementActivity extends AppCompatActivity {

    Bitmap imageBitmapOrigin;
    Uri imageBitmapOriginURI;
    ImageView originalImageView, resultImageView;
    Spinner methodListSpinner;

    EditText weightEditText;
    Button generateImageButton;

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

        //Weight
        weightEditText = (EditText) findViewById(R.id.contrast_enchancement_weight);
        generateImageButton = (Button) findViewById(R.id.contrast_enchancement_generate_image);

        weightEditText.setVisibility(View.GONE);
        generateImageButton.setVisibility(View.GONE);

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
                    new LinearStretchingTask(ContrastEnhancementActivity.this, resultImageView).execute(ContrastEnhancementActivity.this.imageBitmapOrigin);
                } else if (position == 1) {
                    weightEditText.setVisibility(View.VISIBLE);
                    generateImageButton.setVisibility(View.VISIBLE);
                    resultImageView.setVisibility(View.GONE);
                } else if (position == 2){
                    new LogTransformationTask(ContrastEnhancementActivity.this, resultImageView).execute(ContrastEnhancementActivity.this.imageBitmapOrigin);
                } else if (position == 3) {
                    new PowerLawTask(ContrastEnhancementActivity.this, resultImageView).execute(ContrastEnhancementActivity.this.imageBitmapOrigin);
                } else if (position == 4) {
                    new NegativeTask(ContrastEnhancementActivity.this, resultImageView).execute(ContrastEnhancementActivity.this.imageBitmapOrigin);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void histogramEqualizationGenerate(View view) {
        float weight = Float.parseFloat(weightEditText.getText().toString());
        Toast.makeText(this, "Value" + weight, Toast.LENGTH_SHORT).show();

        new HistogramEqualizationTask(ContrastEnhancementActivity.this, resultImageView, weight).execute(ContrastEnhancementActivity.this.imageBitmapOrigin);
        resultImageView.setVisibility(View.VISIBLE);
    }
}
