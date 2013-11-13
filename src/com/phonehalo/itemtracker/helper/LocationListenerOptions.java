package com.phonehalo.itemtracker.helper;

import android.content.Context;

class LocationListenerOptions {
    private int preferredAccuracyMeters = 200;
    private int betterLocationAccuracyMinDifference = 100;
    private int shortTermBetterLocationAccuracyMinDifference = 10;
    private int maxNumberFixes = 5;
    private int minSearchTime = 10 * 1000;
    private int maxSearchTime = 5 * 60 * 1000;
    private int failsafeShutdownTime = 6 * 60 * 1000;
    private final Context context;
    private final String deviceAddress;
    private final boolean connected;
    private final long timeLocationRequested;

    public LocationListenerOptions(
            Context context,
            String deviceAddress,
            boolean connected,
            long timeLocationRequested,
            int preferredAccuracyMeters,
            int betterLocationAccuracyMinDifference,
            int shortTermBetterLocationAccuracyMinDifference,
            int maxNumberFixes,
            int minSearchTime,
            int maxSearchTime,
            int failsafeShutdownTime
    ) {
        this.context = context;
        this.deviceAddress = deviceAddress;
        this.connected = connected;
        this.timeLocationRequested = timeLocationRequested;
        this.betterLocationAccuracyMinDifference = betterLocationAccuracyMinDifference;
        this.failsafeShutdownTime = failsafeShutdownTime;
        this.maxNumberFixes = maxNumberFixes;
        this.maxSearchTime = maxSearchTime;
        this.minSearchTime = minSearchTime;
        this.preferredAccuracyMeters = preferredAccuracyMeters;
        this.shortTermBetterLocationAccuracyMinDifference = shortTermBetterLocationAccuracyMinDifference;
    }

    public boolean isConnected() {
        return connected;
    }

    public Context getContext() {
        return context;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public long getTimeLocationRequested() {
        return timeLocationRequested;
    }

    public int getBetterLocationAccuracyMinDifference() {
        return betterLocationAccuracyMinDifference;
    }

    public int getFailsafeShutdownTime() {
        return failsafeShutdownTime;
    }

    public int getMaxNumberFixes() {
        return maxNumberFixes;
    }

    public int getMaxSearchTime() {
        return maxSearchTime;
    }

    public int getMinSearchTime() {
        return minSearchTime;
    }

    public int getPreferredAccuracyMeters() {
        return preferredAccuracyMeters;
    }

    public int getShortTermBetterLocationAccuracyMinDifference() {
        return shortTermBetterLocationAccuracyMinDifference;
    }

    @Override
    public String toString() {
        return "LocationListenerOptions{" +
                "betterLocationAccuracyMinDifference=" + betterLocationAccuracyMinDifference +
                ", preferredAccuracyMeters=" + preferredAccuracyMeters +
                ", shortTermBetterLocationAccuracyMinDifference=" + shortTermBetterLocationAccuracyMinDifference +
                ", maxNumberFixes=" + maxNumberFixes +
                ", minSearchTime=" + minSearchTime +
                ", maxSearchTime=" + maxSearchTime +
                ", failsafeShutdownTime=" + failsafeShutdownTime +
                ", context=" + context +
                ", deviceAddress=" + deviceAddress +
                ", connected=" + connected +
                ", timeLocationRequested=" + timeLocationRequested +
                '}';
    }
}
