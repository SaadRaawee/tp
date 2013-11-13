package com.phonehalo.itemtracker.utility;

import com.phonehalo.itemtracker.BuildConfig;

public final class Log {

    private static final String APP_BASE_NAME = "PhoneHalo/ItemTracker/";

    public static void d(String pfx, Object message) {
        if (BuildConfig.DEBUG) {
            android.util.Log.d(APP_BASE_NAME + pfx, String.valueOf(message));
        }
    }

    public static void d(String pfx, Object message, Throwable t) {
        if (BuildConfig.DEBUG) {
            android.util.Log.d(APP_BASE_NAME + pfx, String.valueOf(message), t);
        }
    }

    public static void e(String pfx, Object message) {
        if (BuildConfig.DEBUG) {
            android.util.Log.e(APP_BASE_NAME + pfx, String.valueOf(message));
        }
    }

    public static void e(String pfx, Object message, Throwable t) {
        if (BuildConfig.DEBUG) {
            android.util.Log.e(APP_BASE_NAME + pfx, String.valueOf(message), t);
        }
    }

    public static void v(String pfx, Object message) {
        if (BuildConfig.DEBUG) {
            android.util.Log.v(APP_BASE_NAME + pfx, String.valueOf(message));
        }
    }

    public static void i(String pfx, Object message) {
        if (BuildConfig.DEBUG) {
            android.util.Log.i(APP_BASE_NAME + pfx, String.valueOf(message));
        }
    }

    public static void w(String pfx, Object message) {
        if (BuildConfig.DEBUG) {
            android.util.Log.w(APP_BASE_NAME + pfx, String.valueOf(message));
        }
    }

    public static void fauxException(String logTag, String message) {
        Exception e = new Exception("fauxException");
        Log.e(logTag, message, e);
    }
}
