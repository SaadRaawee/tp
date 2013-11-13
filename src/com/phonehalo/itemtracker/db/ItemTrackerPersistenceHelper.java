package com.phonehalo.itemtracker.db;

import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.location.Location;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import com.phonehalo.itemtracker.R;
import com.phonehalo.itemtracker.utility.Log;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;

public class ItemTrackerPersistenceHelper {

    private static ItemTrackerPersistenceHelper theInstance;
    private final SQLiteDatabase trackerServiceDb;
    private static final String LOG_TAG = "ItemTrackerServicePersistenceHelper";
    private final Context context;

    public static final int PERIPHERAL_ALERT_DURATION_NONE = 0;
    public static final int PERIPHERAL_ALERT_DURATION_THREE_RINGS = 1;
    public static final int PERIPHERAL_ALERT_DURATION_CONTINUOUS = 2;
    public static final int PHONE_DEFAULT_ALERT_DURATION = 3;

    public static ItemTrackerPersistenceHelper getInstance(Context context) {
        if (theInstance == null) {
            theInstance = new ItemTrackerPersistenceHelper(context);
        }
        return theInstance;
    }

    private ItemTrackerPersistenceHelper(Context context) {
        this.context = context;
        trackerServiceDb = new ItemTrackerDBHelper(context.getApplicationContext()).getWritableDatabase();
    }

