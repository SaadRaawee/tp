package com.phonehalo.itemtracker.helper;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import com.phonehalo.itemtracker.utility.Log;

import java.util.Timer;
import java.util.TimerTask;

class ItemTrackerServiceLocationListener implements android.location.LocationListener {

    int numberOfFixes = 0;
    int numberPreferredFixes = 0;
    Location bestLocation = null;
    boolean removeUpdatesAfterMinTime = false;

    private final Timer shutdownWatchdog;
    private static final String LOG_TAG = "ItemTrackerServiceLocationListener";
    private final LocationListenerOptions options;
    private final LocationHelper locationHelper;


    public ItemTrackerServiceLocationListener(LocationHelper locationHelper, final LocationListenerOptions options) {
        super();
        Log.v(LOG_TAG, "Options: " + options);
        this.options = options;
        this.locationHelper = locationHelper;

        //make sure we shut down this listener, even if we never get a location
        shutdownWatchdog = new Timer();
        shutdownWatchdog.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.w(LOG_TAG, "Finishing due to failsafe timer");
                finishListening();
            }
        }, options.getFailsafeShutdownTime());

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.v(LOG_TAG, "onLocationChanged(): " + options.getTimeLocationRequested() + "/" + options.getDeviceAddress() + "/" + location);
        numberOfFixes++;
        boolean persistLocation = false;

        if (bestLocation == null && location.hasAccuracy()) {
            Log.v(LOG_TAG, "improved fix #" + numberOfFixes + " because it is the first fix");
            persistLocation = true;
        }

        if (bestLocation != null && location.hasAccuracy() &&
                location.getAccuracy() + options.getBetterLocationAccuracyMinDifference() < bestLocation.getAccuracy()) {
            Log.v(LOG_TAG, "improved fix #" + numberOfFixes + " because it is more accurate; old/new: " + bestLocation.getAccuracy() + "/" + location.getAccuracy());
            persistLocation = true;
        }

        if (location.hasAccuracy()
                && location.getAccuracy() <= options.getPreferredAccuracyMeters()
                && numberPreferredFixes == 0) {
            //this location is good-enough and we don't already have a good-enough fix,
            // store it and know we can stop searching at min time
            Log.v(LOG_TAG, "improved fix #" + numberOfFixes + " because it is the first preferred fix; accuracy: " + location.getAccuracy());
            persistLocation = true;
            removeUpdatesAfterMinTime = true;
        }

        //always count the preferred fixes
        if (location.hasAccuracy()
                && location.getAccuracy() <= options.getPreferredAccuracyMeters()) {
            numberPreferredFixes++;
            Log.v(LOG_TAG, "Preferred fixes: " + numberPreferredFixes);
        }

        if (bestLocation != null && bestLocation.hasAccuracy() && location.hasAccuracy()
                && bestLocation.getAccuracy() - location.getAccuracy() >= options.getShortTermBetterLocationAccuracyMinDifference()
                && System.currentTimeMillis() - options.getTimeLocationRequested() <= options.getMinSearchTime()) {
            //we have a slightly better reading in a short amount of time, so use it
            persistLocation = true;
        }

        if (numberOfFixes >= options.getMaxNumberFixes()) {
            removeUpdatesAfterMinTime = true;
        }

        if (persistLocation) {
            Log.v(LOG_TAG, "persisting location on fix # " + numberOfFixes);
            bestLocation = location;
            locationHelper.setPeripheralLocation(options.getDeviceAddress(), bestLocation);
        }

        long listeningTime = location.getTime() - options.getTimeLocationRequested();
        if ((removeUpdatesAfterMinTime && listeningTime > options.getMinSearchTime())
                || listeningTime > options.getMaxSearchTime()) {
            finishListening();
        }
    }

    private void finishListening() {
        Log.v(LOG_TAG, "removing location updates");
        LocationManager locationManager = (LocationManager) options.getContext().getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(this);
        shutdownWatchdog.cancel();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        String statusName = "None";
        switch (status) {
            case LocationProvider.AVAILABLE:
                statusName = "Available";
                break;
            case LocationProvider.OUT_OF_SERVICE:
                statusName = "Out of Service";
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                statusName = "Temporarily Unavailable";
                break;
        }
        Log.v(LOG_TAG, "location provider/status: " + provider + "/" + statusName);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.v(LOG_TAG, "location provider enabled: " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.v(LOG_TAG, "location provider disabled: " + provider);
    }
}
