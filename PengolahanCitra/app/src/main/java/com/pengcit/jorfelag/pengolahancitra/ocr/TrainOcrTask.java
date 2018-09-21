package com.pengcit.jorfelag.pengolahancitra.ocr;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.pengcit.jorfelag.pengolahancitra.util.MonochromeBitmap;

import java.util.List;

import static com.pengcit.jorfelag.pengolahancitra.util.MonochromeBitmap.BLACK;

public class TrainOcrTask extends AsyncTask<Bitmap, Void, ChainCode> {

    private ProgressDialog dialog;
    private List<ChainCode> chainCodes;
    private String label;
    private Editor editor;

    public TrainOcrTask(Activity activity, List<ChainCode> chainCodes, String label, Editor editor) {
        dialog = new ProgressDialog(activity);
        this.chainCodes = chainCodes;
        this.label = label;
        this.editor = editor;
    }

    @Override
    protected void onPreExecute() {
        dialog.setMessage("Saving labeled data...");
        dialog.show();
    }

    protected ChainCode doInBackground(Bitmap... args) {
        final Bitmap imageBitmap = args[0];
        final Bitmap monochromeBitmap = MonochromeBitmap.createMonochromeBitmap(imageBitmap);

        int x = 0;
        int y = 0;
        boolean found = false;

        while (!found && x < monochromeBitmap.getWidth()) {
            y = 0;
            while (!found && y < monochromeBitmap.getHeight()) {
                if ((monochromeBitmap.getPixel(x, y) & 0x000000FF) == BLACK) {
                    found = true;
                } else {
                    y++;
                }
            }
            if (!found) {
                x++;
            }
        }

        if (found) {
            return new ChainCode(monochromeBitmap, x, y, label);
        } else {
            return null;
        }
    }

    protected void onPostExecute(ChainCode chainCode) {
        chainCodes.add(chainCode);
        Gson gson = new Gson();
        String json = gson.toJson(chainCodes);
        editor.putString("ChainCodes", json);
        editor.commit();
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        editor = null;
        dialog = null;
        chainCodes = null;
    }
}
