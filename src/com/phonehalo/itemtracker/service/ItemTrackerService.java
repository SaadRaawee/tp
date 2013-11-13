package com.phonehalo.itemtracker.service;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.net.Uri;
import android.os.*;
import com.phonehalo.ble.BleConstant;
import com.phonehalo.ble.service.PHBleService;
import com.phonehalo.itemtracker.db.ExternalAlertSettings;
import com.phonehalo.itemtracker.db.ItemTrackerPersistenceHelper;
import com.phonehalo.itemtracker.db.PeripheralSettings;
import com.phonehalo.itemtracker.helper.AlertHelperMediaPlayer;
import com.phonehalo.itemtracker.helper.ExternalAlertHelper;
import com.phonehalo.itemtracker.helper.LocationHelper;
import com.phonehalo.itemtracker.utility.Log;


public class ItemTrackerService extends Service {

    private static final String LOG_TAG = "ItemTrackerService";
    private final IBinder binder = new ItemTrackerServiceBinder();
    private LocationHelper locationHelper;
    private ExternalAlertHelper externalAlertHelper;

    private Messenger bleIncomingMessenger;
    private ServiceConnection phBleServiceConnection = new PhBleServiceConnection();
    private Messenger phBleService = null;
    private boolean isBound = false;

    private AlertHelperMediaPlayer alertServerPlayer;

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(LOG_TAG, "onBind");
        return binder;
    }

    //AIDL is only required if the same service is accessed by different applications/processes
    public class ItemTrackerServiceBinder extends Binder {
        public com.phonehalo.itemtracker.service.ItemTrackerService getService() {
            return ItemTrackerService.this;
        }
    }

    @Override
    public void onCreate() {
        Log.v(LOG_TAG, "onCreate");
        super.onCreate();
        locationHelper = new LocationHelper(getApplicationContext());
        externalAlertHelper = new ExternalAlertHelper(getApplicationContext());

        Intent phBleServiceStarter = new Intent(getApplicationContext(), PHBleService.class);
        getApplicationContext().bindService(phBleServiceStarter, phBleServiceConnection, BIND_AUTO_CREATE);

        //listen to the BleService, since the UI isn't always going to be listening
        bleIncomingMessenger = new Messenger(new BleIncomingMessageHandler());
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.v(LOG_TAG, "onStart");
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(LOG_TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.v(LOG_TAG, "onDestroy");
        Message msg = Message.obtain(null, BleConstant.MESSAGE_UNREGISTER_MESSENGER);
        msg.replyTo = bleIncomingMessenger;
        sendMessageToApi(msg);
        super.onDestroy();
    }

    public void bleServiceConnected() {

    }

    public void alertPhoneOnPeripheralDisconnect(String peripheralAddress) {
        Log.v(LOG_TAG, "alertPhoneOnPeripheralDisconnect: " + peripheralAddress);
        boolean shouldAlert;
        PeripheralSettings peripheralSettings = ItemTrackerPersistenceHelper.getInstance(getApplicationContext()).
                getPeripheralSettings(peripheralAddress);
        if(peripheralSettings != null)
        {
            shouldAlert = peripheralSettings.isPhoneAudibleAlertOn() && !peripheralSettings.isPhoneAudibleAlertMuted();
        }
        else
        {
            shouldAlert = false;
        }
        Log.v(LOG_TAG, "should alert: " + shouldAlert + ", full settings: " + peripheralSettings);
        if (shouldAlert) {
            alertPhone(peripheralSettings.getPhoneAudibleAlertUri(),
                       peripheralSettings.getPhoneAudibleAlertVolume(),
                       peripheralSettings.getPhoneAlertDurationSeconds(),
                       peripheralSettings.isPhoneVibrateAlertOn()
            );
        }
      //  externalAlertHelper.sendExternalAlerts(peripheralAddress);
    }

    public void alertPhone(Uri uri, int volume, int duration, boolean vibrate) {
        AlertHelperMediaPlayer alertHelperMediaPlayer = new AlertHelperMediaPlayer();
        alertHelperMediaPlayer.setup(getApplicationContext(), uri, volume, duration, vibrate);
        alertHelperMediaPlayer.play();
    }

    public AlertHelperMediaPlayer alertPhoneServer(Uri uri, int volume, int duration, boolean vibrate) {
        AlertHelperMediaPlayer alertHelperMediaPlayer = new AlertHelperMediaPlayer();
        alertHelperMediaPlayer.setup(getApplicationContext(), uri, volume, duration, vibrate);
        alertHelperMediaPlayer.play();
        return alertHelperMediaPlayer;
    }

    public Location getLastKnownLocationForPeripheral(String btAddress) {
        return locationHelper.getLastKnownLocationForPeripheral(btAddress);
    }

    public void persistPeripheralSettings(PeripheralSettings peripheralSettings) {
        ItemTrackerPersistenceHelper.getInstance(getApplicationContext()).persistPeripheralSettings(peripheralSettings);
    }

    public PeripheralSettings getPeripheralSettings(String peripheralAddress) {
        return ItemTrackerPersistenceHelper.getInstance(getApplicationContext()).getPeripheralSettings(peripheralAddress);
    }

    public void persistExternalAlertSettings(ExternalAlertSettings externalAlertSettings) {
        ItemTrackerPersistenceHelper.getInstance(getApplicationContext()).persistExternalAlertSettings(externalAlertSettings);
    }

    public ExternalAlertSettings getExternalAlertSettings() {
        return ItemTrackerPersistenceHelper.getInstance(getApplicationContext()).getExternalAlertSettings();
    }

    //dev testing only
    public void sendEmailAlert(String peripheralAddress) {
        externalAlertHelper.sendEmailAlert(peripheralAddress);
    }

    //dev testing only
    public void sendTwitterAlert(String peripheralAddress) {
        externalAlertHelper.sendTwitterAlert(peripheralAddress);
    }

    //dev testing only
    public void sendFacebookAlert(String peripheralAddress) {
        externalAlertHelper.sendFacebookAlert(peripheralAddress);
    }

    public class BleIncomingMessageHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg) {
            Log.v(LOG_TAG, "handleMessage: " + msg);
            Bundle bundle;
            BluetoothDevice peripheral;
            switch (msg.what)
            {
                case BleConstant.MESSAGE_CONNECTED:
                    bundle = msg.getData();
                    peripheral = bundle.getParcelable(BleConstant.KEY_DEVICE);
                    Log.v(LOG_TAG, "connected to peripheral: " + peripheral.getAddress());
                    PeripheralSettings settings = getPeripheralSettings(peripheral.getAddress());
                    if(settings == null)
                    {
                        settings = new PeripheralSettings();
                        settings.setPeripheralAddress(peripheral.getAddress());
                        persistPeripheralSettings(settings);
                    }
                    settings.setDeviceConnectionState(true);
                    persistPeripheralSettings(settings);

                    locationHelper.updateLastKnownLocation(getApplicationContext(), peripheral.getAddress(), true);
                    break;
                case BleConstant.MESSAGE_DISCONNECTED:
                    Log.v(LOG_TAG, "Disconnect!");
                    bundle = msg.getData();
                    peripheral = bundle.getParcelable(BleConstant.KEY_DEVICE);

                    PeripheralSettings settings2 = getPeripheralSettings(peripheral.getAddress());
                    if(settings2 == null)
                    {
                        settings = new PeripheralSettings();
                        settings.setPeripheralAddress(peripheral.getAddress());
                        persistPeripheralSettings(settings);
                    }
                    else
                    {
                    settings2.setDeviceConnectionState(false);
                    persistPeripheralSettings(settings2);
                    }

                    locationHelper.updateLastKnownLocation(getApplicationContext(), peripheral.getAddress(), true);
                    alertPhoneOnPeripheralDisconnect(peripheral.getAddress());
                    break;
                case BleConstant.MESSAGE_SERVER_ALERT:
                    bundle = msg.getData();
                    peripheral = bundle.getParcelable(BleConstant.KEY_DEVICE);
                    int alertLevel = msg.arg1;
                    PeripheralSettings peripheralSettings = ItemTrackerPersistenceHelper.getInstance(getApplicationContext()).
                            getPeripheralSettings(peripheral.getAddress());
                    if(peripheralSettings != null)
                    {
                    if(alertLevel > 0)
                    {
                    alertServerPlayer = alertPhoneServer(peripheralSettings.getPhoneAudibleAlertUri(),
                            100,
                            300,
                            true
                    );
                    }
                    else
                    {
                        if(alertServerPlayer != null)
                        {
                            alertServerPlayer.stop();
                        }
                    }
                    }
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }
    private void sendMessageToApi(Message msg)
    {
        try {
            phBleService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        }

    }

    //lifecycle logging
    @Override
    public void onLowMemory() {
        Log.v(LOG_TAG, "onLowMemory");
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        Log.v(LOG_TAG, "onTrimMemory: " + level);
        super.onTrimMemory(level);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.v(LOG_TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.v(LOG_TAG, "onRebind");
        super.onRebind(intent);
    }

    private class PhBleServiceConnection implements ServiceConnection
    {

        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            phBleService = new Messenger(iBinder);
            isBound = true;
            Message msg = Message.obtain(null, BleConstant.MESSAGE_REGISTER_MESSENGER);
            msg.replyTo = bleIncomingMessenger;
            sendMessageToApi(msg);
        }

        public void onServiceDisconnected(ComponentName componentName) {
            phBleService = null;
            isBound = false;
        }

    }
}