    public void persistLocation(String btAddress, Location location, long deviceEventTime) {
        ContentValues cv = new ContentValues();
        String deviceAddress = (btAddress == null) ? ItemTrackerDBHelper.STRING_VALUE_NONE : btAddress;
        long time = (location == null) ? deviceEventTime : location.getTime();
        double lat = (location == null) ? Double.NaN : location.getLatitude();
        double lon = (location == null) ? Double.NaN : location.getLongitude();
        float accuracy = (location == null) ? Float.NaN : location.getAccuracy();

        cv.put(ItemTrackerDBHelper.KEY_PERIPHERAL_ADDRESS, deviceAddress);
        cv.put(ItemTrackerDBHelper.KEY_TIME_MILLIS, time);
        cv.put(ItemTrackerDBHelper.KEY_LATITUDE, lat);
        cv.put(ItemTrackerDBHelper.KEY_LONGITUDE, lon);
        cv.put(ItemTrackerDBHelper.KEY_ACCURACY, accuracy);

        Log.v(LOG_TAG, "Persisting location row: " + cv.toString());

        long rowId = trackerServiceDb.insertWithOnConflict(ItemTrackerDBHelper.TABLE_NAME_PERIPHERAL_LOCATION, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        logTableContents(LOG_TAG, ItemTrackerDBHelper.TABLE_NAME_PERIPHERAL_LOCATION);
    }

    public Location getMostRecentLocation(String btAddress) {
        Log.v(LOG_TAG, "getMostRecentLocation()");
        LocationRow locationRow = null;
        String[] selectionArgs = {btAddress};
        Cursor c = trackerServiceDb.query(ItemTrackerDBHelper.TABLE_NAME_PERIPHERAL_LOCATION,
                ItemTrackerDBHelper.ALL_COLUMN_KEYS_PERIPHERAL_LOCATION,
                ItemTrackerDBHelper.KEY_PERIPHERAL_ADDRESS + "=?",
                selectionArgs,
                null,//no grouping
                null,//no having filter
                null,//no ordering
                null //no limit
        );

        if (c.moveToFirst()) {
            locationRow = new LocationRow(c, ItemTrackerDBHelper.ALL_COLUMN_KEYS_PERIPHERAL_LOCATION);
        }
        Log.v(LOG_TAG, "getMostRecentLocation(): " + locationRow);
        if((locationRow != null)  && (locationRow.getLat() != 0))
        {
            Location location = new Location("mostRecentLocation");
            location.setLatitude(locationRow.getLat());
            location.setLongitude(locationRow.getLon());
            location.setAccuracy(locationRow.getAccuracy());
            location.setTime(locationRow.getTime());
            return location;
        }
        return null;
    }

    public void persistPeripheralSettings(PeripheralSettings peripheralSettings) {
        PeripheralSettings newPeripheralSettings = peripheralSettings;
        ContentValues cv = new ContentValues();
        String peripheralAddress = newPeripheralSettings.getPeripheralAddress();
        peripheralAddress = (peripheralAddress == null) ? ItemTrackerDBHelper.STRING_VALUE_NONE : peripheralAddress;

        PeripheralSettings storedPeripheralSettings = getPeripheralSettings(peripheralAddress);
        Log.v(LOG_TAG, "persistPeripheralSettings: " + newPeripheralSettings + ", \nstoredPeripheralSettings: " + storedPeripheralSettings);

        cv.put(ItemTrackerDBHelper.KEY_PERIPHERAL_ADDRESS, peripheralAddress);

        if (newPeripheralSettings.isPeripheralNameDirty()) {
            cv.put(ItemTrackerDBHelper.KEY_PERIPHERAL_NAME, newPeripheralSettings.getPeripheralName());
        } else if (storedPeripheralSettings != null) {
            cv.put(ItemTrackerDBHelper.KEY_PERIPHERAL_NAME, storedPeripheralSettings.getPeripheralName());
        }

        if (newPeripheralSettings.isPeripheralImageUriDirty()) {
            cv.put(ItemTrackerDBHelper.KEY_PERIPHERAL_IMAGE_URI, newPeripheralSettings.getPeripheralImageUri().toString());
        } else if (storedPeripheralSettings != null && storedPeripheralSettings.getPeripheralImageUri() != null) {
            cv.put(ItemTrackerDBHelper.KEY_PERIPHERAL_IMAGE_URI, storedPeripheralSettings.getPeripheralImageUri().toString());
        }

        if (newPeripheralSettings.isPeripheralAlertDurationDirty()) {
            cv.put(ItemTrackerDBHelper.KEY_PERIPHERAL_ALERT_DURATION, newPeripheralSettings.getPeripheralAlertDuration());
        } else if (storedPeripheralSettings != null) {
            cv.put(ItemTrackerDBHelper.KEY_PERIPHERAL_ALERT_DURATION, storedPeripheralSettings.getPeripheralAlertDuration());
        }

        if (newPeripheralSettings.isPhoneAlertDurationSecondsDirty()) {
            cv.put(ItemTrackerDBHelper.KEY_PHONE_ALERT_DURATION, newPeripheralSettings.getPhoneAlertDurationSeconds());
        } else if (storedPeripheralSettings != null) {
            cv.put(ItemTrackerDBHelper.KEY_PHONE_ALERT_DURATION, storedPeripheralSettings.getPhoneAlertDurationSeconds());
        }

        if (newPeripheralSettings.isPhoneAudibleAlertOnDirty()) {
            cv.put(ItemTrackerDBHelper.KEY_PHONE_AUDIBLE_ALERT_ON, newPeripheralSettings.isPhoneAudibleAlertOn());
        } else if (storedPeripheralSettings != null) {
            cv.put(ItemTrackerDBHelper.KEY_PHONE_AUDIBLE_ALERT_ON, storedPeripheralSettings.isPhoneAudibleAlertOn());
        }

        if (newPeripheralSettings.isPhoneAudibleAlertMutedDirty()) {
            cv.put(ItemTrackerDBHelper.KEY_PHONE_ALERT_DISABLED, newPeripheralSettings.isPhoneAudibleAlertMuted());
        } else if (storedPeripheralSettings != null) {
            cv.put(ItemTrackerDBHelper.KEY_PHONE_ALERT_DISABLED, storedPeripheralSettings.isPhoneAudibleAlertMuted());
        }

        if (newPeripheralSettings.isPhoneAudibleAlertVolumeDirty()) {
            cv.put(ItemTrackerDBHelper.KEY_PHONE_ALERT_VOLUME, newPeripheralSettings.getPhoneAudibleAlertVolume());
        } else if (storedPeripheralSettings != null) {
            cv.put(ItemTrackerDBHelper.KEY_PHONE_ALERT_VOLUME, storedPeripheralSettings.getPhoneAudibleAlertVolume());
        }

        if (newPeripheralSettings.isPhoneVibrateAlertOnDirty()) {
            cv.put(ItemTrackerDBHelper.KEY_PHONE_VIBRATE_ALERT_ON, newPeripheralSettings.isPhoneVibrateAlertOn());
        } else if (storedPeripheralSettings != null) {
            cv.put(ItemTrackerDBHelper.KEY_PHONE_VIBRATE_ALERT_ON, storedPeripheralSettings.isPhoneVibrateAlertOn());
        }

        if (newPeripheralSettings.isPhoneAudibleAlertUriDirty()) {
            cv.put(ItemTrackerDBHelper.KEY_PHONE_ALERT_URI, newPeripheralSettings.getPhoneAudibleAlertUri().toString());
        } else if (storedPeripheralSettings != null) {
            cv.put(ItemTrackerDBHelper.KEY_PHONE_ALERT_URI, storedPeripheralSettings.getPhoneAudibleAlertUri().toString());
        }

            cv.put(ItemTrackerDBHelper.KEY_DEVICE_CONNECTION_STATE, newPeripheralSettings.isDeviceConnectionState());


        updateOrInsertWhereDeviceEquals(ItemTrackerDBHelper.TABLE_NAME_PERIPHERAL_PREFS, cv, peripheralAddress);
    }

    public PeripheralSettings getPeripheralSettings(String peripheralAddress) {
        Log.v(LOG_TAG, "getPeripheralSettings()");
        PeripheralSettings peripheralSettings = new PeripheralSettings();
        String[] selectionArgs = {peripheralAddress};
        Cursor c = trackerServiceDb.query(ItemTrackerDBHelper.TABLE_NAME_PERIPHERAL_PREFS,
                ItemTrackerDBHelper.ALL_COLUMN_KEYS_PERIPHERAL_PREFS,
                ItemTrackerDBHelper.KEY_PERIPHERAL_ADDRESS + "=?",
                selectionArgs,
                null,//no grouping
                null,//no having filter
                null,//no ordering
                null //no limit
        );

        peripheralSettings.setPeripheralAddress(peripheralAddress);
        if (c.moveToFirst()) {
            peripheralSettings.setPeripheralName(c.getString(c.getColumnIndex(ItemTrackerDBHelper.KEY_PERIPHERAL_NAME)));
            peripheralSettings.setPeripheralAlertDuration(c.getInt(c.getColumnIndex(ItemTrackerDBHelper.KEY_PERIPHERAL_ALERT_DURATION)));
            peripheralSettings.setPhoneAlertDurationSeconds(c.getInt(c.getColumnIndex(ItemTrackerDBHelper.KEY_PHONE_ALERT_DURATION)));
            peripheralSettings.setPhoneAudibleAlertOn(c.getInt(c.getColumnIndex(ItemTrackerDBHelper.KEY_PHONE_AUDIBLE_ALERT_ON)) > 0);
            peripheralSettings.setPhoneAudibleAlertMuted(c.getInt(c.getColumnIndex(ItemTrackerDBHelper.KEY_PHONE_ALERT_DISABLED)) > 0);
            peripheralSettings.setPhoneAudibleAlertVolume(c.getInt(c.getColumnIndex(ItemTrackerDBHelper.KEY_PHONE_ALERT_VOLUME)));
            peripheralSettings.setPhoneVibrateAlertOn(c.getInt(c.getColumnIndex(ItemTrackerDBHelper.KEY_PHONE_VIBRATE_ALERT_ON)) > 0);
            peripheralSettings.setDeviceConnectionState(c.getInt(c.getColumnIndex(ItemTrackerDBHelper.KEY_DEVICE_CONNECTION_STATE))>0);

            String audioUriString = c.getString(c.getColumnIndex(ItemTrackerDBHelper.KEY_PHONE_ALERT_URI));
            if (audioUriString != null) {
                peripheralSettings.setPhoneAudibleAlertUri(Uri.parse(audioUriString));
            } else {
                Log.w(LOG_TAG, "Audio URI should be set but is not, falling back to default");
                peripheralSettings.setPhoneAudibleAlertUri(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALL));
            }

            String imageUriString = c.getString(c.getColumnIndex(ItemTrackerDBHelper.KEY_PERIPHERAL_IMAGE_URI));
            if (imageUriString != null) {
                peripheralSettings.setPeripheralImageUri(Uri.parse(imageUriString));
            } else {
                Log.w(LOG_TAG, "Image URI not set, falling back to default");
                peripheralSettings.setPeripheralImageUri(null);
            }

            peripheralSettings.clearDirtyFlags();
        } else {
            Log.v(LOG_TAG, "getting settings before they have been stored");
            peripheralSettings = null;
        }
        Log.v(LOG_TAG, "getPeripheralSettings: " + peripheralSettings);
        return peripheralSettings;
    }

