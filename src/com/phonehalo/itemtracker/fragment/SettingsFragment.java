package com.phonehalo.itemtracker.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.phonehalo.itemtracker.R;
import com.phonehalo.itemtracker.activity.AddItemActivity;
import com.phonehalo.itemtracker.activity.ItemSettingsActivity;
import com.phonehalo.itemtracker.activity.NavigationActivity;
import com.phonehalo.itemtracker.activity.TwitterAuthActivity;
import com.phonehalo.itemtracker.db.ExternalAlertSettings;
import com.phonehalo.itemtracker.db.ItemTrackerPersistenceHelper;
import com.phonehalo.itemtracker.db.PeripheralSettings;
import com.phonehalo.itemtracker.helper.IntentHelper;
import com.phonehalo.itemtracker.helper.ItemTrackerServiceHelper;
import com.phonehalo.itemtracker.utility.Log;

import java.util.ArrayList;
import java.util.Set;

public class SettingsFragment extends Fragment {
    private static final String LOG_TAG = "SettingsFragment";
    private ItemTrackerServiceHelper itemTrackerServiceHelper;
    private ViewGroup container;
    private boolean isEditMode;
    private ArrayList<View> peripheralDeleteViews;
    private ArrayList<View> peripheralDeleteAndSettingsViews;
    private NavigationActivity navigationActivity;
    private ExternalAlertSettings externalAlertSettings;
//    private EditText emailAddressEditText;
//    private EditText emailCCAddressEditText;
//    private ToggleButton emailAlertToggleButton;
//    private ToggleButton facebookAlertToggleButton;
//    private ToggleButton twitterAlertToggleButton;
    private Button scanDeviceButton;
    private ArrayList<BluetoothDevice> peripherals;

    //this should be set once the helper is actually connected to the service
    public void setItemTrackerServiceHelper(ItemTrackerServiceHelper itemTrackerServiceHelper) {
        this.itemTrackerServiceHelper = itemTrackerServiceHelper;
        refresh();
    }


    public void setNavigationActivity(NavigationActivity navigationActivity) {
        this.navigationActivity = navigationActivity;
    }

    public void setExternalAlertSettings(ExternalAlertSettings externalAlertSettings) {
        this.externalAlertSettings = externalAlertSettings;

//        emailAddressEditText.setText(externalAlertSettings.getEmailAddress());
//        emailCCAddressEditText.setText(externalAlertSettings.getEmailCCAddress());
//        emailAlertToggleButton.setChecked(externalAlertSettings.isEmailAlertOn());
//        facebookAlertToggleButton.setChecked(externalAlertSettings.isFacebookAlertOn());
//        twitterAlertToggleButton.setChecked(externalAlertSettings.isTwitterAlertOn());
    }

    public void setPeripherals(ArrayList<BluetoothDevice> peripherals) {
        Log.v(LOG_TAG, "setPeripherals: " + peripherals);
        this.peripherals = peripherals;
        refresh();
    }

    @Override
    public void onPause() {
        Log.v(LOG_TAG, "onPause");
        super.onPause();
        saveSettings();
    }

    public void saveSettings() {
        if (externalAlertSettings != null) {
            externalAlertSettings.setEmailAddress("");
            externalAlertSettings.setEmailCCAddress("");
            externalAlertSettings.setEmailAlertOn(false);
            externalAlertSettings.setFacebookAlertOn(false);
            externalAlertSettings.setTwitterAlertOn(false);

            Log.v(LOG_TAG, "saveSettings: " + externalAlertSettings);
            itemTrackerServiceHelper.persistExternalAlertSettings(externalAlertSettings);
        }

    }

