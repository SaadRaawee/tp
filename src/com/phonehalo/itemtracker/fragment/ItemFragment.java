package com.phonehalo.itemtracker.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.phonehalo.itemtracker.Constant;
import com.phonehalo.itemtracker.R;
import com.phonehalo.itemtracker.activity.ChooseIconActivity;
import com.phonehalo.itemtracker.activity.MapView;
import com.phonehalo.itemtracker.db.PeripheralSettings;
import com.phonehalo.itemtracker.helper.IntentHelper;
import com.phonehalo.itemtracker.helper.ItemTrackerServiceHelper;
import com.phonehalo.itemtracker.utility.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;

public class ItemFragment extends Fragment {

    public static final int DISCONNECTED = 0;
    public static final int CONNECTING = 1;
    public static final int CONNECTED = 2;
    private static final String CONNECTION_STATUS = "com.phonehalo.itemtracker.connectionstatus";

    private static final String LOG_TAG = "ItemFragment";

    NavPagerInterface mCallBack;

    //bundle keys for saving/restoring state
    private static final String STATE_KEY_PERIPHERAL = "state_key_peripheral";


    private View itemFragmentView;
    private BluetoothDevice peripheral;
    private short rssiDecibels;
    private int batteryPercent;
    private ItemTrackerServiceHelper itemTrackerServiceHelper;
    private int connectionState;
    private boolean alarmOn;
    private View itemPageTray;
    private GestureDetector disableAudioAlertsGestureDetector;
    private PeripheralSettings peripheralSettings;

    private TextView rssiTextView;
    private ImageView rssiImageView;
    private ImageView ringButtonView;
    private AnimationDrawable ringButtonAnimation;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreateView");
        disableAudioAlertsGestureDetector = new GestureDetector(new DisableAudioAlertsGestureDetector());

