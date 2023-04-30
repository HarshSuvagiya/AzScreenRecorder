package com.scorpion.screenrecorder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SRC_Helper {

    public static int height, width;
    public static int isFromScreen;

    public static void FS(Activity mActivity) {
//        mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public static void FS2(Activity mActivity) {
//        mActivity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    public static String formatLength(long millis) {
        @SuppressLint("DefaultLocale") String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        return hms;
    }

    public static void gone(View view) {
        if (view != null) {
            view.setVisibility(View.GONE);
        }
    }

    public static void visible(View view) {
        if (view != null) {
            view.setVisibility(View.VISIBLE);
        }
    }


    public static String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy , HH:mm", Locale.getDefault());
        Date today = Calendar.getInstance().getTime();
        return dateFormat.format(today);
    }

    public static void getHeightAndWidth(Context context) {

        getHeight(context);
        getWidth(context);
    }

    public static int getWidth(Context context) {
        width = context.getResources().getDisplayMetrics().widthPixels;
        return width;
    }

    public static int getHeight(Context context) {
        height = context.getResources().getDisplayMetrics().heightPixels;
        return height;
    }

    public static int setHeight(int h) {

        return (height * h) / 1920;

    }

    public static int setWidth(int w) {
        return (width * w) / 1080;

    }

    public static void setSize(View view, int width, int height) {

        view.getLayoutParams().height = setHeight(height);
        view.getLayoutParams().width = setWidth(width);

    }

    public static void setSize(View view, int width, int height, boolean sameheightandwidth) {

        if (sameheightandwidth) {
            view.getLayoutParams().height = setWidth(height);
            view.getLayoutParams().width = setWidth(width);
        } else {
            view.getLayoutParams().height = setHeight(height);
            view.getLayoutParams().width = setHeight(width);
        }

    }

    public static void setMargin(View view, int left, int top, int right, int bottom) {
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        marginLayoutParams.setMargins(setWidth(left), setWidth(top), setWidth(right), setWidth(bottom));
    }

}
