package com.phonehalo.itemtracker.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.*;
import android.view.View;
import com.facebook.Session;
import com.phonehalo.ble.BleConstant;
import com.phonehalo.ble.service.PHBleService;
import com.phonehalo.itemtracker.R;
import com.phonehalo.itemtracker.db.ItemTrackerPersistenceHelper;
import com.phonehalo.itemtracker.db.PeripheralSettings;
import com.phonehalo.itemtracker.fragment.ItemHomeFragment;
import com.phonehalo.itemtracker.fragment.NavPagerInterface;
import com.phonehalo.itemtracker.fragment.SettingsFragment;
import com.phonehalo.itemtracker.helper.ItemTrackerServiceHelper;
import com.phonehalo.itemtracker.utility.DisplayMetricsHelper;
import com.phonehalo.itemtracker.utility.Log;

import java.util.ArrayList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class NavigationActivity extends Activity implements NavPagerInterface {
    private static final String LOG_TAG = "NavigationActivity";

    private ServiceConnection phBleServiceConnection = new PhBleServiceConnection();
    private Messenger phBleService = null;
    private boolean isBound = false;


    public static final int ALL_TABS = 0;
    public static final int HOME_TAB = 1;
    public static final int SETTINGS_TAB = 2;

    private View currentTab;

    private View homeTab;
    private View settingsTab;

    private ItemTrackerServiceHelper itemTrackerServiceHelper;
    private ItemHomeFragment homeFragment;
    private SettingsFragment settingsFragment;
    private View homeTabControl;
    private View settingsTabControl;

    final Messenger myMessenger = new Messenger(new ApiHandler());

    /********
     * Handles API Communications
     */
    public class ApiHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {
            final Bundle bundle = msg.getData();
            if(itemTrackerServiceHelper == null)
            {
                itemTrackerServiceHelper = ItemTrackerServiceHelper.getInstance(homeFragment, settingsFragment);
            }

            switch (msg.what)
            {
                case BleConstant.MESSAGE_RSSI_UPDATE:
                    Log.v(LOG_TAG, "Rssi Update");
                    final Short rssiValue = (Short)msg.obj;
                    if(rssiValue<-1)
                    {
                        BluetoothDevice mDevice = (BluetoothDevice)bundle.getParcelable(BleConstant.KEY_DEVICE);

                    }
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            homeFragment.peripheralRssiUpdate((BluetoothDevice)bundle.getParcelable(BleConstant.KEY_DEVICE), rssiValue);
                        }
                    }).start();


                    break;
                case BleConstant.MESSAGE_DISCONNECTED:
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                Log.v(LOG_TAG, "Disconnect!");
                            BluetoothDevice disconnectedDevice = (BluetoothDevice)bundle.getParcelable(BleConstant.KEY_DEVICE);
                            itemTrackerServiceHelper = ItemTrackerServiceHelper.getInstance();