        // The last two arguments ensure LayoutParams are inflated properly.
        return inflater.inflate(R.layout.item_view_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //restore the peripheral from the bundle
        if (peripheral == null) {
            Log.v(LOG_TAG, "peripheral restored from saved state");
            peripheral = savedInstanceState.getParcelable(STATE_KEY_PERIPHERAL);
        }

        itemFragmentView = view;

        //add double tap listener
        View iconInSite = itemFragmentView.findViewById(R.id.inSiteLogo);
        itemPageTray = itemFragmentView.findViewById(R.id.itemPageTray);
        DisableAudioAlertsOnTouchListener disableAudioAlertsOnTouchListener = new DisableAudioAlertsOnTouchListener();
        iconInSite.setOnTouchListener(disableAudioAlertsOnTouchListener);
        itemPageTray.setOnTouchListener(disableAudioAlertsOnTouchListener);

        rssiTextView = (TextView) itemFragmentView.findViewById(R.id.peripheralRssiText);
        rssiImageView = (ImageView) itemFragmentView.findViewById(R.id.peripheralRssiImage);
        ringButtonView = (ImageView) itemFragmentView.findViewById(R.id.buttonAlarm);

        //set the listener for the name EditText field
        final EditText peripheralNameEditText = (EditText) view.findViewById(R.id.peripheralName);
        peripheralNameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                boolean handled = false;
                switch (actionId) {
                    case EditorInfo.IME_ACTION_DONE:
                        peripheralNameEditText.setCursorVisible(false);
                        PeripheralSettings peripheralSettings = new PeripheralSettings();
                        peripheralSettings.setPeripheralAddress(peripheral.getAddress());
                        peripheralSettings.setPeripheralName(textView.getText().toString());
                        itemTrackerServiceHelper.persistPeripheralSettings(peripheralSettings);
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
//        if(peripheral != null && peripheral.isLEDeviceConnected())
//        {
//            setConnectionState(CONNECTED);
//        }
//        else
//        {
        if(peripheral != null)
        {
            try{

//            if(peripheralSettings.isDeviceConnectionState())
//            {
//            setConnectionState(CONNECTED);
//            }
//            else
//            {
            setConnectionState(CONNECTING);
        //    }
            }catch (NullPointerException e)
            {
                e.printStackTrace();
            }
        }
 //       }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        refresh();
    }

    private void refresh() {
        //refresh peripheral settings
        //for example, the icon chooser activity might have changed it
        try
        {
        peripheralSettings = itemTrackerServiceHelper.getPeripheralSettings(peripheral.getAddress());
        }catch (NullPointerException e)
        {
            e.printStackTrace();
        }

        if(peripheralSettings != null)
        {
          //set the peripheral name
          final TextView peripheralNameTextView = (TextView) itemFragmentView.findViewById(R.id.peripheralName);
          peripheralNameTextView.setText(peripheralSettings.getPeripheralName());

         //set the peripheral icon
          final ImageView peripheralIconImageView = (ImageView) itemFragmentView.findViewById(R.id.itemImage);
          Uri drawableUri = peripheralSettings.getPeripheralImageUri();
          Drawable drawable = getDrawable(drawableUri);
          peripheralIconImageView.setImageDrawable(drawable);
          Log.v(LOG_TAG, "Peripheral Connection state is  " + peripheralSettings.isDeviceConnectionState());
            if(peripheralSettings.isDeviceConnectionState())
            {
                connectionState = CONNECTED;
                Log.v(LOG_TAG, "Connected with rssi = " + rssiDecibels);
                setRssiDecibels((short)-1);
            }
            else
            {
                connectionState = DISCONNECTED;
                setRssiDecibels((short)0);
            }
        }

        //set battery and rssi
        setBatteryPercent(batteryPercent);

        itemFragmentView.invalidate();

        //request battery update
        //BleServiceHelper.getInstance().sendMessage(BleConstant.MESSAGE_READ_BATTERY, peripheral);
    }

    /**
     * @return the drawable for the peripheral (can be null)
     */
    private Drawable getDrawable(Uri imageUri) {
        Drawable drawable = null;
        Log.v(LOG_TAG, "getDrawable: " + imageUri);

        if (imageUri == null) {
            //set a default image
            imageUri = Uri.parse(ChooseIconActivity.RESOURCE_URI_PREFIX + R.drawable.icon);
        }

        BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
        Matrix matrix = new Matrix();

        //rotate and down-sample, if the URI is not a known resource
        Log.v(LOG_TAG, "image URI: " + imageUri);
        if (
                !imageUri.getLastPathSegment().equals("" + R.drawable.item_icon_purse) &&
                !imageUri.getLastPathSegment().equals("" + R.drawable.item_icon_wallet) &&
                !imageUri.getLastPathSegment().equals("" + R.drawable.item_icon_briefcase) &&
                !imageUri.getLastPathSegment().equals("" + R.drawable.item_icon_car_key_fob) &&
                !imageUri.getLastPathSegment().equals("" + R.drawable.item_icon_camera) &&
                !imageUri.getLastPathSegment().equals("" + R.drawable.icon)
                ) {
            bitmapFactoryOptions.inSampleSize = 20; // sample at 1/20th the size!  saves lots of memory

            //rotate 90 deg... for some reason, the image comes back with landscape layout when taken as portrait
            // ... for example, the image will be previewed as portrait in the camera and gallery,
            // but will appear in this app as landscape by default
            matrix.postRotate(90);
        }

        try {
            Context context = getActivity().getApplicationContext();
            InputStream stream = context.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(stream, null, bitmapFactoryOptions);
            stream.close();

            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            drawable = new BitmapDrawable(context.getResources(), bitmap);
        } catch (FileNotFoundException e) {
            //leave drawable as null, it will be handled by caller
            e.printStackTrace();
        } catch (IOException e) {
            //leave drawable as null, it will be handled by caller
            e.printStackTrace();
        } catch (NullPointerException e) {
            //uri was null, that is OK
        }

        return drawable;
    }

    public void setItemTrackerServiceHelper(ItemTrackerServiceHelper itemTrackerServiceHelper) {
        this.itemTrackerServiceHelper = itemTrackerServiceHelper;
    }



    public void setPeripheral(final BluetoothDevice peripheral) {
        Log.v(LOG_TAG, "setPeripheral");
        if((peripheral != null) && (itemTrackerServiceHelper != null))
        {
        this.peripheral = peripheral;
        try{
        this.peripheralSettings = itemTrackerServiceHelper.getPeripheralSettings(peripheral.getAddress());
        }catch (NullPointerException e)
        {
            PeripheralSettings mperipheralSettings = new PeripheralSettings();
            mperipheralSettings.setPeripheralAddress(peripheral.getAddress());
            mperipheralSettings.setPeripheralName("phonehalo");
            try{
            itemTrackerServiceHelper.persistPeripheralSettings(mperipheralSettings);
            peripheralSettings = itemTrackerServiceHelper.getPeripheralSettings(peripheral.getAddress());
            }
            catch (NullPointerException e2)
            {
                e2.printStackTrace();
            }
        }
        }
    }

    public BluetoothDevice getPeripheral() {
        return peripheral;
    }

    public void setRssiDecibels(short rssi) {
        Log.v(LOG_TAG, "setRssiDecibels: " + rssi);
        short lastRssi = this.rssiDecibels;
        this.rssiDecibels = rssi;

        //this if/else could be a big compound OR statement, since the resulting code is all the same;
        //we're determining if we update or not.  Update requires two consecutive rssi updates in the same zone
        if (rssi < Constant.RSSI_MAX_FAR && lastRssi < Constant.RSSI_MAX_FAR) {
            updateRssiViewWithDecibels(rssi);
        } else if (rssi < Constant.RSSI_MAX_MEDIUM && lastRssi < Constant.RSSI_MAX_MEDIUM && lastRssi > Constant.RSSI_MAX_FAR ) {
            updateRssiViewWithDecibels(rssi);
        } else if (rssi > Constant.RSSI_MAX_MEDIUM && lastRssi > Constant.RSSI_MAX_MEDIUM) {
            updateRssiViewWithDecibels(rssi);
        } else if (rssi == 0 && lastRssi == 0) {
            updateRssiViewWithDecibels(rssi);
        }

    }

    private void updateRssiViewWithDecibels(short rssi) {

        Log.v(LOG_TAG, "updateRssiViewWithDecibels: " + rssi);
        if (itemFragmentView != null) {

            //set default resources
            int rssiStatusTextResource = R.string.disconnected;
            int rssiStatusImageResource = R.drawable.proximity_01;

            switch (connectionState) {
                case CONNECTED:
                    peripheralSettings.setDeviceConnectionState(true);
                    itemTrackerServiceHelper.persistPeripheralSettings(peripheralSettings);
                    rssiStatusTextResource = R.string.rssiDistanceFar;
                    rssiStatusImageResource = R.drawable.proximity_02;
                    if (rssi > Constant.RSSI_MAX_FAR) {
                        rssiStatusTextResource = R.string.rssiDistanceMedium;
                        rssiStatusImageResource = R.drawable.proximity_03;
                    } if (rssi > Constant.RSSI_MAX_MEDIUM) {
                        rssiStatusTextResource = R.string.rssiDistanceNear;
                        rssiStatusImageResource = R.drawable.proximity_04;
                    } if (rssi == 0) {
                        rssiStatusTextResource = R.string.connected;
                        rssiStatusImageResource = R.drawable.proximity_04;
                    }
                    break;
                case DISCONNECTED:
                    if(rssi != 0)
                    {
                        rssiStatusTextResource = R.string.rssiDistanceFar;
                        rssiStatusImageResource = R.drawable.proximity_02;
                        if (rssi > Constant.RSSI_MAX_FAR) {
                            rssiStatusTextResource = R.string.rssiDistanceMedium;
                            rssiStatusImageResource = R.drawable.proximity_03;
                        } if (rssi > Constant.RSSI_MAX_MEDIUM) {
                        rssiStatusTextResource = R.string.rssiDistanceNear;
                        rssiStatusImageResource = R.drawable.proximity_04;
                        }
                        connectionState = CONNECTED;

                    }
                    else
                    {
                        peripheralSettings.setDeviceConnectionState(false);
                        itemTrackerServiceHelper.persistPeripheralSettings(peripheralSettings);
                        rssiStatusTextResource = R.string.disconnected;
                        rssiStatusImageResource = R.drawable.proximity_01;
                    }
                    break;
                case CONNECTING:
                    rssiStatusTextResource = R.string.connecting;
                    rssiStatusImageResource = R.drawable.proximity_01;

                    if(rssi< Constant.RSSI_MAX_FAR)
                    {
                        rssiStatusTextResource = R.string.connected;
                        rssiStatusImageResource = R.drawable.proximity_03;
                        connectionState = CONNECTED;
                    }

                    if (rssi > Constant.RSSI_MAX_FAR) {
                        rssiStatusTextResource = R.string.connected;
                        rssiStatusImageResource = R.drawable.proximity_03;
                        connectionState = CONNECTED;
                    } if (rssi > Constant.RSSI_MAX_MEDIUM) {
                    rssiStatusTextResource = R.string.connected;
                    rssiStatusImageResource = R.drawable.proximity_03;
                    connectionState = CONNECTED;
                } if (rssi == 0) {
                    rssiStatusTextResource = R.string.connecting;
                    rssiStatusImageResource = R.drawable.proximity_01;
                }
                    break;
//                    rssiStatusTextResource = R.string.connecting;
//                    rssiStatusImageResource = R.drawable.links_blue_broken_02;
//                    break;
            }


            final int finalRssiStatusTextResource = rssiStatusTextResource;
            final int finalRssiStatusImageResource = rssiStatusImageResource;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try
                    {
                        Log.v(LOG_TAG, "Setting Image");
                    rssiTextView.setText(getText(finalRssiStatusTextResource));
                    rssiImageView.setImageResource(finalRssiStatusImageResource);
                    }catch (NullPointerException e)
                    {
                        e.printStackTrace();
                    }
                }
            });
           // rssiTextView.setText(getResources().getText(rssiStatusTextResource));

        }
    }

    public void setBatteryPercent(int batteryPercent) {
        try {
            this.batteryPercent = batteryPercent;
            if (itemFragmentView != null) {
                TextView batteryLevelTextView = (TextView) itemFragmentView.findViewById(R.id.peripheralBatteryLevelText);
                ImageView batteryLevelImageView = (ImageView) itemFragmentView.findViewById(R.id.peripheralBatteryLevelImage);
                String batteryPercentText = MessageFormat.format(getResources().getString(R.string.batteryPercentFormat),
                        batteryPercent, getResources().getString(R.string.percentSign));
//                if (batteryPercent < 0) {
//                    //default to full battery display
//                        batteryPercentText = MessageFormat.format(getResources().getString(R.string.batteryPercentFormat),
//                                100, getResources().getString(R.string.percentSign));
//                        batteryLevelTextView.setText(batteryPercent + getResources().getString(R.string.percentSign));
//
//                }
                batteryLevelTextView.setText(batteryPercentText);
                batteryLevelImageView.setImageResource(R.drawable.links_grn_together);
            }
        } catch (IllegalStateException e) {
            //if fragment gets updated as activity is destroyed, trying to fetch a resource can cause this exception
            //In this case we don't care, since we will be destroyed soon
            Log.v(LOG_TAG, "Error updating during app shutdown.  Not a problem.");
        }
    }

    public void setConnectionState(int connectionState) {
        Log.v(LOG_TAG, "Connection State: " + connectionState);
        if (this.connectionState != connectionState) {
            this.connectionState = connectionState;
            updateRssiViewWithDecibels((short)0);
        }
    }

    public void onClick(View view) {
        Log.v(LOG_TAG, "onClick: " + view.getId());
        switch (view.getId()) {
            case R.id.itemImage:
                //start ChooseIconActivity with appropriate EXTRAs
                Intent chooseIconIntent = new Intent(getActivity().getApplicationContext(), ChooseIconActivity.class);

                chooseIconIntent.putExtra(IntentHelper.EXTRA_DEVICE_ADDRESS, peripheralSettings.getPeripheralAddress());
                chooseIconIntent.putExtra(IntentHelper.EXTRA_DEVICE_NAME, peripheralSettings.getPeripheralName());

                //update image intent
                chooseIconIntent.setAction(IntentHelper.ACTION_UPDATE_CHOICE);
                startActivity(chooseIconIntent);

                break;
            case R.id.buttonAlarm:
                alarmOn = !alarmOn;
                if (alarmOn) {
                    mCallBack.ringDevice(peripheral);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run()
                        {

                            ringButtonView.setImageResource(R.drawable.ring_button_animation);
                            ringButtonAnimation = (AnimationDrawable)ringButtonView.getDrawable();
                            ringButtonAnimation.start();

                        }
                    });
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ringButtonAnimation.stop();
                            ringButtonView.setImageResource(R.drawable.button_speaker_waves_3);
                        }
                    });
                    mCallBack.silenceDevice(peripheral);
                }
                break;
            case R.id.buttonLocate:
                //start the map activity with the last known location
               // Location location = itemTrackerServiceHelper.getLastKnownLocationForPeripheral(peripheral.getAddress());
