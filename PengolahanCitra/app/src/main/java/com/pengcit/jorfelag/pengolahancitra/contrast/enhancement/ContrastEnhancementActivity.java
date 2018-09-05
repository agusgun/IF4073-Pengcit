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

    SeekBar pointSeekBar1, pointSeekBar2, pointSeekBar3;
    TextView detailSeekBar1, detailSeekBar2, detailSeekBar3;
    int seekBarValue1, seekBarValue2, seekBarValue3;

    RelativeLayout equalizerLayout;
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

        //Weight and Equalizer
        weightEditText = (EditText) findViewById(R.id.contrast_enchancement_weight);
        generateImageButton = (Button) findViewById(R.id.contrast_enchancement_generate_image);
        equalizerLayout = (RelativeLayout) findViewById(R.id.equalizer_layout);

        weightEditText.setVisibility(View.GONE);
        generateImageButton.setVisibility(View.GONE);
        equalizerLayout.setVisibility(View.GONE);

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
                    equalizerLayout.setVisibility(RelativeLayout.VISIBLE);
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

        pointSeekBar1 = (SeekBar) findViewById(R.id.contrast_enchancement_seekbar_point_1);
        pointSeekBar2 = (SeekBar) findViewById(R.id.contrast_enchancement_seekbar_point_2);
        pointSeekBar3 = (SeekBar) findViewById(R.id.contrast_enchancement_seekbar_point_3);

        detailSeekBar1 = (TextView) findViewById(R.id.contrast_enchancement_detail_1);
        detailSeekBar2 = (TextView) findViewById(R.id.contrast_enchancement_detail_2);
        detailSeekBar3 = (TextView) findViewById(R.id.contrast_enchancement_detail_3);

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

    public void histogramEqualizationGenerate(View view) {
        float weight = Float.parseFloat(weightEditText.getText().toString());
        Toast.makeText(this, "Value" + weight, Toast.LENGTH_SHORT).show();

        new HistogramEqualizationTask(ContrastEnhancementActivity.this, resultImageView, weight).execute(ContrastEnhancementActivity.this.imageBitmapOrigin);
        resultImageView.setVisibility(View.VISIBLE);
    }
}
