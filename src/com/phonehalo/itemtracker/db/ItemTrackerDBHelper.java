package com.phonehalo.itemtracker.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.phonehalo.ble.util.Log;

public class ItemTrackerDBHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = "ItemTrackerServiceDBHelper";
    public static final String STRING_VALUE_NONE = "NONE";

    private static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "ItemTrackerService";
    public static final String TABLE_NAME_PERIPHERAL_LOCATION = "peripheral_location_table";
    public static final String TABLE_NAME_PERIPHERAL_PREFS = "peripheral_prefs_table";
    public static final String TABLE_NAME_EXTERNAL_ALERT_PREFS = "app_prefs_table";

    public static final String KEY_PERIPHERAL_ADDRESS = "address";
    public static final String KEY_TIME_MILLIS = "time";
    public static final String KEY_LATITUDE = "lat";
    public static final String KEY_LONGITUDE = "lon";
    public static final String KEY_ACCURACY = "accuracy";

    public static final String KEY_PERIPHERAL_NAME = "peripheral_name";
    public static final String KEY_PERIPHERAL_IMAGE_URI = "peripheral_image_uri";
    public static final String KEY_PERIPHERAL_ALERT_DURATION = "peripheral_alert_duration";
    public static final String KEY_PHONE_AUDIBLE_ALERT_ON = "phone_audible_alert";
    public static final String KEY_PHONE_ALERT_DISABLED = "phone_alert_disabled";
    public static final String KEY_PHONE_ALERT_VOLUME = "phone_alert_volume";
    public static final String KEY_PHONE_VIBRATE_ALERT_ON = "phone_vibrate_alert";
    public static final String KEY_PHONE_ALERT_DURATION = "phone_alert_duration";
    public static final String KEY_PHONE_ALERT_URI = "phone_alert_media";
    public static final String KEY_DEVICE_CONNECTION_STATE = "device_connection_status";

    public static final String KEY_EMAIL_ADDRESS = "email_address";
    public static final String KEY_EMAIL_ADDRESS_CC = "email_address_cc";
    public static final String KEY_EMAIL_ALERT_ON = "email_alert_on";
    public static final String KEY_FACEBOOK_ALERT_ON = "facebook_alert_on";
    public static final String KEY_TWITTER_ALERT_ON = "twitter_alert_on";
    public static final String KEY_TWITTER_OAUTH_TOKEN = "twitter_oauth_token";
    public static final String KEY_TWITTER_OAUTH_SECRET = "twitter_oauth_secret";

    public static final String CREATE_PERIPHERAL_PREFS =
            "CREATE TABLE " + TABLE_NAME_PERIPHERAL_PREFS + " (" +
                    KEY_PERIPHERAL_ADDRESS + " TEXT primary key not null, " +
                    KEY_PERIPHERAL_NAME + " TEXT, " +
                    KEY_PERIPHERAL_IMAGE_URI + " TEXT, " +
                    KEY_PERIPHERAL_ALERT_DURATION + " INTEGER, " +
                    KEY_PHONE_AUDIBLE_ALERT_ON + " BOOLEAN, " +
                    KEY_PHONE_VIBRATE_ALERT_ON + " BOOLEAN, " +
                    KEY_PHONE_ALERT_DURATION + " INTEGER, " +
                    KEY_PHONE_ALERT_URI + " TEXT, " +
                    KEY_PHONE_ALERT_DISABLED + " INTEGER, " +
                    KEY_PHONE_ALERT_VOLUME + " INTEGER, " +
                    KEY_DEVICE_CONNECTION_STATE + " BOOLEAN " +
                    ");";
    private static final String CREATE_PERIPHERAL_LOCATION =
            "CREATE TABLE " + TABLE_NAME_PERIPHERAL_LOCATION + " (" +
                    KEY_PERIPHERAL_ADDRESS + " TEXT primary key not null, " +
                    KEY_TIME_MILLIS + " LONG, " +
                    KEY_LATITUDE + " REAL, " +
                    KEY_LONGITUDE + " REAL, " +
                    KEY_ACCURACY + " REAL" +
                    ");";
    private static final String CREATE_EXTERNAL_ALERT_PREFS =
            "CREATE TABLE " + TABLE_NAME_EXTERNAL_ALERT_PREFS + " (" +
                    KEY_PERIPHERAL_ADDRESS + " TEXT primary key not null, " + //note, this is per app right now, but just in case this becomes per-peripheral ...
                    KEY_EMAIL_ADDRESS + " TEXT, " +
                    KEY_EMAIL_ADDRESS_CC + " TEXT, " +
                    KEY_EMAIL_ALERT_ON + " INTEGER, " +
                    KEY_FACEBOOK_ALERT_ON + " INTEGER, " +
                    KEY_TWITTER_ALERT_ON + " INTEGER, " +
                    KEY_TWITTER_OAUTH_TOKEN + " TEXT, " +
                    KEY_TWITTER_OAUTH_SECRET + " TEXT " +
                    ");";
    public static final String[] ALL_COLUMN_KEYS_PERIPHERAL_LOCATION = {
            KEY_PERIPHERAL_ADDRESS,
            KEY_TIME_MILLIS,
            KEY_LATITUDE,
            KEY_LONGITUDE,
            KEY_ACCURACY
    };
    public static final String[] ALL_COLUMN_KEYS_PERIPHERAL_PREFS = {
            KEY_PERIPHERAL_ADDRESS,
            KEY_PERIPHERAL_NAME,
            KEY_PERIPHERAL_IMAGE_URI,
            KEY_PERIPHERAL_ALERT_DURATION,
            KEY_PHONE_AUDIBLE_ALERT_ON,
            KEY_PHONE_ALERT_DISABLED,
            KEY_PHONE_ALERT_VOLUME,
            KEY_PHONE_ALERT_URI,
            KEY_PHONE_VIBRATE_ALERT_ON,
            KEY_PHONE_ALERT_DURATION,
            KEY_DEVICE_CONNECTION_STATE
    };
    public static final String[] ALL_COLUMN_KEYS_EXTERNAL_ALERT_PREFS = {
            KEY_PERIPHERAL_ADDRESS,
            KEY_EMAIL_ADDRESS,
            KEY_EMAIL_ADDRESS_CC,
            KEY_EMAIL_ALERT_ON,
            KEY_FACEBOOK_ALERT_ON,
            KEY_TWITTER_ALERT_ON,
            KEY_TWITTER_OAUTH_TOKEN,
            KEY_TWITTER_OAUTH_SECRET
    };
    public static final String[] ALL_COLUMN_PERIPHERAL_ADDRESS_PREFS = {
            KEY_PERIPHERAL_ADDRESS
    };
    public static final String PERIPHERAL_ADDRESS_FOR_APP_SETTINGS = "PERIPHERAL_ADDRESS_FOR_APP_SETTINGS";

    public ItemTrackerDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.v(LOG_TAG, "Constructor for " + DATABASE_NAME + " version " + DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.v(LOG_TAG, "Creating " + DATABASE_NAME + " version " + DATABASE_VERSION);
        db.execSQL(CREATE_PERIPHERAL_LOCATION);
        db.execSQL(CREATE_PERIPHERAL_PREFS);
        db.execSQL(CREATE_EXTERNAL_ALERT_PREFS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.v(LOG_TAG, "Database upgrade from " + oldVersion + " to " + newVersion);
        String upgradeQuery = "ALTER TABLE "+TABLE_NAME_PERIPHERAL_PREFS+" ADD COLUMN "+ KEY_DEVICE_CONNECTION_STATE + " BOOLEAN;";
        if (oldVersion == 1 && newVersion == 2)
            db.execSQL(upgradeQuery);
    }
}

