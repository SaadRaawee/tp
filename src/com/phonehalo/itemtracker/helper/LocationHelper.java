package com.phonehalo.itemtracker.helper;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import com.phonehalo.itemtracker.db.ItemTrackerPersistenceHelper;
import com.phonehalo.itemtracker.utility.Log;

import java.util.HashMap;
import java.util.Map;

public class LocationHelper {
    private static final String LOG_TAG = "LocationHelper";
    private static final long MILLIS_DURATION_RECENT = 30 * 1000;
    private Map<String, LocationListener> peripheralToLocationListenerMap;
    private ItemTrackerPersistenceHelper itemTrackerPersistenceHelper;
    private ExternalAlertHelper externalAlertHelper;

    public LocationHelper(Context context) {
        peripheralToLocationListenerMap = new HashMap<String, LocationListener>();
        itemTrackerPersistenceHelper = ItemTrackerPersistenceHelper.getInstance(context);
        externalAlertHelper = new ExternalAlertHelper(context);
    }

    public void setPeripheralLocation(String btAddress, Location location) {
        Log.v(LOG_TAG, "setPeripheralLocation: " + btAddress + ", location: " + location);
        itemTrackerPersistenceHelper.persistLocation(btAddress, location, System.currentTimeMillis());
        externalAlertHelper.sendExternalAlerts(btAddress);
    }

    public Location getLastKnownLocationForPeripheral(String btAddress) {
        Location location = itemTrackerPersistenceHelper.getMostRecentLocation(btAddress);

        return location;
    }

    public void updateLastKnownLocation(final Context context, final String btAddress, final boolean connected) {
        //if we're not on the main thread, call back to ourselves on the main thread
        //this happens when trying to update location on the ProximityMonitor callbacks from the Samsung API
        if (Looper.getMainLooper() != Looper.myLooper()) {
            Handler mainHandler = new Handler(Looper.getMainLooper());
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    LocationHelper.this.updateLastKnownLocation(context, btAddress, connected);
                }
            });
            return;
        }


        Log.v(LOG_TAG, "updateLastKnownLocation(), connected: " + connected);

        //check to see if we have had a recent connection/disconnection
        //if so, do not update the connection again
        if (!hasRecentLocation(btAddress)) {

            //determine location
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            LocationListener previousListener = peripheralToLocationListenerMap.get(btAddress);
            if (previousListener != null) {
                locationManager.removeUpdates(previousListener);
            }
            LocationListener newListener;
            LocationListenerOptions options;

            options = new LocationListenerOptions(
                    context, btAddress, connected, System.currentTimeMillis(),
                    200,
                    100,
                    10,
                    5,
                    10 * 1000,
                    5 * 60 * 1000,
                    6 * 60 * 1000
            );

            newListener = new ItemTrackerServiceLocationListener(this, options);
            Log.v(LOG_TAG, "ItemTrackerServiceLocationListener object: " + newListener);
            peripheralToLocationListenerMap.put(btAddress, newListener);  //overwrites any existing value in the map

            int MIN_TIME_BETWEEN_READINGS = 100;
            try {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BETWEEN_READINGS, 0, newListener);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BETWEEN_READINGS, 0, newListener);
            } catch (Exception e) {
                Log.e(LOG_TAG, "could not request updates", e);
            }
        } else {
            //we have a recent location, so don't get it again
            Log.v(LOG_TAG, "Recent location already available, do not determine location again");
        }
    }

    private boolean hasRecentLocation(String peripheralAddress) {
        boolean result = false;
        Location location = itemTrackerPersistenceHelper.getMostRecentLocation(peripheralAddress);
        if (location != null) {
            long millisLastLocationTime = location.getTime();
            Log.v(LOG_TAG, "last location found x seconds ago: " + millisLastLocationTime / 1000);
            result =  System.currentTimeMillis() - millisLastLocationTime < MILLIS_DURATION_RECENT;
        }
        return result;
    }
}
