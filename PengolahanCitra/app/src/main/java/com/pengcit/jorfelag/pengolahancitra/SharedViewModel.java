package com.pengcit.jorfelag.pengolahancitra;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.graphics.Bitmap;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<Bitmap> bitmapLiveData = new MutableLiveData<>();

    public void setBitmap(Bitmap bitmap) {
        bitmapLiveData.setValue(bitmap);
    }

    public LiveData<Bitmap> getBitmapLiveData() {
        return bitmapLiveData;
    }
}
