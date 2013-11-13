package com.phonehalo.itemtracker.utility;

import android.content.Context;
import android.util.DisplayMetrics;
import java.lang.String;

public class DisplayMetricsHelper {

    public static String LOG_TAG = "DisplayMetricsHelper";

    public static void logDisplayMetricsSummary (Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

        Log.d(LOG_TAG, "Raw Display Metrics: " + displayMetrics);
        Log.d(LOG_TAG, "Display W Dp: " + DisplayMetricsHelper.convertPixelsToDp(displayMetrics.widthPixels, context) + "(This is what is used for the res/drawable-swXXX dirs)");
        Log.d(LOG_TAG, "Display H Dp: " + DisplayMetricsHelper.convertPixelsToDp(displayMetrics.heightPixels, context));

    }

    public static float convertPixelsToDp(int px,Context context)
    {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;

    }

    public static float convertDpToPixel(int dp,Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float px = dp * (metrics.densityDpi/160f);
        return px;
    }
}
