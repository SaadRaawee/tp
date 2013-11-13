package com.phonehalo.itemtracker.helper;

import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.*;
import com.phonehalo.ble.BleConstant;
import com.phonehalo.ble.service.PHBleService;
import com.phonehalo.itemtracker.db.ItemTrackerPersistenceHelper;
import com.phonehalo.itemtracker.fragment.ItemHomeFragment;
import com.phonehalo.itemtracker.fragment.SettingsFragment;
import com.phonehalo.itemtracker.utility.Log;

import java.util.ArrayList;

public class BleServiceHelper {

    private static final String LOG_TAG = "BleServiceHelper";

    public static final int DEFAULT_RSSI_TIMER_INTERVAL = 8000;
    public static final int DEFAULT_LINKLOSS_ALERT_LEVEL = 2;

    private static BleServiceHelper theInstance;
    private ItemHomeFragment homeFragment;
    private SettingsFragment settingsFragment;
    private Messenger bleServiceIncomingMessenger;
    private BleIncomingMessageHandler bleIncomingMessageHandler;
    private Messenger bleServiceOutgoingMessenger;
    private boolean isBound;


    public static BleServiceHelper getInstance(ItemHomeFragment homeFragment, SettingsFragment settingsFragment) {
        if (theInstance == null) {
            theInstance = new BleServiceHelper(homeFragment, settingsFragment);
            Log.v(LOG_TAG, "create singleton instance: " + theInstance);
            Log.v(LOG_TAG, "settingsFragment: " + settingsFragment);
            Log.v(LOG_TAG, "homeFragment: " + homeFragment);
        }
        return theInstance;
    }

    //requires theInstance to be already initialized
    public static BleServiceHelper getInstance() {
        return theInstance;
    }

    public void destroy() {
        Log.v(LOG_TAG, "destroy singleton instance: " + theInstance);
        sendMessage(BleConstant.MESSAGE_UNREGISTER_MESSENGER, null);
        theInstance = null;
        settingsFragment = null;
        homeFragment = null;
    }

    public BleServiceHelper(ItemHomeFragment homeFragment, SettingsFragment settingsFragment) {
        this.homeFragment = homeFragment;
        this.settingsFragment = settingsFragment;
        bleIncomingMessageHandler = new BleIncomingMessageHandler(Looper.getMainLooper());
        bleServiceIncomingMessenger = new Messenger(bleIncomingMessageHandler);
    }

