package com.phonehalo.itemtracker.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.os.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.phonehalo.ble.BleConstant;
import com.phonehalo.ble.service.PHBleService;
import com.phonehalo.itemtracker.Constant;
import com.phonehalo.itemtracker.R;
import com.phonehalo.itemtracker.db.ItemTrackerPersistenceHelper;
import com.phonehalo.itemtracker.helper.IntentHelper;
import com.phonehalo.itemtracker.utility.Log;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class AddItemActivity extends Activity {
    private static final String LOG_TAG = "AddItemActivity";

    private ServiceConnection phBleServiceConnection = new PhBleServiceConnection();
    private Messenger phBleService = null;
    private boolean isBound = false;

    private ViewGroup deviceList;
    private TextView errorText;
    private ArrayList<BluetoothDevice> peripherals;
    private BluetoothDevice peripheralPendingAdd;
    private ProgressBar progressBar;
    private Boolean connectionSuccess;

    private Timer progressTimer;

    final Messenger bleIncomingMessenger = new Messenger(new BleIncomingMessageHandler());

    private class BleIncomingMessageHandler extends Handler {

        public void handleMessage(Message message) {
            Log.v(LOG_TAG, "handleMessage: " + message);
            Log.v(LOG_TAG, "current thread is Main? : " + Thread.currentThread().equals(Looper.getMainLooper().getThread()));
            switch (message.what)
            {
                case BleConstant.MESSAGE_DISCOVERED_DEVICES:
                    onDiscoveredDevices(message);
                    break;
                case BleConstant.MESSAGE_CONNECTED:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress(1200);
                        }
                    });
                    if(!connectionSuccess)
                    {
                    onPeripheralConnected(message);
                    }
                    break;
                case BleConstant.MESSAGE_RSSI_UPDATE:
                    onRssiUpdate(message);
                    break;
                case BleConstant.MESSAGE_ERROR_CONNECTING:
                    Bundle bundle = message.getData();
                    BluetoothDevice device = bundle.getParcelable(BleConstant.KEY_DEVICE);
                    errorConnectingToDevice(device);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress(600);
                        }
                    });
                    progressTimer.cancel();
                    break;
                case BleConstant.MESSAGE_ERROR_BONDING:
                    Bundle mBundle = message.getData();
                    BluetoothDevice mDevice = mBundle.getParcelable(BleConstant.KEY_DEVICE);
                    errorConnectingToDevice(mDevice);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress(600);
                        }
                    });
                    progressTimer.cancel();
                    break;
                case BleConstant.MESSAGE_FRAMEWORK_SUPPORT_ERROR:
                    frameworkSupportError();
                    break;
                default:
                    super.handleMessage(message);
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

        peripherals = new ArrayList<BluetoothDevice>();
        setContentView(R.layout.add_item_activity_layout);
        errorText = (TextView)findViewById(R.id.addItemErrorText);
        deviceList = (ViewGroup)findViewById(R.id.discoveredDeviceList);
        progressBar = (ProgressBar)findViewById(R.id.addDeviceProgressBar);

        Intent phBleServiceStarter = new Intent(getApplicationContext(), PHBleService.class);
        if(!isPhBleServiceRunning())
        {
            startService(phBleServiceStarter);
        }
        getApplicationContext().bindService(phBleServiceStarter, phBleServiceConnection, BIND_AUTO_CREATE);

    }

    private void frameworkSupportError()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.framework_error_msg).setTitle(R.string.framework_error);
        builder.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onPause() {
        Log.v(LOG_TAG, "onPause");
        super.onPause();

        Message msg = Message.obtain(null, BleConstant.MESSAGE_UNREGISTER_MESSENGER);
        msg.replyTo = bleIncomingMessenger;
        sendMessageToApi(msg);

    }

    @Override
    protected void onResume() {
        Log.v(LOG_TAG, "onResume");
        connectionSuccess = false;
        super.onResume();
        if(isBound)
        {
        Message msg = Message.obtain(null, BleConstant.MESSAGE_REGISTER_MESSENGER);
        msg.replyTo = bleIncomingMessenger;
        sendMessageToApi(msg);
        msg = Message.obtain(null, BleConstant.MESSAGE_DISCOVER);
        sendMessageToApi(msg);
        }
    }

    public void onClick(View view)  {
        Log.v(LOG_TAG, "onClick: " + view);
        Message msg;
        switch (view.getId()) {
            case R.id.buttonAddItemRefresh:
                msg = Message.obtain(null, BleConstant.MESSAGE_DISCOVER);
                sendMessageToApi(msg);
                progressTimer = new Timer();
                progressBar.setMax(1200);
                progressBar.setIndeterminate(false);
                progressBar.setProgress(0);
                progressBar.setVisibility(View.VISIBLE);

                progressTimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if(progressBar.getProgress() == 1200)
                        {
                            progressTimer.cancel();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setVisibility(View.INVISIBLE);
                                }
                            });
                        }
                        else
                        {
                            progressBar.setProgress(progressBar.getProgress()+1);
                        }
                    }
                },10,10);
                break;
            case R.id.buttonCancel:
                msg = Message.obtain(null, BleConstant.MESSAGE_UNREGISTER_MESSENGER);
                msg.replyTo = bleIncomingMessenger;
                sendMessageToApi(msg);
                finish();
                break;
        }
    }

    private void addPeripheral(BluetoothDevice peripheral, short rssi) {
        Log.v(LOG_TAG, "addPeripheral: " + peripheral.getAddress() + ", rssi: " + rssi);
        if (!peripherals.contains(peripheral)) {
            this.peripherals.add(peripheral);
//            ItemTrackerPersistenceHelper.getInstance(getApplicationContext()).persistDefaultSettingsIfNecessary(peripheral);
            displayPeripheralRow(peripheral, rssi);
        }
    }

    private void errorConnectingToDevice(BluetoothDevice device) {

        progressTimer.cancel();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.error_connecting).setTitle(R.string.failed_to_connect);
        builder.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void displayPeripheralRow(BluetoothDevice peripheral, short rssi) {
        Log.v(LOG_TAG, "addDevice: " + rssi + "/" + peripheral.getName());
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View discoveredDeviceView = layoutInflater.inflate(R.layout.add_item_discovered_device, null);
        int backgroudDrawableId = R.drawable.dot_grey;
        if (rssi > Constant.RSSI_MAX_MEDIUM && rssi < 0) {
            backgroudDrawableId = R.drawable.signal_bar_3;
        } if (rssi > Constant.RSSI_MAX_FAR) {
            backgroudDrawableId = R.drawable.signal_bar_2;
        } else if (rssi <= Constant.RSSI_MAX_FAR || rssi == 0) {
            backgroudDrawableId = R.drawable.signal_bar_1;
        }

        ImageView signalStrengthImage = (ImageView) discoveredDeviceView.findViewById(R.id.imageSignalStrength);
        signalStrengthImage.setBackgroundResource(backgroudDrawableId);

        TextView deviceNameTextView = (TextView)discoveredDeviceView.findViewById(R.id.itemName);
        deviceNameTextView.setText(peripheral.getName());

        discoveredDeviceView.setOnClickListener(new PeripheralClickListener(peripheral));
        discoveredDeviceView.setClickable(true);

        deviceList.addView(discoveredDeviceView);
    }

    private class PeripheralClickListener implements View.OnClickListener {

        BluetoothDevice device;
        PeripheralClickListener(BluetoothDevice peripheral) {
            Log.v(LOG_TAG, "creating PeripheralClickListener: " + peripheral.getAddress());
            this.device = peripheral;
        }


        @Override
        public void onClick(View v) {
            connectionSuccess = false;
            AddItemActivity.this.peripheralPendingAdd = this.device;
            Log.v(LOG_TAG, "onClick: " + this.device.getAddress());
            Message msg = Message.obtain(null, BleConstant.MESSAGE_CONNECT);
            Bundle bundle = new Bundle();
            bundle.putParcelable(BleConstant.KEY_DEVICE, this.device);
            msg.setData(bundle);
            sendMessageToApi(msg);

            progressBar.setMax(1200);
            progressBar.setIndeterminate(false);
            progressBar.setProgress(0);
            progressBar.setVisibility(View.VISIBLE);

            progressTimer = new Timer();

            progressTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if(progressBar.getProgress() == 600)
                    {
                        try{
                        progressTimer.cancel();
                        }
                        catch (IllegalStateException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        progressBar.setProgress(progressBar.getProgress()+1);
                    }
                }
            },10,10);
        }
    }

    private void onRssiUpdate(Message msg) {
        Log.v(LOG_TAG, "onRssiUpdate");
        Bundle bundle;
        bundle = msg.getData();
        BluetoothDevice peripheral = bundle.getParcelable(BleConstant.KEY_DEVICE);

        Log.v(LOG_TAG, "Device: " + peripheral.getAddress() + " has RSSI: " + msg.obj);

        Short rssi = (Short)msg.obj;
        this.addPeripheral(peripheral, rssi);
    }

    private void onDiscoveredDevices(Message msg) {
        Log.v(LOG_TAG, "onDiscoveredDevices");
        Bundle bundle = msg.getData();
        ArrayList<BluetoothDevice> discoveredDevices = bundle.getParcelableArrayList(BleConstant.KEY_DEVICE_LIST);
        Log.v(LOG_TAG, "devices are" + discoveredDevices.toString());
        for (BluetoothDevice peripheral : discoveredDevices)
        {
            if(peripheral != null  && peripheral.getName() != null)
            {
//              if(peripheral.getName().equalsIgnoreCase("inSite"))
//              {
//                  try
//                  {
                   addPeripheral(peripheral, (short)0);
//                  }catch (NullPointerException e)
//                  {
//                      e.printStackTrace();
//                  }
//              }
            }
        }
    }

    private void onPeripheralConnected(Message msg) {
        Log.v(LOG_TAG, "onPeripheralConnected");

        //if the connected peripheral matches the peripheralPendingAdd, then choose an icon for it, otherwise ignore
        Bundle bundle = msg.getData();
        BluetoothDevice connectedPeripheral = bundle.getParcelable(BleConstant.KEY_DEVICE);

        if ( peripheralPendingAdd != null &&
             connectedPeripheral != null &&
             connectedPeripheral.getAddress().equals(peripheralPendingAdd.getAddress()) ) {
            connectionSuccess = true;
            //the peripheral we just selected has been connected... go ahead and choose an icon
            Log.e(LOG_TAG, "Okay...NowAddThePeripheral with addr " + peripheralPendingAdd.getAddress());

            ItemTrackerPersistenceHelper persistenceHelper = ItemTrackerPersistenceHelper.getInstance(getApplicationContext());
            persistenceHelper.persistDefaultSettingsIfNecessary(peripheralPendingAdd);

            Intent chooseIconIntent = new Intent(getApplicationContext(), ChooseIconActivity.class);

            chooseIconIntent.putExtra(IntentHelper.EXTRA_DEVICE_ADDRESS, peripheralPendingAdd.getAddress());
            chooseIconIntent.putExtra(IntentHelper.EXTRA_DEVICE_NAME, peripheralPendingAdd.getName());
            chooseIconIntent.setAction(IntentHelper.ACTION_FIRST_CHOICE);
            startActivity(chooseIconIntent);

            finish();
        }
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
            msg.replyTo = bleIncomingMessenger;
            sendMessageToApi(msg);
        }

        public void onServiceDisconnected(ComponentName componentName) {
            phBleService = null;
            isBound = false;
        }
    }

    @Override
    public void onBackPressed() {
        Message msg = Message.obtain(null, BleConstant.MESSAGE_REMOVE_DEVICE);
        Bundle bundle = new Bundle();
        bundle.putParcelable(BleConstant.KEY_DEVICE, peripheralPendingAdd);
        msg.setData(bundle);
        sendMessageToApi(msg);
        super.onBackPressed();
    }
}