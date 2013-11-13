package com.phonehalo.itemtracker.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.*;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.phonehalo.ble.BleConstant;
import com.phonehalo.ble.service.PHBleService;
import com.phonehalo.itemtracker.R;
import com.phonehalo.itemtracker.db.ItemTrackerPersistenceHelper;
import com.phonehalo.itemtracker.db.PeripheralSettings;
import com.phonehalo.itemtracker.helper.IntentHelper;
import com.phonehalo.itemtracker.utility.Log;

public class ItemSettingsActivity extends Activity {
    private static final String LOG_TAG = "ItemSettingsActivity";
    private static final int REQUEST_CODE = 888;

    private EditText peripheralNameEditText;
    private TextView peripheralIconTextView;
    private SeekBar peripheralAlertDurationSeekBar;
    private TextView peripheralAlertDurationValueText;
    private ToggleButton phoneAudioAlertToggleButton;
    private ToggleButton phoneVibrateAlertToggleButton;
    private SeekBar phoneAlertVolumeSeekBar;
    private SeekBar phoneAlertDurationSeekBar;
    private TextView phoneAlertDurationValueText;
    private PeripheralSettings peripheralSettings;

    private ServiceConnection phBleServiceConnection = new PhBleServiceConnection();
    private Messenger phBleService = null;
    private boolean isBound = false;

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
        super.onCreate(savedInstanceState);

        setContentView(R.layout.item_settings_activity_layout);

        BluetoothDevice peripheral = getIntent().getParcelableExtra(IntentHelper.EXTRA_DEVICE);
        initializeUIReferences();
        initializeUIValues(peripheral.getAddress());
        initializeSeekBarListeners(peripheral);
        initializePeripheralNameListeners();