    public void startService(Context context) {
        Intent bleServiceIntent = new Intent(context, PHBleService.class);
        context.startService(bleServiceIntent);

        BleServiceConnection bleServiceConnection = new BleServiceConnection();
        context.bindService(bleServiceIntent, bleServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void onServiceConnected(IBinder iBinder) {
        Log.v(LOG_TAG, "service Connected");
        bleServiceOutgoingMessenger = new Messenger(iBinder);
        isBound = true;
        sendMessage(BleConstant.MESSAGE_REGISTER_MESSENGER, null);
        ItemTrackerServiceHelper.getInstance().bleServiceConnected();

        requestPeripherals();
    }

    private void onServiceDisconnected() {
        bleServiceOutgoingMessenger = null;
        isBound = false;
    }

    public void requestPeripherals() {
        sendMessage(BleConstant.MESSAGE_GET_BONDED_DEVICES, null);
    }

    public void sendMessage(int message, Parcelable data) {
        //replyTo is default incoming messenger
        sendMessage(message, data, bleServiceIncomingMessenger, 0);
    }

    public void sendMessage(int message, Parcelable data, Messenger replyTo) {
        //replyTo is default incoming messenger
        sendMessage(message, data, replyTo, 0);
    }

    public void sendMessage(int message, Parcelable data, int arg1) {
        //replyTo is default incoming messenger
        sendMessage(message, data, bleServiceIncomingMessenger, arg1);
    }


    //todo consider converting this to a set of methods
    //replyTo is for a specialized messenger other than default
    public void sendMessage(int message, Parcelable data, Messenger replyTo, int arg1) {
        if (!isBound) return;
        Message msg = null;
        if (message == BleConstant.MESSAGE_DISCOVER) {
            msg = Message.obtain(null, BleConstant.MESSAGE_DISCOVER);
        } else if (message == BleConstant.MESSAGE_CONNECT) {
            msg = Message.obtain(null, BleConstant.MESSAGE_CONNECT);
            Bundle bundle = new Bundle();
            bundle.putParcelable(BleConstant.KEY_DEVICE, data);
            msg.setData(bundle);
        } else if (message == BleConstant.MESSAGE_REGISTER_MESSENGER) {
            msg = Message.obtain(null, BleConstant.MESSAGE_REGISTER_MESSENGER);
            msg.replyTo = replyTo;
        } else if (message == BleConstant.MESSAGE_UNREGISTER_MESSENGER) {
            msg = Message.obtain(null, BleConstant.MESSAGE_UNREGISTER_MESSENGER);
            msg.replyTo = replyTo;
        } else if (message == BleConstant.MESSAGE_RING) {
            msg = Message.obtain(null, BleConstant.MESSAGE_RING);
            Bundle bundle = new Bundle();
            bundle.putParcelable(BleConstant.KEY_DEVICE, data);
            msg.setData(bundle);
            msg.arg1 = arg1;
        } else if (message == BleConstant.MESSAGE_SETRSSITIMER) {
            msg = Message.obtain(null, BleConstant.MESSAGE_SETRSSITIMER);
            msg.arg1 = DEFAULT_RSSI_TIMER_INTERVAL;
        } else if (message == BleConstant.MESSAGE_SETLINKLOSS) {
            msg = Message.obtain(null, BleConstant.MESSAGE_SETLINKLOSS);
            Bundle bundle = new Bundle();
            bundle.putParcelable(BleConstant.KEY_DEVICE, data);
            msg.setData(bundle);
            msg.arg1 = DEFAULT_LINKLOSS_ALERT_LEVEL;
        } else if (message == BleConstant.MESSAGE_REMOVE_DEVICE) {
            msg = Message.obtain(null, BleConstant.MESSAGE_REMOVE_DEVICE);
            Bundle bundle = new Bundle();
            bundle.putParcelable(BleConstant.KEY_DEVICE, data);
            msg.setData(bundle);
        } else if (message == BleConstant.MESSAGE_READ_BATTERY) {
            msg = Message.obtain(null, BleConstant.MESSAGE_READ_BATTERY);
            Bundle bundle = new Bundle();
            bundle.putParcelable(BleConstant.KEY_DEVICE, data);
            msg.setData(bundle);
            Log.v(LOG_TAG, "read battery bundle for peripheral: " + bundle);
        } else if (message == BleConstant.MESSAGE_GET_BONDED_DEVICES) {
            msg = Message.obtain(null, BleConstant.MESSAGE_GET_BONDED_DEVICES);
        }

        try {
            Log.v(LOG_TAG, "sending message: " + msg);
            bleServiceOutgoingMessenger.send(msg);
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "Failed to send message to service", e);
        }
    }


    private class BleServiceConnection implements ServiceConnection {

        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            BleServiceHelper.this.onServiceConnected(iBinder);
        }

        public void onServiceDisconnected(ComponentName componentName) {
            BleServiceHelper.this.onServiceDisconnected();
        }
    }

    private class BleIncomingMessageHandler extends Handler {

        public BleIncomingMessageHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            try {
                handleMessage2(msg);
            } catch (IllegalStateException e) {
                Log.e(LOG_TAG, "problem delivering message to a fragment without an attached activity", e);
                Log.e(LOG_TAG, "BleServiceHelper instance: " + theInstance);
                Log.e(LOG_TAG, "settingsFragment: " + settingsFragment);
                Log.e(LOG_TAG, "homeFragment: " + homeFragment);
            }
        }

