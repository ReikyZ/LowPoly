package com.reikyz.lowpoly;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    final static String TAG = "==MainActivity==";

    static ImageView ivOriginal, ivOut;
    Bitmap bitmapOriginal;

    int acc = 10;
    static long time = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ivOut = (ImageView) findViewById(R.id.iv_out);

        bitmapOriginal = BitmapUtils.loadBitmapRes(this, R.mipmap.umbrela);
        Log.e(TAG, Utils.getLineNumber(new Exception()));

        time = System.currentTimeMillis();
        ivOut.setBackgroundResource(R.mipmap.umbrela);
        ivOut.setImageBitmap(LowPoly.createLowPolyBmp(this, bitmapOriginal, acc));

        ivOut.setOnClickListener(this);

    }

    static Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    ivOut.setImageBitmap(LowPoly.bmpRendered);
                    Log.e(TAG, "Render FINISH in==" + (System.currentTimeMillis() - time) + " ms" + Utils.getLineNumber(new Exception()));
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        time = System.currentTimeMillis();
        Log.e(TAG, "START Render by Accuracy=== " + acc);
        ivOut.setImageBitmap(LowPoly.createLowPolyBmp(this, bitmapOriginal, acc--));
        if (acc < 1) acc = 10;
    }
}