        Intent phBleServiceStarter = new Intent(getApplicationContext(), PHBleService.class);
        if(!isPhBleServiceRunning())
        {
            startService(phBleServiceStarter);
        }
        getApplicationContext().bindService(phBleServiceStarter, phBleServiceConnection, BIND_AUTO_CREATE);
    }

    private void initializeUIReferences() {
        peripheralNameEditText = (EditText)findViewById(R.id.contentPeripheralName);
        peripheralIconTextView = (TextView)findViewById(R.id.contentPeripheralIconName);
        peripheralAlertDurationSeekBar = (SeekBar)findViewById(R.id.seekBarPeripheralAlertDuration);
        peripheralAlertDurationValueText = (TextView)findViewById(R.id.valuePeripheralAlertDuration);
        phoneAudioAlertToggleButton = (ToggleButton)findViewById(R.id.togglePhoneAudioAlertOnOff);
        phoneVibrateAlertToggleButton = (ToggleButton)findViewById(R.id.togglePhoneVibrateAlertOnOff);
        phoneAlertVolumeSeekBar = (SeekBar)findViewById(R.id.seekBarPhoneAlertVolume);
        phoneAlertDurationSeekBar = (SeekBar)findViewById(R.id.seekBarPhoneAlertDuration);
        phoneAlertDurationValueText = (TextView)findViewById(R.id.valuePhoneAlertDuration);
    }

    private void initializeSeekBarListeners(final BluetoothDevice peripheral) {
        peripheralAlertDurationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.e(LOG_TAG, "value is " + progress);
                peripheralAlertDurationValueText.setText(getLabelForPeripheralAlertDuration(progress));
                saveSettings();

                Message msg = Message.obtain(null, BleConstant.MESSAGE_SETLINKLOSS);
                Bundle bundle = new Bundle();
                bundle.putParcelable(BleConstant.KEY_DEVICE,peripheral);
                msg.setData(bundle);
                msg.arg1 = progress;
                sendMessageToApi(msg);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { /** Do Nothing */ }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { /** Do Nothing */ }
        });

        phoneAlertVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                saveSettings();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { /** Do Nothing */}
            @Override public void onStopTrackingTouch(SeekBar seekBar) { /** Do Nothing */}
        });

        phoneAlertDurationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                phoneAlertDurationValueText.setText(getLabelForPhoneAlertDuration(progress));
                saveSettings();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { /** Do Nothing */ }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { /** Do Nothing */ }
        });
    }

    private void initializePeripheralNameListeners() {

        peripheralNameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                switch (actionId) {
                    case EditorInfo.IME_ACTION_DONE:
                        peripheralNameEditText.setCursorVisible(false);
                        saveSettings();
                        //allow "handled" to remain false, so the default behavior of the event will also trigger
                        //this will close the keyboard automatically
                        break;
                    default:
                        Log.e(LOG_TAG, "Unexpected EditText action");
                        break;
                }
                return handled;
            }
        });

        peripheralNameEditText.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                peripheralNameEditText.setCursorVisible(true);
                return false; //return not-handled, so we benefit from auto-closing of keyboard behavior... are there other side effects?
            }
        });
    }

    public void onClick(View view) {
        Log.v(LOG_TAG, "onClick: " + view);
        switch (view.getId()) {
            case R.id.labelPhoneChooseAlarm:
                Intent pickSongIntent = new Intent(Intent.ACTION_GET_CONTENT);
                pickSongIntent.setType("audio/*");
                pickSongIntent.addCategory(Intent.CATEGORY_OPENABLE);

                startActivityForResult(pickSongIntent, REQUEST_CODE);
                break;
        }
    }

    private void initializeUIValues(String peripheralAddress) {

        peripheralSettings =
                ItemTrackerPersistenceHelper.getInstance(getApplicationContext()).getPeripheralSettings(peripheralAddress);

        peripheralNameEditText.setText(peripheralSettings.getPeripheralName());
        String iconText = convertImageUriToUserViewableString(peripheralSettings.getPeripheralImageUri());
        peripheralIconTextView.setText(iconText);

        peripheralAlertDurationSeekBar.setProgress(peripheralSettings.getPeripheralAlertDuration());
        peripheralAlertDurationValueText.setText(getLabelForPeripheralAlertDuration(peripheralSettings.getPeripheralAlertDuration()));

        phoneAudioAlertToggleButton.setChecked(peripheralSettings.isPhoneAudibleAlertOn());
        phoneVibrateAlertToggleButton.setChecked(peripheralSettings.isPhoneVibrateAlertOn());

        phoneAlertVolumeSeekBar.setProgress(peripheralSettings.getPhoneAudibleAlertVolume());

        phoneAlertDurationSeekBar.setProgress(peripheralSettings.getPhoneAlertDurationSeconds());
        phoneAlertDurationValueText.setText(getLabelForPhoneAlertDuration(peripheralSettings.getPhoneAlertDurationSeconds()));
    }

    private String convertImageUriToUserViewableString (Uri deviceImageUri) {
        String result = getResources().getString(R.string.customIcon);
        if (deviceImageUri != null) {
            if (deviceImageUri.getLastPathSegment().equals("" + R.drawable.item_icon_camera)) {
                result = getResources().getString(R.string.cameraBag);
            } else if (deviceImageUri.getLastPathSegment().equals("" + R.drawable.item_icon_car_key_fob)) {
                result = getResources().getString(R.string.keys);
            } else if (deviceImageUri.getLastPathSegment().equals("" + R.drawable.item_icon_briefcase)) {
                result = getResources().getString(R.string.briefcase);
            } else if (deviceImageUri.getLastPathSegment().equals("" + R.drawable.item_icon_wallet)) {
                result = getResources().getString(R.string.wallet);
            } else if (deviceImageUri.getLastPathSegment().equals("" + R.drawable.item_icon_purse)) {
                result = getResources().getString(R.string.purse);
            }
        }

        return result;
    }

    private String getLabelForPeripheralAlertDuration(int duration) {
        String durationLabel = "";

        switch (duration) {
            case 0:
                durationLabel = getResources().getString(R.string.durationNone);
                break;
            case 1:
                durationLabel = getResources().getString(R.string.durationThreeRings);
                break;
            case 2:
                durationLabel = getResources().getString(R.string.durationContinuous);
                break;
        }

        return durationLabel;
    }

    private String getLabelForPhoneAlertDuration(int duration) {
        return duration + " " + getResources().getString(R.string.seconds_lowercase);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveSettings();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(LOG_TAG, "resultCode: " + resultCode + ", intent/data: " + data);
        //we have a resulting audio
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            //remember the chosen URI for the saving when activity pauses
            peripheralSettings.setPhoneAudibleAlertUri(data.getData());
            saveSettings();
        }
        //do nothing if the user has cancelled
    }

    private void saveSettings() {
        PeripheralSettings peripheralSettings = getPeripheralSettings();
        ItemTrackerPersistenceHelper.getInstance(getApplicationContext()).persistPeripheralSettings(peripheralSettings);
        peripheralSettings.clearDirtyFlags();
    }

    private PeripheralSettings getPeripheralSettings() {

        peripheralSettings.setPeripheralName(peripheralNameEditText.getText().toString());
        peripheralSettings.setPeripheralAlertDuration(peripheralAlertDurationSeekBar.getProgress());
        peripheralSettings.setPhoneAudibleAlertOn(phoneAudioAlertToggleButton.isChecked());
        peripheralSettings.setPhoneAudibleAlertVolume(phoneAlertVolumeSeekBar.getProgress());
        peripheralSettings.setPhoneVibrateAlertOn(phoneVibrateAlertToggleButton.isChecked());
        peripheralSettings.setPhoneAlertDurationSeconds(phoneAlertDurationSeekBar.getProgress());

        return peripheralSettings;
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

        public void onServiceConnected(ComponentName componentName, IBinder iBinder)
        {
            Log.e(LOG_TAG, "service Connected");
            phBleService = new Messenger(iBinder);
            isBound = true;
        }

        public void onServiceDisconnected(ComponentName componentName) {
            phBleService = null;
            isBound = false;
        }
    }
}