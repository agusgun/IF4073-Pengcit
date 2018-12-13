package com.pengcit.jorfelag.pengolahancitra;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Pair;

import java.lang.ref.WeakReference;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.BradleyLocalThreshold;
import Catalano.Imaging.Filters.FourierTransform;
import Catalano.Imaging.Filters.FrequencyFilter;
import Catalano.Imaging.Filters.NiblackThreshold;
import Catalano.Imaging.Filters.SauvolaThreshold;
import Catalano.Imaging.Filters.WolfJolionThreshold;

public class ImageBeautifierTask extends AsyncTask<Bitmap, Void, Bitmap> {

    private WeakReference<ImageBeautifierFragment> fragmentRef;
    private ProgressDialog dialog;

    protected int offset;

    private String methodName;
    private int threshold1;
    private int threshold2;

    public ImageBeautifierTask(ImageBeautifierFragment fr, String methodName, int threshold1, int threshold2) {
        fragmentRef = new WeakReference<>(fr);
        dialog = new ProgressDialog(fr.getContext());
        this.methodName = methodName;
        this.threshold1 = threshold1;
        this.threshold2 = threshold2;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog.setMessage("Processing image, please wait...");
        dialog.show();
    }


    @Override
    protected Bitmap doInBackground(Bitmap... bitmaps) {

        Bitmap inputBitmap = bitmaps[0];

        Bitmap originalBitmap = inputBitmap.copy(inputBitmap.getConfig(), true);

        int height = originalBitmap.getHeight();
        int width = originalBitmap.getWidth();

        FastBitmap fb = new FastBitmap(originalBitmap);
        fb.toGrayscale();


        if (methodName.equals("FFT")) {
            // FFT
            FourierTransform ft = new FourierTransform(fb);
            ft.Forward();
            FrequencyFilter ff = new FrequencyFilter(threshold1, threshold2);
            ff.ApplyInPlace(ft);
            ft.Backward();
            fb = ft.toFastBitmap();
        } else if (methodName.equals("Bradley")) {
            BradleyLocalThreshold blt = new BradleyLocalThreshold();
            blt.applyInPlace(fb);
        } else if (methodName.equals("Sauvola")) {
            SauvolaThreshold st = new SauvolaThreshold();
            st.applyInPlace(fb);
        } else if (methodName.equals("Niblack")) {
            NiblackThreshold nt = new NiblackThreshold();
            nt.applyInPlace(fb);
        } else {
            WolfJolionThreshold wjt = new WolfJolionThreshold();
            wjt.applyInPlace(fb);
        }

        originalBitmap = fb.toBitmap();

        return originalBitmap;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        ImageBeautifierFragment fr = fragmentRef.get();
        if (fr == null
                || fr.getActivity() == null
                || fr.getActivity().isFinishing()) return;

        if (dialog.isShowing()) {
            dialog.dismiss();
        }

        fr.setResultImageView(result);
    }

}
