package com.pengcit.jorfelag.pengolahancitra;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Button takePictureButton;
    private ImageView imageView;
    private Uri file;

    private static final int REQUEST_IMAGE_CAPTURE = 100;
    private static final int PICK_IMAGE = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        takePictureButton = (Button) findViewById(R.id.button_image);
        imageView = (ImageView) findViewById(R.id.imageview);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            takePictureButton.setEnabled(false);
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                takePictureButton.setEnabled(true);
            }
        }
    }

    public void takePicture(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Todo: save to URI
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);

            Map<String, Integer[]> rgbgrayHashMap = getRGBGrayHashMap(imageBitmap);
            Integer[] redQuantityArray = rgbgrayHashMap.get("red");
            Integer[] greenQunatityArray = rgbgrayHashMap.get("green");
            Integer[] blueQuantityArray = rgbgrayHashMap.get("blue");
            Integer[] grayQuantityArray = rgbgrayHashMap.get("gray");

            //Todo jordhy: show histogram
            for (int i = 0; i < 256; i++) {
                System.out.println(redQuantityArray[i] + " " + greenQunatityArray[i] + " " + blueQuantityArray[i] + " " + grayQuantityArray[i]);
            }
        } else if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap imageBitmap = BitmapFactory.decodeStream(imageStream);
                imageView.setImageBitmap(imageBitmap);

                Map<String, Integer[]> rgbgrayHashMap = getRGBGrayHashMap(imageBitmap);
                Integer[] redQuantityArray = rgbgrayHashMap.get("red");
                Integer[] greenQunatityArray = rgbgrayHashMap.get("green");
                Integer[] blueQuantityArray = rgbgrayHashMap.get("blue");
                Integer[] grayQuantityArray = rgbgrayHashMap.get("gray");

                for (int i = 0; i < 256; i++) {
                    System.out.println(redQuantityArray[i] + " " + greenQunatityArray[i] + " " + blueQuantityArray[i] + " " + grayQuantityArray[i]);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            //Todo jordhy: add here also
        }
    }

    private Map<String, Integer[]> getRGBGrayHashMap(Bitmap imageBitmap) {
        Map<String, Integer[]> results = new HashMap<>();
        Integer[] redQuantityArray = new Integer[256];
        Integer[] greenQuantityArray = new Integer[256];
        Integer[] blueQuantityArray = new Integer[256];
        Integer[] grayQuantityArray = new Integer[256];

        for (int i = 0; i < 256; i++) {
            redQuantityArray[i] = 0;
            greenQuantityArray[i] = 0;
            blueQuantityArray[i] = 0;
            grayQuantityArray[i] = 0;
        }

        Bitmap processedBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);
        for (int x = 0; x < processedBitmap.getWidth(); x++) {
            for (int y = 0; y < processedBitmap.getHeight(); y++) {
                int pixelColor = processedBitmap.getPixel(x, y);

                int red = (pixelColor& 0x00FF0000) >> 16;
                int green = (pixelColor & 0x0000FF00) >> 8;
                int blue = (pixelColor & 0x000000FF);
                int gray = (red + green + blue) / 3;

                redQuantityArray[red]++;
                greenQuantityArray[green]++;
                blueQuantityArray[blue]++;
                grayQuantityArray[gray]++;
            }
        }

        results.put("red", redQuantityArray);
        results.put("green", greenQuantityArray);
        results.put("blue", blueQuantityArray);
        results.put("gray", grayQuantityArray);
        return results;
    }

    public void selectPicture(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }
}