//                            PeripheralSettings settings = itemTrackerServiceHelper.getPeripheralSettings(disconnectedDevice.getAddress());
//                            settings.setDeviceConnectionState(false);
//                            itemTrackerServiceHelper.persistPeripheralSettings(settings);
                            homeFragment.peripheralDisconnected(disconnectedDevice);
                            }catch (NullPointerException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    break;
                case BleConstant.MESSAGE_CONNECTED:
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.v(LOG_TAG, "Peripheral Connected Message");
                            final BluetoothDevice connectedDevice = (BluetoothDevice)bundle.getParcelable(BleConstant.KEY_DEVICE);
//                            itemTrackerServiceHelper = ItemTrackerServiceHelper.getInstance();
//                            PeripheralSettings settings = itemTrackerServiceHelper.getPeripheralSettings(connectedDevice.getAddress());
//                            if(settings == null)
//                            {
//                                settings = new PeripheralSettings();
//                                settings.setPeripheralAddress(connectedDevice.getAddress());
//                                itemTrackerServiceHelper.persistPeripheralSettings(settings);
//                            }
//                            settings.setDeviceConnectionState(true);
//                            itemTrackerServiceHelper.persistPeripheralSettings(settings);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    homeFragment.peripheralConnected(connectedDevice);
                                }
                            });

                            Message msgNew = Message.obtain(null, BleConstant.MESSAGE_SETRSSITIMER);
                            msgNew.arg1 = 8000;
                            sendMessageToApi(msgNew);


                        }
                    }).start();

                    Timer llswritetimer = new Timer();
                    llswritetimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            try{
                            ItemTrackerPersistenceHelper helper = ItemTrackerPersistenceHelper.getInstance(getApplicationContext());
                            BluetoothDevice tempDevice = bundle.getParcelable(BleConstant.KEY_DEVICE);
                            PeripheralSettings peripheralSettings = helper.getPeripheralSettings(tempDevice.getAddress());
                            int linkLossValue = peripheralSettings.getPeripheralAlertDuration();
                            Log.v(LOG_TAG, "link loss is " + linkLossValue);
                            Message msgNew2 = Message.obtain(null, BleConstant.MESSAGE_SETLINKLOSS);
                            msgNew2.setData(bundle);
                            msgNew2.arg1 = linkLossValue;
                            sendMessageToApi(msgNew2);
                            }catch (NullPointerException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }, 30000);
                    break;
                case BleConstant.MESSAGE_BONDED_DEVICES:
                    Log.v(LOG_TAG, "bonded devices message: " + msg);
                    Log.v(LOG_TAG, "bonded devices data bundle: " + msg.getData());

                    Bundle bundleDevices = msg.getData();
                    final ArrayList<BluetoothDevice> bondedPeripherals = bundleDevices.getParcelableArrayList(BleConstant.KEY_DEVICE_LIST);
                    Log.v(LOG_TAG, "Received bonded peripherals: " + bondedPeripherals);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            homeFragment.setPeripherals(bondedPeripherals);
                        }
                    });

                    break;
                default:
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


    public void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        DisplayMetricsHelper.logDisplayMetricsSummary(getApplicationContext());
        setContentView(R.layout.main_navigation_tabs_layout);

        homeTab = findViewById(R.id.tab_home);
        settingsTab = findViewById(R.id.tab_settings);

        homeTabControl = findViewById(R.id.tabControlHome);
        settingsTabControl = findViewById(R.id.tabControlSettings);

        settingsFragment = new SettingsFragment();
        settingsFragment.setNavigationActivity(this);
        homeFragment = new ItemHomeFragment();
        homeFragment.setNavigationActivity(this);

        //when the service connects, we want the service to tell those main components who need to know, so they can update
        itemTrackerServiceHelper = ItemTrackerServiceHelper.getInstance(homeFragment, settingsFragment);
        itemTrackerServiceHelper.startService(getApplicationContext());

        Intent phBleServiceStarter = new Intent(getApplicationContext(), PHBleService.class);
        
        
        
        if(!isPhBleServiceRunning())
        {
            startService(phBleServiceStarter);
        }
        getApplicationContext().bindService(phBleServiceStarter, phBleServiceConnection, BIND_AUTO_CREATE);

        FragmentTransaction fTx = getFragmentManager().beginTransaction();
        fTx.add(R.id.tab_home, homeFragment);
        fTx.add(R.id.tab_settings, settingsFragment);
        fTx.commit();

        currentTab = settingsTab;
        selectTab(homeTab);
    }



    private void selectTab(View tabView) {
        if (currentTab == tabView) { return; }

        View previousTab = currentTab;
        currentTab = tabView;

        currentTab.setVisibility(View.VISIBLE);
        previousTab.setVisibility(View.INVISIBLE);

        updateTabAppearance();
    }

    private void updateTabAppearance() {
//        switch (currentTab.getId()) {
//            case R.id.tab_home:
//                homeTabControl.setBackgroundColor(getResources().getColor(R.color.DodgerBlue));
//                settingsTabControl.setBackgroundColor(getResources().getColor(R.color.DarkSlateGrayTransparent));
//                break;
//            case R.id.tab_settings:
//                homeTabControl.setBackgroundColor(getResources().getColor(R.color.DarkSlateGrayTransparent));
//                settingsTabControl.setBackgroundColor(getResources().getColor(R.color.DodgerBlue));
//                break;
//            default:
//                Log.e(LOG_TAG, "Unknown Tab");
//        }
    }

    public void onClick(View view) {
//        Log.v(LOG_TAG, "onClick: " + view);
        switch (view.getId()) {
            case R.id.tabControlHome:
                selectHomeTab();
                //when we select the home tab, ensure that the settings tab values are saved
                settingsFragment.saveSettings();
                break;
            case R.id.tabControlSettings:
                selectSettingsTab();
                break;
            default:
                switch (currentTab.getId()) {
                    case R.id.tab_home:
                        homeFragment.onClick(view);
                        break;
                    case R.id.tab_settings:
                        settingsFragment.onClick(view);
                        break;
                }
        }
    }

    public void selectHomeTab() {
        selectTab(homeTab);
    }
    public void selectSettingsTab() {
        selectTab(settingsTab);
    }
    public void refresh(int tabId) {
        switch (tabId) {
            case HOME_TAB:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        homeFragment.refresh();
                    }
                });
                break;
            case SETTINGS_TAB:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        settingsFragment.refresh();
                    }
                });

                break;
            case ALL_TABS:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        homeFragment.refresh();
                        settingsFragment.refresh();
                    }
                });
                break;
        }
    }

    @Override
    protected void onResume() {
        Log.v(LOG_TAG, "onResume, service connected: " + itemTrackerServiceHelper.isConnected());
//                Message msgBond = Message.obtain(null, BleConstant.MESSAGE_GET_BONDED_DEVICES);
//                sendMessageToApi(msgBond);


        Message msg = Message.obtain(null, BleConstant.MESSAGE_REGISTER_MESSENGER);
        msg.replyTo = myMessenger;
        sendMessageToApi(msg);

        if (itemTrackerServiceHelper.isConnected()) {
            //only refresh when connected, since a pending connection will generate a refresh anyway
            //also, if we refresh before a pending connection completes, we will show a "no devices" screen
            //that quickly changes to a device list
            homeFragment.setItemTrackerServiceHelper(itemTrackerServiceHelper);
            settingsFragment.setItemTrackerServiceHelper(itemTrackerServiceHelper);

            ItemTrackerPersistenceHelper helper = ItemTrackerPersistenceHelper.getInstance(getApplicationContext());
            Set<BluetoothDevice> deviceSet;
            final ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
            if(Build.VERSION.SDK_INT <= 17)
            {
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                deviceSet = adapter.getBondedDevices();
            }
            else
            {
                //   BluetoothManager

                BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                final BluetoothAdapter adapter = manager.getAdapter();
                Log.v(LOG_TAG, "adapter is " + adapter.getName());
                deviceSet = adapter.getBondedDevices();
                Log.v(LOG_TAG,"get device set " +deviceSet.size());
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        refreshTheItemList(adapter);
                    }
                };
                Timer timer = new Timer();
                timer.schedule(task, 5000);

            }
            for (BluetoothDevice device : deviceSet) {
                if (helper.isPeripheralKnown(device.getAddress())) {
                    Log.v(LOG_TAG, "device is " + device.getAddress());
                    devices.add(device);
                }
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    homeFragment.setPeripherals(devices);
                }
            });

            refresh(ALL_TABS);
        }

        Message msgNew = Message.obtain(null, BleConstant.MESSAGE_SETRSSITIMER);
        msgNew.arg1 = 8000;
        sendMessageToApi(msgNew);
        super.onResume();
    }

    private void refreshTheItemList(BluetoothAdapter adapter)
    {
        ItemTrackerPersistenceHelper helper = ItemTrackerPersistenceHelper.getInstance(getApplicationContext());
        Set<BluetoothDevice> deviceSet;
        final ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();


            Log.v(LOG_TAG, "adapter is " + adapter.getName());
            deviceSet = adapter.getBondedDevices();
            Log.v(LOG_TAG, "get device set " + deviceSet.size());

        for (BluetoothDevice device : deviceSet) {
            if (helper.isPeripheralKnown(device.getAddress())) {
                Log.v(LOG_TAG, "device is " + device.getAddress());
                devices.add(device);
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                homeFragment.setPeripherals(devices);
            }
        });
        refresh(ALL_TABS);
    }

    public void peripheralDeleted(String peripheralAddress) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = adapter.getRemoteDevice(peripheralAddress);
        Message msg = Message.obtain(null, BleConstant.MESSAGE_REMOVE_DEVICE);
        Bundle bundle = new Bundle();
        bundle.putParcelable(BleConstant.KEY_DEVICE, device);
        msg.setData(bundle);
        sendMessageToApi(msg);
        homeFragment.peripheralDeleted(peripheralAddress);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(LOG_TAG, "onPause");
        Message msg = Message.obtain(null, BleConstant.MESSAGE_UNREGISTER_MESSENGER);
        msg.replyTo = myMessenger;
        sendMessageToApi(msg);

    }

    //Lifecycle overrides for logging

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG, "onDestroy");
        itemTrackerServiceHelper.destroy();
        itemTrackerServiceHelper = null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(LOG_TAG, "onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.v(LOG_TAG, "onRestart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(LOG_TAG, "onStop");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.v(LOG_TAG, "onLowMemory");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Log.v(LOG_TAG, "onTrimMemory: " + level);
    }

    public void startDiscoveringDevices() {
        settingsFragment.startDiscoveringDevices();
    }

    private boolean isPhBleServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (PHBleService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private class PhBleServiceConnection implements ServiceConnection
    {

        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.e(LOG_TAG, "service Connected");
            phBleService = new Messenger(iBinder);
            isBound = true;
            Message msg = Message.obtain(null, BleConstant.MESSAGE_REGISTER_MESSENGER);
            msg.replyTo = myMessenger;
            sendMessageToApi(msg);
            refresh(ALL_TABS);
            ItemTrackerPersistenceHelper helper = ItemTrackerPersistenceHelper.getInstance(getApplicationContext());
            //helper.getTrackedPeripherals();
            Set<BluetoothDevice> deviceSet;
            if(Build.VERSION.SDK_INT <= 17)
            {
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                deviceSet = adapter.getBondedDevices();
            }
            else
            {
             //   BluetoothManager
                BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                deviceSet = manager.getAdapter().getBondedDevices();
            }
            final ArrayList<BluetoothDevice> trackedDevices = new ArrayList<BluetoothDevice>();
            for (BluetoothDevice device : deviceSet)
            {
                Log.v(LOG_TAG,"get device set " +deviceSet.size());
                if(helper.isPeripheralKnown(device.getAddress()))
                {
                    msg = Message.obtain(null, BleConstant.MESSAGE_CONNECT);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(BleConstant.KEY_DEVICE,device);
                    msg.setData(bundle);
                    sendMessageToApi(msg);
                    trackedDevices.add(device);
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    homeFragment.setPeripherals(trackedDevices);
                }
            });
        }

        public void onServiceDisconnected(ComponentName componentName) {
            phBleService = null;
            isBound = false;
        }
    }

    /**********
     * CallBack Handlers
     */
    public boolean ringDevice(BluetoothDevice device)
    {
        Message msg = Message.obtain(null, BleConstant.MESSAGE_RING);
        msg.arg1 = 2;
        Bundle bundle = new Bundle();
        bundle.putParcelable(BleConstant.KEY_DEVICE, device);
        msg.setData(bundle);
        sendMessageToApi(msg);
        return true;
    }

    public boolean silenceDevice(BluetoothDevice device)
    {
        Message msg = Message.obtain(null, BleConstant.MESSAGE_RING);
        msg.arg1 = 0;
        Bundle bundle = new Bundle();
        bundle.putParcelable(BleConstant.KEY_DEVICE, device);
        msg.setData(bundle);
        sendMessageToApi(msg);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(LOG_TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode,
                resultCode, data);
    }

}