//                ItemTrackerPersistenceHelper helper = ItemTrackerPersistenceHelper.getInstance(getActivity().getApplicationContext());
//                Location location = helper.getMostRecentLocation(peripheral.getAddress());

                Intent mapIntent = new Intent(getActivity().getApplicationContext(), MapView.class);
                mapIntent.putExtra(BluetoothDevice.EXTRA_DEVICE, peripheral);
                Log.v(LOG_TAG, "connection state is " + this.connectionState);
                if(this.connectionState == 2)
                {
                    peripheralSettings.setDeviceConnectionState(true);
                    itemTrackerServiceHelper.persistPeripheralSettings(peripheralSettings);
                }
                else
                {
                    peripheralSettings.setDeviceConnectionState(false);
                    itemTrackerServiceHelper.persistPeripheralSettings(peripheralSettings);
                }
                mapIntent.putExtra(CONNECTION_STATUS, this.connectionState);
                startActivity(mapIntent);
                break;
            default:
                Log.v(LOG_TAG, "Unrecognized click");
                break;

            //test button handling for dev/test use
            case R.id.buttonTestPhoneAlert:
                itemTrackerServiceHelper.alertPhoneOnPeripheralDisconnect(peripheral.getAddress());
                break;
        }
    }

    private class DisableAudioAlertsGestureDetector extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {

            View iconAudioAlertsDisabled = itemFragmentView.findViewById(R.id.iconAudioAlertsDisabled);
            boolean muted = peripheralSettings.isPhoneAudibleAlertMuted();
            if (muted) {
                iconAudioAlertsDisabled.setVisibility(View.VISIBLE);
                itemPageTray.setBackgroundResource(R.drawable.rectangle_rounded_right_red_transparent);
            } else {
                iconAudioAlertsDisabled.setVisibility(View.GONE);
                itemPageTray.setBackgroundColor(Color.TRANSPARENT);
            }

            peripheralSettings.setPhoneAudibleAlertMuted(!muted);
            itemTrackerServiceHelper.persistPeripheralSettings(peripheralSettings);
            peripheralSettings.clearDirtyFlags();

            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }
    }

    private class DisableAudioAlertsOnTouchListener implements View.OnTouchListener {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                disableAudioAlertsGestureDetector.onTouchEvent(event);
                return false;
            }
    }

    @Override
    public void onDetach() {
        super.onDetach();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void onAttach(Activity activity) {
        Log.e(LOG_TAG, "activity is " + activity.getLocalClassName());
        super.onAttach(activity);    //To change body of overridden methods use File | Settings | File Templates.
        try {
            mCallBack = (NavPagerInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }
}