    public void persistExternalAlertSettings(ExternalAlertSettings externalAlertSettings) {
        Log.v(LOG_TAG, "persistExternalAlertSettings: " + externalAlertSettings);
        ContentValues cv = new ContentValues();
        cv.put(ItemTrackerDBHelper.KEY_PERIPHERAL_ADDRESS, ItemTrackerDBHelper.PERIPHERAL_ADDRESS_FOR_APP_SETTINGS);

        if (externalAlertSettings.isEmailAddressDirty()) {
            cv.put(ItemTrackerDBHelper.KEY_EMAIL_ADDRESS, externalAlertSettings.getEmailAddress());
        }

        if (externalAlertSettings.isEmailCCAddressDirty()){
            cv.put(ItemTrackerDBHelper.KEY_EMAIL_ADDRESS_CC, externalAlertSettings.getEmailCCAddress());
        }

        if (externalAlertSettings.isEmailAlertOnDirty()) {
            cv.put(ItemTrackerDBHelper.KEY_EMAIL_ALERT_ON, externalAlertSettings.isEmailAlertOn());
        }

        if (externalAlertSettings.isFacebookAlertOnDirty()) {
            cv.put(ItemTrackerDBHelper.KEY_FACEBOOK_ALERT_ON, externalAlertSettings.isFacebookAlertOn());
        }

        if (externalAlertSettings.isTwitterAlertOnDirty()) {
            cv.put(ItemTrackerDBHelper.KEY_TWITTER_ALERT_ON, externalAlertSettings.isTwitterAlertOn());
        }

        if (externalAlertSettings.isOauthTokenTwitterDirty()) {
            cv.put(ItemTrackerDBHelper.KEY_TWITTER_OAUTH_TOKEN, externalAlertSettings.getTwitterTokenAndSecret()[0]);
        }

        if (externalAlertSettings.isOauthSecretTwitterDirty()) {
            cv.put(ItemTrackerDBHelper.KEY_TWITTER_OAUTH_SECRET, externalAlertSettings.getTwitterTokenAndSecret()[1]);
        }

        updateOrInsertWhereDeviceEquals(ItemTrackerDBHelper.TABLE_NAME_EXTERNAL_ALERT_PREFS,
                                        cv,
                                        ItemTrackerDBHelper.PERIPHERAL_ADDRESS_FOR_APP_SETTINGS);
    }

