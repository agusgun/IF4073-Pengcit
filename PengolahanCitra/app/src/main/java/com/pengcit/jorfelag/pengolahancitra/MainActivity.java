package com.pengcit.jorfelag.pengolahancitra;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.pengcit.jorfelag.pengolahancitra.contrast_enhancement.ContrastEnhancementFragment;
import com.pengcit.jorfelag.pengolahancitra.contrast_enhancement.HistogramSpecificationFragment;
import com.pengcit.jorfelag.pengolahancitra.histogram.ShowHistogramFragment;
import com.pengcit.jorfelag.pengolahancitra.ocr.OCRFragment;
import com.pengcit.jorfelag.pengolahancitra.preprocess.PreprocessOperatorFragment;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements
        LoadImageFragment.OnFragmentInteractionListener,
        ShowHistogramFragment.OnFragmentInteractionListener,
        ContrastEnhancementFragment.OnFragmentInteractionListener,
        HistogramSpecificationFragment.OnFragmentInteractionListener,
        OCRFragment.OnFragmentInteractionListener,
        PreprocessOperatorFragment.OnFragmentInteractionListener {

    private TextToSpeech textToSpeech;

    /**
     * Tag for logging.
     */
    private static final String TAG = "MainActivity";

    /**
     * Id to identify a camera permission request.
     */
    private static final int REQUEST_CAMERA = 0;


    /**
     * Id to identify a image capture request.
     */
    private static final int IMAGE_CAPTURE = 1;


    /**
     * Id to identify a select image request.
     */
    private static final int SELECT_IMAGE = 2;

    /**
     * Id to identify a skeletonization request
     */
    private static final int SKELETONIZATION_IMAGE = 3;

    private static final String PREFS_NAME = "ChainCode_Models";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabLayout tabLayout = findViewById(R.id.main_act_tab_layout);

        tabLayout.addTab(tabLayout.newTab().setText("File"));
        tabLayout.addTab(tabLayout.newTab().setText("Show Histogram"));
        tabLayout.addTab(tabLayout.newTab().setText("Enhance Contrast"));
        tabLayout.addTab(tabLayout.newTab().setText("Histogram Specification"));
        tabLayout.addTab(tabLayout.newTab().setText("OCR"));
        tabLayout.addTab(tabLayout.newTab().setText("Preprocess Operator"));

        final ViewPager viewPager = findViewById(R.id.main_act_pager);
        final PagerAdapter adapter = new TabPagerAdapter(
                getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }
        });

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(new Locale("id","ID"));
                }
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CAMERA);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