    public void refresh() {
        Log.v(LOG_TAG, "refresh");
        removePeripheralList();

        peripherals.clear();
        try{
        ItemTrackerPersistenceHelper helper = ItemTrackerPersistenceHelper.getInstance(getActivity().getApplicationContext());




        Set<BluetoothDevice> deviceSet;
        if(Build.VERSION.SDK_INT <= 17)
        {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            deviceSet = adapter.getBondedDevices();
        }
        else
        {
            //   BluetoothManager
            BluetoothManager manager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
            deviceSet = manager.getAdapter().getBondedDevices();
        }
        for (BluetoothDevice device : deviceSet) {
            if (helper.isPeripheralKnown(device.getAddress())) {
                peripherals.add(device);
            }
        }
        if(peripherals.size()>0)
        {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    scanDeviceButton.setVisibility(View.GONE);
                }
            });
        }
        else
        {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    scanDeviceButton.setVisibility(View.VISIBLE);
                }
            });
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                layoutPeripheralList(peripherals);
            }
        });
        if (peripherals.size() == 0) {
            if (isEditMode) {
                Button editButton = (Button) getActivity().findViewById(R.id.settingsEditButton);
                editButton.setBackgroundResource(R.drawable.ic_device_disconnect);
                editButton.setText("Edit");
                toggleEditMode();
            }
        }

        if (itemTrackerServiceHelper != null) {
            setExternalAlertSettings(ItemTrackerPersistenceHelper.getInstance(getActivity().getApplicationContext()).getExternalAlertSettings());
        }
        ViewGroup linearLayoutSettingsView = (ViewGroup) container.findViewById(R.id.linearLayoutSettings);
        linearLayoutSettingsView.invalidate();
        Log.v(LOG_TAG, "device list child count after invalidate: " + linearLayoutSettingsView.getChildCount());
        }catch (NullPointerException e)
        {
            e.printStackTrace();
        }
    }

    private void removePeripheralList() {
        Log.v(LOG_TAG, "removePeripheralList");
        ViewGroup linearLayoutSettingsView = (ViewGroup) container.findViewById(R.id.linearLayoutSettings);
        for (View view : peripheralDeleteAndSettingsViews) {
            linearLayoutSettingsView.removeView(view);
        }
    }

    private void layoutPeripheralList(ArrayList<BluetoothDevice> peripherals) {
        if (peripherals == null || peripherals.size() == 0) {
            return;
        }

        Log.v(LOG_TAG, "layoutPeripheralList: " + peripherals);
        //add buttons to control the existing peripherals

        ViewGroup linearLayoutSettingsView = (ViewGroup) container.findViewById(R.id.linearLayoutSettings);

        LinearLayout.LayoutParams linearLayoutParamsSettingsButton = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayoutParamsSettingsButton.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
        linearLayoutParamsSettingsButton.setMargins(10, 1, 10, 1);

        LinearLayout.LayoutParams linearLayoutParamsDeleteButton = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayoutParamsDeleteButton.gravity = Gravity.CENTER_VERTICAL;
        linearLayoutParamsDeleteButton.setMargins(1, 1, 1, 1);

        for (int i = 0; i < peripherals.size(); i++) {
            Drawable buttonBackground = getResources().getDrawable(R.drawable.rectangle_blue);
            if (peripherals.size() == 1) {
                //only button
          //      buttonBackground = getResources().getDrawable(R.drawable.rectangle_rounded_white);
            } else if (i == 0) {
                //first button
           //     buttonBackground = getResources().getDrawable(R.drawable.rectangle_rounded_top_white);
            } else if (i == peripherals.size() - 1) {
                //last button
           //     buttonBackground = getResources().getDrawable(R.drawable.rectangle_rounded_bottom_white);
            } else {
                //middle button
           //     buttonBackground = getResources().getDrawable(R.drawable.rectangle_white);
            }
            PeripheralSettings peripheralSettings = ItemTrackerPersistenceHelper.getInstance(getActivity().getApplicationContext()).
                    getPeripheralSettings(peripherals.get(i).getAddress());

            //create new linear layout sublayout to hold delete/peripheral buttons
            LinearLayout deleteAndSettingsViewGroupForPeripheral = new LinearLayout(getActivity().getApplicationContext());
            deleteAndSettingsViewGroupForPeripheral.setLayoutParams(linearLayoutParamsSettingsButton);

            Button peripheralSettingsButton = new Button(getActivity().getApplicationContext(),null, R.style.ButtonLightStyle);
            peripheralSettingsButton.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            peripheralSettingsButton.setOnClickListener(new PeripheralClickListener(peripherals.get(i), false));
           // peripheralSettingsButton.setLayoutParams(linearLayoutParamsSettingsButton);
            peripheralSettingsButton.setText(peripheralSettings.getPeripheralName());
            peripheralSettingsButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
            peripheralSettingsButton.setTextColor(Color.WHITE);
            peripheralSettingsButton.setTextSize(20);
            peripheralSettingsButton.setBackgroundDrawable(buttonBackground);
            peripheralSettingsButton.setPadding(30, 10, 30, 10);
            //peripheralSettingsButton.setAlpha((float) 0.5);
            //peripheralSettingsButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.dot_grey, 0);

            ImageView deletePeripheralView = new ImageView(getActivity().getApplicationContext());
            deletePeripheralView.setOnClickListener(new PeripheralClickListener(peripherals.get(i), true));
            deletePeripheralView.setLayoutParams(linearLayoutParamsDeleteButton);
            //     deletePeripheralView.setBackgroundColor(Color.RED);
            deletePeripheralView.setImageResource(R.drawable.x_red_square);
            deletePeripheralView.setAdjustViewBounds(true);
            deletePeripheralView.setMaxHeight(100);
            deletePeripheralView.setMaxWidth(100);
            deletePeripheralView.setMinimumHeight(70);
            deletePeripheralView.setMinimumWidth(70);
            //deletePeripheralView.setPadding(20, -10, 20, -10);
            if (isEditMode) {
                deletePeripheralView.setVisibility(View.VISIBLE);
            } else {
                deletePeripheralView.setVisibility(View.GONE);
            }

            peripheralDeleteViews.add(deletePeripheralView);

            linearLayoutSettingsView.addView(deleteAndSettingsViewGroupForPeripheral, 1 + i);
            deleteAndSettingsViewGroupForPeripheral.addView(deletePeripheralView);
            deleteAndSettingsViewGroupForPeripheral.addView(peripheralSettingsButton);
            peripheralDeleteAndSettingsViews.add(deleteAndSettingsViewGroupForPeripheral);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreateView");
        this.container = container;
        this.peripheralDeleteViews = new ArrayList<View>();
        this.peripheralDeleteAndSettingsViews = new ArrayList<View>();
        this.peripherals = new ArrayList<BluetoothDevice>();

        View settingsView = inflater.inflate(R.layout.app_settings_view_layout, container, false);


//        emailAddressEditText = (EditText) settingsView.findViewById(R.id.valueEmailAddress);
//        emailCCAddressEditText = (EditText) settingsView.findViewById(R.id.valueEmailCCAddress);
//        emailAlertToggleButton = (ToggleButton) settingsView.findViewById(R.id.toggleEmailAlertOnOff);
//        facebookAlertToggleButton = (ToggleButton) settingsView.findViewById(R.id.toggleFacebookAlertOnOff);
//        twitterAlertToggleButton = (ToggleButton) settingsView.findViewById(R.id.toggleTwitterAlertOnOff);
        scanDeviceButton = (Button) settingsView.findViewById(R.id.buttonDiscoverDevices);
        initializeAppSettingsEditTextListeners();

        ItemTrackerPersistenceHelper helper = ItemTrackerPersistenceHelper.getInstance(getActivity().getApplicationContext());
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> deviceSet = adapter.getBondedDevices();
        for (BluetoothDevice device : deviceSet) {
            if (helper.isPeripheralKnown(device.getAddress())) {
                peripherals.add(device);
            }
        }

        return settingsView;
    }

    private void toggleEditMode() {
        //change the peripheral buttons to allow deletion
        Log.v(LOG_TAG, "toggleEditMode()");
        if (isEditMode) {
            showDeleteButtons(false);
        } else {
            showDeleteButtons(true);
        }
        isEditMode = !isEditMode;
    }

    private void showDeleteButtons(boolean showFlag) {
        int visibility = showFlag ? View.VISIBLE : View.GONE;
        for (View view : peripheralDeleteViews) {
            view.setVisibility(visibility);
        }
    }

    public void onClick(View view) {
        Log.v(LOG_TAG, "onClick: " + view);
        switch (view.getId()) {
            case R.id.buttonDiscoverDevices:
                startDiscoveringDevices();
                break;
            case R.id.settingsEditButton:
                Log.v(LOG_TAG, "settingsEditButton");
                if (peripherals.size() > 0) {
                    if (isEditMode) {
                        view.setBackgroundResource(R.drawable.ic_device_disconnect);
                        Button button = (Button) view;
                        button.setText("Edit");
                    } else {
                        view.setBackgroundResource(R.drawable.ic_device_connect);
                        Button button = (Button) view;
                        button.setText("Done");
                    }
                    toggleEditMode();
                }
                break;
//            case R.id.toggleTwitterAlertOnOff:
//                ToggleButton toggleButton = (ToggleButton) view;
//                if (toggleButton.isChecked()) {
//                    checkTwitterAuth();
//                }
//                break;
//            case R.id.toggleFacebookAlertOnOff:
//                ToggleButton toggleFacebookButton = (ToggleButton) view;
//                if (toggleFacebookButton.isChecked()) {
//                    checkFacebookAuth();
//                }
//                break;
//            case R.id.labelAboutAudiovox:
//                viewUrlResource(R.string.urlAboutAudioVox);
//                break;
            case R.id.labelAboutPhoneHalo:
                viewUrlResource(R.string.urlAboutPhoneHalo);
                break;
            case R.id.labelFAQ:
                viewUrlResource(R.string.urlFAQ);
                break;
        }
    }

    private void checkTwitterAuth() {
        //maybe in the future, we could have a design that keeps the credentials until explicitly removed by user
        Intent intent = new Intent(getActivity().getApplicationContext(), TwitterAuthActivity.class);
        startActivity(intent);
    }

    private void checkFacebookAuth() {
        Session.openActiveSession(getActivity(), true, new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                if (session.isOpened()) {

                    // make request to the /me API
                    Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {

                        // callback after Graph API response with user object
                        @Override
                        public void onCompleted(GraphUser user, Response response) {
                            if (user != null) {
                                Log.e(LOG_TAG, "w00t" + user.getFirstName());
                            }
                        }
                    });
                }
            }
        });
    }


    private void initializeAppSettingsEditTextListeners() {

//        emailAddressEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                boolean handled = false;
//                switch (actionId) {
//                    case EditorInfo.IME_ACTION_NEXT:
//                        emailAddressEditText.setCursorVisible(false);
//                        //allow "handled" to remain false, so the default behavior of the event will also trigger
//                        //this will close the keyboard automatically
//                        break;
//                    default:
//                        Log.e(LOG_TAG, "Unexpected EditText action");
//                        break;
//                }
//                return handled;
//            }
//        });
//
//        emailAddressEditText.setOnTouchListener(new View.OnTouchListener() {
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                emailAddressEditText.setCursorVisible(true);
//                return false; //return not-handled, so we benefit from auto-closing of keyboard behavior... are there other side effects?
//            }
//        });
//
//        emailCCAddressEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                boolean handled = false;
//                switch (actionId) {
//                    case EditorInfo.IME_ACTION_DONE:
//                        emailCCAddressEditText.setCursorVisible(false);
//                        //allow "handled" to remain false, so the default behavior of the event will also trigger
//                        //this will close the keyboard automatically
//                        break;
//                    default:
//                        Log.e(LOG_TAG, "Unexpected EditText action");
//                        break;
//                }
//                return handled;
//            }
//        });
//
//        emailCCAddressEditText.setOnTouchListener(new View.OnTouchListener() {
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                emailCCAddressEditText.setCursorVisible(true);
//                return false; //return not-handled, so we benefit from auto-closing of keyboard behavior... are there other side effects?
//            }
//        });
    }

    private void viewUrlResource(int urlResource) {
        String url = getResources().getString(urlResource);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    public void peripheralConnected(BluetoothDevice peripheral) {
        if (!peripherals.contains(peripheral)) {
            peripherals.add(peripheral);
            refresh();
        }
    }


    private class PeripheralClickListener implements View.OnClickListener {

        private final BluetoothDevice peripheral;
        private final boolean isDeleteListener;

        PeripheralClickListener(BluetoothDevice peripheral, boolean isDeleteListener) {
            this.peripheral = peripheral;
            this.isDeleteListener = isDeleteListener;
        }


        @Override
        public void onClick(View v) {
            if (isDeleteListener) {
                SettingsFragment.this.deletePeripheral(peripheral);
            } else {
                SettingsFragment.this.startPeripheralSettings(peripheral);
            }
        }
    }

    private void startPeripheralSettings(BluetoothDevice peripheral) {
        Log.v(LOG_TAG, "startPeripheralSettings: " + peripheral);

        Intent itemSettingsIntent = new Intent(getActivity().getApplicationContext(), ItemSettingsActivity.class);
        itemSettingsIntent.putExtra(IntentHelper.EXTRA_DEVICE, peripheral);
        startActivity(itemSettingsIntent);
    }

    private void deletePeripheral(BluetoothDevice peripheral) {
        Log.v(LOG_TAG, "deletePeripheral: " + peripheral);
        ItemTrackerPersistenceHelper.getInstance(getActivity().getApplicationContext()).deletePeripheral(peripheral.getAddress());
        navigationActivity.peripheralDeleted(peripheral.getAddress());
        refresh();
    }

    public void startDiscoveringDevices() {
        Log.v(LOG_TAG, "buttonDiscoverDevices");
        //go to home tab after discovery.  Home tab will display the discovered device or the no-connection screen again
        Intent addItemActivityIntent = new Intent(getActivity().getApplicationContext(), AddItemActivity.class);
        startActivity(addItemActivityIntent);
        if (navigationActivity != null) {
            navigationActivity.selectHomeTab();
        }
    }

    //overrides for logging


    @Override
    public void onDetach() {
        super.onDetach();
        Log.v(LOG_TAG, "onDetach of activity");
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.v(LOG_TAG, "onAttach of activity: " + activity);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.v(LOG_TAG, "onDestroyView");
    }
}