    public ExternalAlertSettings getExternalAlertSettings() {
        ExternalAlertSettings externalAlertSettings = new ExternalAlertSettings();
        String[] selectionArgs = {ItemTrackerDBHelper.PERIPHERAL_ADDRESS_FOR_APP_SETTINGS};
        Cursor c = trackerServiceDb.query(ItemTrackerDBHelper.TABLE_NAME_EXTERNAL_ALERT_PREFS,
                ItemTrackerDBHelper.ALL_COLUMN_KEYS_EXTERNAL_ALERT_PREFS,
                ItemTrackerDBHelper.KEY_PERIPHERAL_ADDRESS + "=?",
                selectionArgs,
                null,//no grouping
                null,//no having filter
                null,//no ordering
                null //no limit
        );


        if (c.moveToFirst()) {
            externalAlertSettings.setEmailAddress(c.getString(c.getColumnIndex(ItemTrackerDBHelper.KEY_EMAIL_ADDRESS)));
            externalAlertSettings.setEmailCCAddress(c.getString(c.getColumnIndex(ItemTrackerDBHelper.KEY_EMAIL_ADDRESS_CC)));
            externalAlertSettings.setEmailAlertOn(c.getInt(c.getColumnIndex(ItemTrackerDBHelper.KEY_EMAIL_ALERT_ON)) > 0);
            externalAlertSettings.setFacebookAlertOn(c.getInt(c.getColumnIndex(ItemTrackerDBHelper.KEY_FACEBOOK_ALERT_ON)) > 0);
            externalAlertSettings.setTwitterAlertOn(c.getInt(c.getColumnIndex(ItemTrackerDBHelper.KEY_TWITTER_ALERT_ON)) > 0);
            externalAlertSettings.setTwitterTokenAndSecret(
                    c.getString(c.getColumnIndex(ItemTrackerDBHelper.KEY_TWITTER_OAUTH_TOKEN)),
                    c.getString(c.getColumnIndex(ItemTrackerDBHelper.KEY_TWITTER_OAUTH_SECRET))
            );
            externalAlertSettings.clearDirtyFlags();
        }
        Log.v(LOG_TAG, "getExternalAlertSettings: " + externalAlertSettings);
        return externalAlertSettings;
    }

