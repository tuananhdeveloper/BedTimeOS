package com.example.bedtimeios.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

public class Converter {
    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static int pixelsToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static int dpToPixels(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static double degreesToRadians(float degrees) {
        return (double) degrees*Math.PI/180;
    }

    public static float radiansToDegrees(double radians) {
        return (float) ((radians*180)/Math.PI);
    }

    public static double coordinatesToDegrees(float originX, float originY, float touchX, float touchY) {
        float radius = (float) Math.sqrt(Math.pow(touchX - originX, 2) + Math.pow(touchY - originY, 2));
        double degrees = 0;
        if (touchX - originX > 0) {
            if (touchY - originY  > 0) {
                degrees = Converter.radiansToDegrees(Math.asin(Math.abs(touchY - originY)/radius));
            }
            else {
                degrees = - Converter.radiansToDegrees(Math.asin(Math.abs(touchY - originY)/radius));
            }
        }
        else {
            if (touchY - originY > 0) {
                degrees = 90 + Converter.radiansToDegrees(Math.asin(Math.abs(touchX - originX)/radius));
            }
            else {
                degrees = 180 + 90 - Converter.radiansToDegrees(Math.asin(Math.abs(touchX - originX)/radius));
            }
        }
        return degrees;
    }

    public static final String minutesToHHmm(int m) {
        String hours = (m/60)%60 + "";
        String minutes = m%60 + "";

        if (hours.length() < 2) hours = "0" + hours;
        if (minutes.length() < 2) minutes = "0" + minutes;
        return hours + ":" + minutes;
    }
}
