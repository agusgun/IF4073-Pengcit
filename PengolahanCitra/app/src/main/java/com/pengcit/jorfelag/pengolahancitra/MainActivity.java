package com.pengcit.jorfelag.pengolahancitra;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private Uri file;
    String mCurrentImagePath;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageview);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CAMERA);
        }
    }

    /**
     * Called when the 'take a picture!' button is clicked.
     * Callback is defined in resource layout definition.
     */
    public void takePicture(View view) {
        Log.i(TAG, "Take a picture! button pressed. Checking permission.");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "CAMERA permission has NOT been granted. Requesting permission.");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA);
        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                File imageFile = null;
                try {
                    imageFile = createImageFile();
                } catch (IOException ex) {
                    Log.e(TAG, "Failed to create image file");
                }

                if (imageFile != null) {
                    Uri imageURI = FileProvider.getUriForFile(this,
                            "com.pengcit.jorfelag.pengolahancitra.provider",
                            imageFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI);
                    startActivityForResult(intent, IMAGE_CAPTURE);
                }
            } else {
                Log.i(TAG, "No activity available to resolve intent");
            }
        }
    }

    /**
     * Called when the 'select a picture!' button is clicked.
     * Callback is defined in resource layout definition.
     */
    public void selectPicture(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Uri imageUri = null;
            if (requestCode == IMAGE_CAPTURE) {
                File imageFile = new File(mCurrentImagePath);
                if (imageFile.exists()) {
                    imageUri = Uri.fromFile(imageFile);
                    imageView.setRotation(90);
                }
            } else if (requestCode == SELECT_IMAGE) {
                imageUri = data.getData();
                imageView.setRotation(0);
            }
            if (imageUri != null) {
                imageView.setImageURI(imageUri);
                try {
                    InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    Bitmap imageBitmap = BitmapFactory.decodeStream(imageStream);
                    new CreateImageHistogramTask(this).execute(imageBitmap);
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "File not found for image uri " + imageUri);
                    e.printStackTrace();
                }
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        mCurrentImagePath = image.getAbsolutePath();
        return image;
    }
}