    private void updateOrInsertWhereDeviceEquals(String tableName, ContentValues cv, String peripheralAddress) {

        Log.v(LOG_TAG, "Persisting peripheral prefs row: " + cv.toString());
        String whereClause = ItemTrackerDBHelper.KEY_PERIPHERAL_ADDRESS + "=?";
        String[] whereArgs = { peripheralAddress };
        long rowsAffected = 0;
        try{
        rowsAffected = trackerServiceDb.update(tableName, cv, whereClause, whereArgs);
        }catch (SQLiteException e)
        {
            trackerServiceDb.delete(tableName, null, null);
            rowsAffected = trackerServiceDb.insert(tableName, null, cv);
        }
        if (rowsAffected == 0) {
            rowsAffected = trackerServiceDb.insert(tableName, null, cv);
            Log.v(LOG_TAG, "row inserted: " + cv + "rowsAffected: " + rowsAffected);
        } else {
            Log.v(LOG_TAG, "row updated: " + cv + "rowsAffected: " + rowsAffected);
        }

        logTableContents(LOG_TAG, tableName);
    }

    public void logTableContents(String logTag, String tableName) {
        String[] allColumnKeys = {};
        StringBuilder sb = new StringBuilder();
        if (ItemTrackerDBHelper.TABLE_NAME_PERIPHERAL_PREFS.equals(tableName)) {
            sb.append(ItemTrackerDBHelper.TABLE_NAME_PERIPHERAL_PREFS).append(" ");
            allColumnKeys = ItemTrackerDBHelper.ALL_COLUMN_KEYS_PERIPHERAL_PREFS;
        }

        Cursor c = trackerServiceDb.query(tableName,
                allColumnKeys,
                null,//returns all rows
                null,//no selectionArgs, since no selectionString
                null,//no grouping
                null,//no having filter
                null,//no ordering
                null//no limit
        );

        sb.append("{");
        while (c.moveToNext()) {
            sb.append("[");
            for (int i = 0; i < c.getColumnCount(); i++) {
                sb.append(c.getColumnName(i)).append(" : ").append(c.getString(i)).append(", ");
            }
            sb.append("]");
        }
        sb.append("}");
        Log.v(logTag, sb.toString());
    }

    public void persistDefaultSettingsIfNecessary(BluetoothDevice peripheral) {
        if (!isPeripheralKnown(peripheral.getAddress())) {
            persistPeripheralSettings(getDefaultPeripheralSettings(peripheral));
        }
    }

    private PeripheralSettings getDefaultPeripheralSettings(BluetoothDevice peripheral) {
        PeripheralSettings peripheralSettings = new PeripheralSettings();

        //set default values
        peripheralSettings.setPeripheralAddress(peripheral.getAddress());
        peripheralSettings.setPeripheralName(peripheral.getName());
        peripheralSettings.setPeripheralAlertDuration(PERIPHERAL_ALERT_DURATION_THREE_RINGS);
        peripheralSettings.setPhoneAudibleAlertOn(true);
        peripheralSettings.setPhoneAudibleAlertMuted(false);
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        peripheralSettings.setPhoneAudibleAlertVolume(audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM));
        peripheralSettings.setPhoneVibrateAlertOn(true);
        peripheralSettings.setDeviceConnectionState(false);
        peripheralSettings.setPhoneAlertDurationSeconds(PHONE_DEFAULT_ALERT_DURATION);
        peripheralSettings.setPhoneAudibleAlertUri(Uri.parse("android.resource://com.phonehalo.itemtracker/" + R.raw.ph_sfx_005_shortened));

