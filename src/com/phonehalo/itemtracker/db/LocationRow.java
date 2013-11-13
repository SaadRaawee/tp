package com.phonehalo.itemtracker.db;

import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;

import java.util.Arrays;
import java.util.List;

public class LocationRow {
    private static final String LOCATION_PROVIDER_DB = "LOCATION_PROVIDER_DB";
    private String deviceAddress;
    private long time;
    private double lat;
    private double lon;
    private float accuracy;

    private final Bundle statusBundle;

    public LocationRow(Cursor c, String[] allColumnKeys) {
        statusBundle = new Bundle();
        List<String> allColumnKeysList = Arrays.asList(allColumnKeys);

        if (allColumnKeysList.contains(ItemTrackerDBHelper.KEY_PERIPHERAL_ADDRESS)) {
            deviceAddress = c.getString(c.getColumnIndex(ItemTrackerDBHelper.KEY_PERIPHERAL_ADDRESS));
        }
        if (allColumnKeysList.contains(ItemTrackerDBHelper.KEY_TIME_MILLIS)) {
            time = c.getLong(c.getColumnIndex(ItemTrackerDBHelper.KEY_TIME_MILLIS));
        }

        if (allColumnKeysList.contains(ItemTrackerDBHelper.KEY_LATITUDE)) {
            lat = c.getDouble(c.getColumnIndex(ItemTrackerDBHelper.KEY_LATITUDE));
        }

        if (allColumnKeysList.contains(ItemTrackerDBHelper.KEY_LONGITUDE)) {
            lon = c.getDouble(c.getColumnIndex(ItemTrackerDBHelper.KEY_LONGITUDE));
        }

        if (allColumnKeysList.contains(ItemTrackerDBHelper.KEY_ACCURACY)) {
            accuracy = c.getFloat(c.getColumnIndex(ItemTrackerDBHelper.KEY_ACCURACY));
        }

    }

    String getDeviceAddress() {
        return deviceAddress;
    }

    double getLat() {
        return lat;
    }

    double getLon() {
        return lon;
    }

    long getTime() {
        return time;
    }

    float getAccuracy() {
        return accuracy;
    }

    @Override
    public String toString() {
        return "LocationRow{" +
                "accuracy=" + accuracy +
                ", deviceAddress='" + deviceAddress + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                ", time=" + time +
                '}';
    }

    public Bundle asBundle() {
        statusBundle.putString(ItemTrackerDBHelper.KEY_PERIPHERAL_ADDRESS, getDeviceAddress());
        statusBundle.putLong(ItemTrackerDBHelper.KEY_TIME_MILLIS, getTime());
        statusBundle.putDouble(ItemTrackerDBHelper.KEY_LATITUDE, getLat());
        statusBundle.putDouble(ItemTrackerDBHelper.KEY_LONGITUDE, getLon());
        statusBundle.putFloat(ItemTrackerDBHelper.KEY_ACCURACY, getAccuracy());
        return statusBundle;
    }

    public Location asLocation() {
        Location location = new Location(LOCATION_PROVIDER_DB);
        location.setAccuracy(getAccuracy());
        location.setLatitude(getLat());
        location.setLongitude(getLon());
        location.setTime(getTime());

        return location;
    }
}