        public void handleMessage2(Message msg) {
            Log.v(LOG_TAG, "handleMessage: " + msg);
            Log.v(LOG_TAG, "current thread is Main? : " + Thread.currentThread().equals(Looper.getMainLooper().getThread()));
            if (theInstance == null || settingsFragment == null || homeFragment == null) { return; }
            Bundle bundle;
            BluetoothDevice peripheral;
            switch (msg.what) {
                case BleConstant.MESSAGE_DISCOVERED_DEVICES:
                    bundle = msg.getData();
                    ArrayList<BluetoothDevice> discoveredDevices = bundle.getParcelableArrayList(BleConstant.KEY_DEVICE_LIST);
                    Log.v(LOG_TAG, "discovered peripherals: " + discoveredDevices.toString());
                    break;
                case BleConstant.MESSAGE_CONNECTED:
                    Log.v(LOG_TAG, "connected to peripheral");
                    bundle = msg.getData();
                    peripheral = bundle.getParcelable(BleConstant.KEY_DEVICE);

                    Log.v(LOG_TAG, "adding peripheral " + peripheral);
                    homeFragment.peripheralConnected(peripheral);
                    settingsFragment.peripheralConnected(peripheral);
                    BleServiceHelper.this.sendMessage(BleConstant.MESSAGE_SETRSSITIMER, null);
                    BleServiceHelper.this.sendMessage(BleConstant.MESSAGE_SETLINKLOSS, peripheral);
                    break;
                case BleConstant.MESSAGE_RSSI_UPDATE:
                    bundle = msg.getData();
                    peripheral = bundle.getParcelable(BleConstant.KEY_DEVICE);

                    Log.v(LOG_TAG, "Peripheral: " + peripheral.getAddress() + " has RSSI: " + msg.obj);


                    homeFragment.peripheralRssiUpdate(peripheral, (Short) msg.obj);
                    break;
                case BleConstant.MESSAGE_DISCONNECTED:
                    bundle = msg.getData();
                    peripheral = bundle.getParcelable(BleConstant.KEY_DEVICE);

                    homeFragment.peripheralDisconnected(peripheral);
                    break;
                case BleConstant.MESSAGE_BATTERY_UPDATE:
                    bundle = msg.getData();
                    peripheral = bundle.getParcelable(BleConstant.KEY_DEVICE);
                    //get battery update out of the bundle or param
                    int batteryPercent = msg.arg1;
                    homeFragment.peripheralBatteryUpdate(peripheral, batteryPercent);
                    break;
                case BleConstant.MESSAGE_BONDED_DEVICES:
                    Log.v(LOG_TAG, "bonded devices message: " + msg);
                    Log.v(LOG_TAG, "bonded devices data bundle: " + msg.getData());

                    bundle = msg.getData();
                    ArrayList<BluetoothDevice> bondedPeripherals = bundle.getParcelableArrayList(BleConstant.KEY_DEVICE_LIST);
                    Log.v(LOG_TAG, "Received bonded peripherals: " + bondedPeripherals);
                    for (BluetoothDevice bondedPeripheral : bondedPeripherals) {
                        //ensure that we have default settings available
                        Log.v(LOG_TAG, "homeFragment activity: " + homeFragment.getActivity());
                        if (homeFragment.getActivity() != null) {
                            ItemTrackerPersistenceHelper itemTrackerPersistenceHelper = ItemTrackerPersistenceHelper.getInstance(homeFragment.getActivity().getApplicationContext());
                            itemTrackerPersistenceHelper.persistDefaultSettingsIfNecessary(bondedPeripheral);
                        }
                    }
                    homeFragment.setPeripherals(bondedPeripherals);
                    settingsFragment.setPeripherals(bondedPeripherals);
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

}
