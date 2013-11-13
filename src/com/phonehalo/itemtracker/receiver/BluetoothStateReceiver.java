package com.phonehalo.itemtracker.receiver;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.phonehalo.itemtracker.fragment.ItemHomeFragment;

public class BluetoothStateReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "BluetoothStateReceiver";
    public static final String IMMEDIATEALERTCHANGED = "com.phonehalo.ble.immediatealertchanged";
    private final ItemHomeFragment itemHomeFragment;

    public BluetoothStateReceiver(ItemHomeFragment itemHomeFragment) {
        this.itemHomeFragment = itemHomeFragment;
    }

    public void onReceive(Context context, Intent intent) {

        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
            int btState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            Log.d(LOG_TAG, "Received state change = " + btState);

            if (btState == BluetoothAdapter.STATE_ON) {
//                context.startService(new Intent(context, PXPService.class));
            }
            if (btState == BluetoothAdapter.STATE_TURNING_OFF) {
//                context.stopService(new Intent(context, PXPService.class));
            }
        } else if (IMMEDIATEALERTCHANGED.equals(intent.getAction())) {
            int alert_level;
            String remoteDeviceAddr = intent.getStringExtra("remoteDeviceAddr");
            alert_level = intent.getIntExtra("alert_level", 0);
            Log.d(LOG_TAG, "alert_level change : " + alert_level);
            Log.d(LOG_TAG, "device remoteDeviceAddr : " + remoteDeviceAddr);
        }
    }
}
