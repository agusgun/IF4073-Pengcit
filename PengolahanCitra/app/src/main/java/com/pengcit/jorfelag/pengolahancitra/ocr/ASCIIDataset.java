package com.pengcit.jorfelag.pengolahancitra.ocr;

import android.content.Context;

import com.pengcit.jorfelag.pengolahancitra.R;
import com.pengcit.jorfelag.pengolahancitra.util.dataset.CsvDataset;
import com.pengcit.jorfelag.pengolahancitra.util.dataset.Dataset;

import java.io.IOException;
import java.io.InputStream;

public class ASCIIDataset {

    public static Dataset instance;

    private ASCIIDataset() {}

    public static void init(Context context) throws IOException {
        instance = new CsvDataset();
        InputStream is = context.getResources().openRawResource(R.raw.ascii_features);
        instance.load(is);
    }
}
