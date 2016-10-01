package com.reikyz.lowpoly;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Int2;
import android.support.v8.renderscript.RenderScript;
import android.util.Log;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by reikyZ on 16/10/1.
 */

public class LowPoly {
    final static String TAG = "==LowPoly==";

    private static Allocation allocationOriginal;
    private static Allocation allocationGrayed;
    private static Allocation allocationSobel;
    private static RenderScript mRs;
    private static ScriptC_lowpoly scriptLowPoly;
    private static int width, height;

    private static Bitmap mBitmapIn;
    public static Bitmap bmpRendered;

    private static int pointCount;
    private static int groupCount = 100;
    private static Int2[] points = new Int2[10000];
    private static List<Int2> pointz = new ArrayList<Int2>();


    public static void createLowPolyBmp(Context context, Bitmap bitmapIn, int accuracy) {
        mBitmapIn = bitmapIn;

        Bitmap bitmapOut = Bitmap.createBitmap(bitmapIn.getWidth(), bitmapIn.getHeight(),
                bitmapIn.getConfig());
        width = bitmapIn.getWidth();
        height = bitmapIn.getHeight();
        Log.e(TAG, "Width==" + width + "==Height==" + height + "==accuracy==" + accuracy);
        createLowPolyScript(context, accuracy, bitmapIn, bitmapOut);

        Log.e(TAG, "Start GRAYED");
        scriptLowPoly.invoke_process(1);
    }

    private static void createLowPolyScript(Context context, int accuracy, final Bitmap bitmapIn, final Bitmap bitmapOut) {
        mRs = RenderScript.create(context);

        allocationOriginal = Allocation.createFromBitmap(mRs, bitmapIn,
                Allocation.MipmapControl.MIPMAP_NONE,
                Allocation.USAGE_SCRIPT);
        allocationGrayed = Allocation.createFromBitmap(mRs, bitmapOut,
                Allocation.MipmapControl.MIPMAP_NONE,
                Allocation.USAGE_SCRIPT);
        allocationSobel = Allocation.createFromBitmap(mRs, bitmapOut,
                Allocation.MipmapControl.MIPMAP_NONE,
                Allocation.USAGE_SCRIPT);

        scriptLowPoly = new ScriptC_lowpoly(mRs);

        scriptLowPoly.set_gScript(scriptLowPoly);
        scriptLowPoly.set_gOriginal(allocationOriginal);
        scriptLowPoly.set_gGrayed(allocationGrayed);
        scriptLowPoly.set_gSobel(allocationSobel);

        scriptLowPoly.set_accuracy(accuracy);
        scriptLowPoly.set_width(width);
        scriptLowPoly.set_height(height);


        mRs.setMessageHandler(new RenderScript.RSMessageHandler() {
            @Override
            public void run() {
                super.run();
                if (mID == 101) {
                    Log.e(TAG, "GRAYED finish");
//                    allocationGrayed.copyTo(bitmapOut);
                    Log.e(TAG, "Start SOBEL");
                    scriptLowPoly.invoke_process(2);
                    return;
                }
                if (mID == 102) {
                    Log.e(TAG, "SOBEL finish");
//                    allocationSobel.copyTo(bitmapOut);
                    scriptLowPoly.invoke_send_points();

                    points = new Int2[10000];
                    pointz.clear();
                    return;
                }

                if (mID == 0) {
                    pointCount = mData[0];
                    groupCount = mLength;
                    Log.e(TAG, "Receive points==" + pointCount + "==by group==" + groupCount);
                } else if (mID == groupCount) {
                    for (int i = 0; i < mData.length; i += 2) {
                        points[i / 2 + 625 * (mID - 1)] = new Int2(mData[i], mData[i + 1]);
                    }
                    for (int i = 0; i < pointCount; i++) {
                        Int2 int2 = points[i];
                        pointz.add(int2);
                    }

                    for (int i = 0; i < 200; i++) {
                        Int2 int2 = new Int2((int) (Math.random() * width), (int) (Math.random() * height));
                        pointz.add(int2);
                    }


                    pointz.add(new Int2(0, 0));
                    pointz.add(new Int2(0, height));
                    pointz.add(new Int2(width, 0));
                    pointz.add(new Int2(width, height));

                    Log.e(TAG, "Points size==" + pointz.size() + "");

                    List<Integer> tris = Delaunay.triangulate(pointz);

                    Log.e(TAG, "Triangle size== " + tris.size() / 3 + "");

                    bmpRendered = Bitmap.createBitmap((int) (width), (int) (height), Bitmap.Config.ARGB_8888);

                    long t = System.currentTimeMillis();

                    Canvas canvas = new Canvas(bmpRendered);
                    Paint paint = new Paint();
                    paint.setAntiAlias(true);
                    paint.setStyle(Paint.Style.FILL);

                    float x1, x2, x3, y1, y2, y3, cx, cy;
                    for (int i = 0; i < tris.size(); i += 3) {
                        x1 = pointz.get(tris.get(i)).x;
                        x2 = pointz.get(tris.get(i + 1)).x;
                        x3 = pointz.get(tris.get(i + 2)).x;
                        y1 = pointz.get(tris.get(i)).y;
                        y2 = pointz.get(tris.get(i + 1)).y;
                        y3 = pointz.get(tris.get(i + 2)).y;

                        cx = (x1 + x2 + x3) / 3;
                        cy = (y1 + y2 + y3) / 3;

                        Path path = new Path();
                        path.moveTo(x1, y1);
                        path.lineTo(x2, y2);
                        path.lineTo(x3, y3);
                        path.close();

                        paint.setColor(mBitmapIn.getPixel((int) cx, (int) cy));

                        canvas.drawPath(path, paint);
                    }
                    Log.e(TAG, "Canvas cost === " + (System.currentTimeMillis() - t) + " ms");

                    MainActivity.mHandler.sendEmptyMessageAtTime(0, 0);

                    System.gc();
                } else {
                    Log.e(TAG, "Receive group==" + mID);
                    for (int i = 0; i < mData.length; i += 2) {
                        points[i / 2 + 625 * (mID - 1)] = new Int2(mData[i], mData[i + 1]);
                    }
                }
            }
        });
    }
}