        return peripheralSettings;
    }

    public boolean isPeripheralKnown(String peripheralAddress) {
        String whereClause = ItemTrackerDBHelper.KEY_PERIPHERAL_ADDRESS + "=?";
        String[] whereArgs = { peripheralAddress };
        Cursor c = trackerServiceDb.query(ItemTrackerDBHelper.TABLE_NAME_PERIPHERAL_PREFS,
                ItemTrackerDBHelper.ALL_COLUMN_PERIPHERAL_ADDRESS_PREFS,
                whereClause,
                whereArgs,
                null,//no grouping
                null,//no having filter
                null,//no ordering
                null//no limit
        );
        return c.getCount() > 0;
    }

    public int getPeripheralCount() {
        Cursor c = trackerServiceDb.query(ItemTrackerDBHelper.TABLE_NAME_PERIPHERAL_PREFS,
                ItemTrackerDBHelper.ALL_COLUMN_KEYS_PERIPHERAL_PREFS,
                null,//returns all rows
                null,//no selectionArgs, since no selectionString
                null,//no grouping
                null,//no having filter
                null,//no ordering
                null//no limit
        );
        return c.getCount();
    }

    public ArrayList<String> getTrackedPeripherals() {
        Cursor c = trackerServiceDb.query(ItemTrackerDBHelper.TABLE_NAME_PERIPHERAL_PREFS,
                ItemTrackerDBHelper.ALL_COLUMN_PERIPHERAL_ADDRESS_PREFS,
                null,//returns all rows
                null,//no selectionArgs, since no selectionString
                null,//no grouping
                null,//no having filter
                null,//no ordering
                null//no limit
        );
        ArrayList<String> stringList = new ArrayList<String>();
        for(int i = 0; i < c.getCount();i++)
        {
            c.moveToPosition(i);
            Log.v(LOG_TAG, "DB MOVE TO " + c.getString(0));
            stringList.add(c.getString(0));
        }
        return stringList;
    }


    public void deletePeripheral(String peripheralAddress) {
        deletePeripheralFromTable(ItemTrackerDBHelper.TABLE_NAME_PERIPHERAL_LOCATION, peripheralAddress);
        logTable(ItemTrackerDBHelper.TABLE_NAME_PERIPHERAL_LOCATION, ItemTrackerDBHelper.ALL_COLUMN_KEYS_PERIPHERAL_LOCATION);

        deletePeripheralFromTable(ItemTrackerDBHelper.TABLE_NAME_PERIPHERAL_PREFS, peripheralAddress);
        logTable(ItemTrackerDBHelper.TABLE_NAME_PERIPHERAL_PREFS, ItemTrackerDBHelper.ALL_COLUMN_KEYS_PERIPHERAL_PREFS);
    }

    private void deletePeripheralFromTable(String tableName, String peripheralAddress) {
        String whereClause = MessageFormat.format("{0} = ?", ItemTrackerDBHelper.KEY_PERIPHERAL_ADDRESS);
        String[] selectionArgs = {peripheralAddress};
        int rowsAffected = trackerServiceDb.delete(
                tableName,
                whereClause,
                selectionArgs
        );
        Log.v(LOG_TAG, "deletePeripheralFromTable: " + tableName + ", for address: " + peripheralAddress + ", rowsAffected: " + rowsAffected);
        Log.v(LOG_TAG, "where: " + whereClause + ", selectionArgs: " + Arrays.toString(selectionArgs));
    }

    private void logTable(String tableName, String[] allColumnKeys) {
        Log.v(LOG_TAG, "logTable: " + tableName);
        Cursor c = trackerServiceDb.query(
                tableName,
                allColumnKeys,
                null,//returns all rows
                null,//no selectionArgs, since no selectionString
                null,//no grouping
                null,//no having filter
                null,//no ordering
                null//no limit
        );
        StringBuffer rowLog = new StringBuffer();
        while(c.moveToNext()) {
            for (String key : allColumnKeys) {
                rowLog.append(c.getString(c.getColumnIndex(key))).append(", ");
            }
            Log.v(LOG_TAG, rowLog);

            rowLog.delete(0, rowLog.length() - 1); //clear the buffer for the next row
        }
    }
}
