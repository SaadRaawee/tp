package com.phonehalo.itemtracker.helper;

import android.content.*;
import android.location.Location;
import android.os.IBinder;
import com.phonehalo.itemtracker.db.ExternalAlertSettings;
import com.phonehalo.itemtracker.db.PeripheralSettings;
import com.phonehalo.itemtracker.fragment.ItemHomeFragment;
import com.phonehalo.itemtracker.fragment.SettingsFragment;
import com.phonehalo.itemtracker.service.ItemTrackerService;
import com.phonehalo.itemtracker.utility.Log;


public class ItemTrackerServiceHelper {
    private static final String LOG_TAG = "ItemTrackerServiceHelper";

    private static ItemTrackerServiceHelper theInstance;

    private ItemTrackerService itemTrackerService;
    private ServiceConnection bleManagerServiceConnection = new BLEManagerServiceConnection();
    private ItemHomeFragment itemHomeFragment;
    private SettingsFragment settingsFragment;
    private boolean connected;

    //this method requires that getInstance(ItemHomeFragment, SettingsFragment) was called first
    public static ItemTrackerServiceHelper getInstance() {
        return theInstance;
    }

    public static ItemTrackerServiceHelper getInstance(ItemHomeFragment itemHomeFragment, SettingsFragment settingsFragment) {
        if (theInstance == null) {
            theInstance = new ItemTrackerServiceHelper(itemHomeFragment, settingsFragment);
        } else {
//            Log.w(LOG_TAG, "requesting new instance with fragments: " + itemHomeFragment + ", " + settingsFragment);
//            Log.w(LOG_TAG, "existing fragments: " + theInstance.itemHomeFragment + ", " + theInstance.settingsFragment);
        }
        return theInstance;
    }

    private ItemTrackerServiceHelper(ItemHomeFragment itemHomeFragment, SettingsFragment settingsFragment) {
        this.itemHomeFragment = itemHomeFragment;
        this.settingsFragment = settingsFragment;
    }

    public void destroy() {
        Log.v(LOG_TAG, "destroy singleton instance");
        theInstance = null;
    }

    public void startService(Context context) {
        Intent serviceIntent = new Intent(context.getApplicationContext(), ItemTrackerService.class);
        context.startService(serviceIntent); //allow it to run in the background indefinitely
        context.bindService(serviceIntent, bleManagerServiceConnection, Context.BIND_AUTO_CREATE);  //bind to get a reference to it
    }

    public void persistPeripheralSettings(PeripheralSettings peripheralSettings) {
        Log.v(LOG_TAG, "Persist Settings");
        itemTrackerService.persistPeripheralSettings(peripheralSettings);
    }

    public PeripheralSettings getPeripheralSettings(String peripheralAddress) {
        return itemTrackerService.getPeripheralSettings(peripheralAddress);
    }

    public void persistExternalAlertSettings(ExternalAlertSettings externalAlertSettings) {
        itemTrackerService.persistExternalAlertSettings(externalAlertSettings);
    }

    public ExternalAlertSettings getExternalAlertSettings() {
        return itemTrackerService.getExternalAlertSettings();
    }

    private void onServiceConnected() {
        Log.v(LOG_TAG, "onServiceConnected");
        itemHomeFragment.setItemTrackerServiceHelper(this);
        settingsFragment.setItemTrackerServiceHelper(this);
    }

    public boolean isConnected() {
        return connected;
    }

    public Location getLastKnownLocationForPeripheral(String btAddress) {
        return itemTrackerService.getLastKnownLocationForPeripheral(btAddress);
    }

    public void alertPhoneOnPeripheralDisconnect(String peripheralAddress) {
        itemTrackerService.alertPhoneOnPeripheralDisconnect(peripheralAddress);
    }

    public void bleServiceConnected() {
        itemTrackerService.bleServiceConnected();
    }

    private class BLEManagerServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.v(LOG_TAG, "onServiceConnected: " + name);
            ItemTrackerService.ItemTrackerServiceBinder binder = (ItemTrackerService.ItemTrackerServiceBinder) service;
            itemTrackerService = binder.getService();
            ItemTrackerServiceHelper.this.connected = true;
            ItemTrackerServiceHelper.this.onServiceConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.v(LOG_TAG, "onServiceDisconnected: " + name);
            ItemTrackerServiceHelper.this.connected = false;
        }
    }
}