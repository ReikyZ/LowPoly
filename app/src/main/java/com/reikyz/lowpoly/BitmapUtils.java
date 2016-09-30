package com.reikyz.lowpoly;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

/**
 * Created by reikyZ on 16/10/1.
 */

public class BitmapUtils {
    public static Bitmap loadBitmapRes(Context context, int resID) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeResource(context.getResources(), resID, options);
    }

    public static Bitmap zoomBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bitmapOut = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return bitmapOut;
    }

}
