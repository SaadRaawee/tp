package com.phonehalo.itemtracker.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.phonehalo.itemtracker.R;
import com.phonehalo.itemtracker.activity.NavigationActivity;
import com.phonehalo.itemtracker.db.ItemTrackerPersistenceHelper;
import com.phonehalo.itemtracker.helper.ItemTrackerServiceHelper;
import com.phonehalo.itemtracker.utility.Log;

import java.util.ArrayList;
import java.util.Set;

public class ItemHomeFragment extends Fragment implements ViewPager.OnPageChangeListener {

    private static final String LOG_TAG = "ItemHomeFragment";

    NavPagerInterface mCallBack;

    private static final int ARROW_LEFT = 0;
    private static final int ARROW_RIGHT = 1;
    private ViewPager itemPager;
    private ItemFragmentStatePagerAdapter itemPageAdapter;
    private ViewGroup pageControls;
    private int previouslySelectedItem;  //by the time we need to use this value, we will already have a new selected item
    private ItemTrackerServiceHelper itemTrackerServiceHelper;
    private LayoutInflater inflater;
    private NavigationActivity navigationActivity;
    private ImageView noConnectionView;
    private View itemHomeView;

    public void setItemTrackerServiceHelper(ItemTrackerServiceHelper itemTrackerServiceHelper) {
        this.itemTrackerServiceHelper = itemTrackerServiceHelper;

        //if items exist, set the itemTrackerServiceHelper on them
        if (itemPageAdapter != null) {
            Log.v(LOG_TAG, "itemPageAdapter: " + itemPageAdapter);
            Log.v(LOG_TAG, "itemPageAdapter.getCount(): " + itemPageAdapter.getCount());
            for (int i = 1; i < itemPageAdapter.getCount(); i++) {
                ItemFragment itemFragment = (ItemFragment)itemPageAdapter.getItem(i-1);
                itemFragment.setItemTrackerServiceHelper(itemTrackerServiceHelper);
            }
        }
    }

    public void setNavigationActivity(NavigationActivity navigationActivity) {
        this.navigationActivity = navigationActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        itemPageAdapter = new ItemFragmentStatePagerAdapter(getFragmentManager());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreateView");
        this.inflater = inflater;
        itemHomeView = inflater.inflate(R.layout.item_home_view_layout, container, false);

        noConnectionView = (ImageView)itemHomeView.findViewById(R.id.homeViewNoConnection);

        pageControls = (ViewGroup)itemHomeView.findViewById(R.id.homeViewPageControls);

        itemPager = (ViewPager)itemHomeView.findViewById(R.id.itemPager);
        itemPager.setAdapter(itemPageAdapter);
        itemPager.setOnPageChangeListener(this);

        return itemHomeView;
    }

    private void addPages(ArrayList<BluetoothDevice> peripheralList) {
        //adding pages as a list allows the app to avoid flicker as they are reported one at a time on startup with multiple known devices
        if (peripheralList == null) { return; }

        boolean atLeastOneItemAdded = false;

        for (BluetoothDevice peripheral : peripheralList) {

            //add fragment to pager
            boolean itemAdded;
            ItemFragment itemFragment = new ItemFragment();
            itemFragment.setItemTrackerServiceHelper(itemTrackerServiceHelper);
            itemFragment.setPeripheral(peripheral);
//            itemFragment.setBleServiceHelper(bleServiceHelper);

            //item will not be added if it already is known
            itemAdded = itemPageAdapter.addItem(itemFragment);
            atLeastOneItemAdded = atLeastOneItemAdded || itemAdded;

            if (itemAdded) {
                updatePageMarkersWhenItemAdded();
            }
        }

        if (atLeastOneItemAdded) {
            updatePageArrows();
            itemHomeView.invalidate();
            navigationActivity.refresh(NavigationActivity.SETTINGS_TAB);
        }

        //we want to update the home view even if there is an empty list of peripherals, to show the no-connection display
        //this is better because we don't want to see the flicker of the no-connection view while we are waiting to find
        //out if we have known devices
        updateHomeView();
    }

    public void peripheralDeleted(String peripheralAddress) {
        int itemIndex = itemPageAdapter.getItemIndexByPeripheral(peripheralAddress);
        int itemCountBeforeDelete = itemPageAdapter.getCount();
        itemPageAdapter.deletePeripheral(peripheralAddress);

        //in case other bugs allow deletion of a peripheral not shown in the pager, we will guard here
        //root cause is that settings page and main view item display are out of sync
        Log.v(LOG_TAG, "removing view at index: " + itemIndex + " for a list of size: " + itemCountBeforeDelete);
        if (itemIndex > -1) {
            updateHomeView();
            updatePageArrows();
            updatePageMarkersWhenItemDeleted(itemIndex);
        }
    }

    private void updatePageMarkersWhenItemAdded() {
        ImageButton pageMarker;
        int itemCount = itemPageAdapter.getCount();
        int newPageIndex = itemCount - 1;
        if (itemCount > 1) {
            Log.v(LOG_TAG, "Adding unselected page marker");
            pageMarker = (ImageButton)inflater.inflate(R.layout.reusable_page_marker_unselected, null);
        } else {
            Log.v(LOG_TAG, "Adding selected page marker");
            pageMarker = (ImageButton)inflater.inflate(R.layout.reusable_page_marker_selected, null);
            previouslySelectedItem = 0;
        }
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        linearLayoutParams.gravity = Gravity.CENTER;
        pageMarker.setLayoutParams(linearLayoutParams);
        pageMarker.setTag(newPageIndex);

        //add the button to the view, to the left of the right arrow button
        pageControls.addView(pageMarker, pageControls.getChildCount() - 1);
        itemPager.setCurrentItem(newPageIndex);
    }

    private void updatePageMarkersWhenItemDeleted(int itemIndex) {
        pageControls.removeViewAt(itemIndex + 1); //+1 is to compensate for left arrow in page controls, but not in pager index
        //onPageSelected should handle setting the correct appearance of the remaining page markers
    }

    private void setArrowVisibility(int arrowType, int visibilityType) {
        View arrowButton = null;
        if (arrowType == ARROW_LEFT) {
            arrowButton = pageControls.getChildAt(0);
        } else if (arrowType == ARROW_RIGHT) {
            arrowButton = pageControls.getChildAt(pageControls.getChildCount() - 1);
        } else {
            Log.e(LOG_TAG, "Unrecognized arrow button type");
        }

        if (arrowButton != null) {
            arrowButton.setVisibility(visibilityType);
        }
    }

    //handler for all clickable views directly added to this view
    public void onClick(View view) {
        Log.d(LOG_TAG,"onClick, view:" + view.getClass().getSimpleName() + ":" + view.getId()+ ":" + view.getTag());
        switch (view.getId()) {
            case R.id.homeViewNoConnection:
                navigationActivity.startDiscoveringDevices();
                break;
            case R.id.buttonPageLeft:
                itemPager.setCurrentItem(itemPager.getCurrentItem() - 1); //itemPager handles trying to page to a non-existent page
                break;
            case R.id.buttonPageRight:
                itemPager.setCurrentItem(itemPager.getCurrentItem() + 1); //itemPager handles trying to page to a non-existent page
                break;
            case R.id.pageIndicatorDotUnselected:
            case R.id.pageIndicatorDotSelected:
                //button will have one of these ids, depending on if it was inflated first or later
                //tag contains the respective page number
                itemPager.setCurrentItem((Integer)view.getTag());
                break;
            default:
                ItemFragment itemFragment = (ItemFragment)itemPageAdapter.getItem(itemPager.getCurrentItem());
                itemFragment.onClick(view);
                break;
        }
    }

    private void updatePageArrows() {
        int currentPage = itemPager.getCurrentItem() + 1;
        int pageCount = itemPageAdapter.getCount();
        Log.v(LOG_TAG, "updatePageArrows, page " + currentPage + "/" + pageCount);
        if (itemPageAdapter.getCount() < 2) {
            setArrowVisibility(ARROW_LEFT, View.INVISIBLE);
            setArrowVisibility(ARROW_RIGHT, View.INVISIBLE);
        } else if (currentPage == 1) {
            //the first item is selected, hide left arrow only
            setArrowVisibility(ARROW_LEFT, View.INVISIBLE);
            setArrowVisibility(ARROW_RIGHT, View.VISIBLE);
        } else if (currentPage == pageCount) {
            //the last item is selected, hide right arrow only
            setArrowVisibility(ARROW_LEFT, View.VISIBLE);
            setArrowVisibility(ARROW_RIGHT, View.INVISIBLE);
        } else {
            setArrowVisibility(ARROW_LEFT, View.VISIBLE);
            setArrowVisibility(ARROW_LEFT, View.VISIBLE);
        }
        pageControls.invalidate();
    }

    private void updateHomeView() {
        Log.v(LOG_TAG, "updateHomeView, childCount: " + itemPageAdapter.getCount());
        if (itemPageAdapter.getCount() == 0) {
            itemPager.setVisibility(View.GONE);
            pageControls.setVisibility(View.GONE);
            noConnectionView.setVisibility(View.VISIBLE);
        } else {
            try{
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    itemPager.setVisibility(View.VISIBLE);
                    pageControls.setVisibility(View.VISIBLE);
                    noConnectionView.setVisibility(View.GONE);
                    itemPager.invalidate();
                }
            });
            }catch (NullPointerException e)
            {
                e.printStackTrace();
            }

        }
    }

    //ViewPager.OnPageChangeListener
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//        Log.d(LOG_TAG, MessageFormat.format("onPageScrolled, position: {0}, positionOffset:{1}, positionOffsetPixels:{2}",position,positionOffset, positionOffsetPixels));
    }

    @Override
    public void onPageSelected(int position) {
        Log.d(LOG_TAG, "onPageSelected:" + position);

        //unselect the old page marker
        // we use (previouslySelectedItem + 1) because of the left arrow that precedes the page markers ... might be helpful to refactor the page controls in a more isolated way
        ImageButton pageMarker = (ImageButton)pageControls.getChildAt(previouslySelectedItem + 1);
        pageMarker.setImageResource(R.drawable.dot_grey);

        //select the current page marker
        pageMarker = (ImageButton)pageControls.findViewWithTag(position);
        pageMarker.setImageResource(R.drawable.dot_white);
        previouslySelectedItem = position;

        updatePageArrows();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        Log.d(LOG_TAG, "scrolling states reference: idle:0, dragging:1, settling:2");
        Log.d(LOG_TAG, "onPageScrollStateChanged: " + state);
    }

    public void refresh() {
        Log.v(LOG_TAG, "refresh");
//        ItemTrackerPersistenceHelper helper = ItemTrackerPersistenceHelper.getInstance(getActivity().getApplicationContext());
//        ArrayList<BluetoothDevice> interestingDevices = new ArrayList<BluetoothDevice>();
//        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
//        Set<BluetoothDevice> deviceSet = adapter.getBondedDevices();
//        for (BluetoothDevice device : deviceSet)
//        {
//            if(helper.isPeripheralKnown(device.getAddress()))
//            {
//                interestingDevices.add(device);
//            }
//        }
//        this.addPages(interestingDevices);
        updateHomeView();
    }

    public void refreshPeripheral(BluetoothDevice peripheral) {
        ItemFragment itemFragment = itemPageAdapter.getItemByPeripheralAddress(peripheral.getAddress());
        //todo is this method still necessary?
    }

    //***************** Minimal Fragment Lifecycle overrides for logging


    @Override
    public void onStart() {
        super.onStart();
        Log.v(LOG_TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(LOG_TAG, "onResume");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.v(LOG_TAG, "onDestroyView");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(LOG_TAG, "onPause");
    }
    @Override
    public void onStop() {
        super.onStop();
        Log.v(LOG_TAG, "onStop");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG, "onDestroy");
    }

    public void setPeripherals(ArrayList<BluetoothDevice> peripheralList) {
        Log.v(LOG_TAG, "setPeripherals: " + peripheralList);
        if (peripheralList != null && peripheralList.size() > 0){
            addPages(peripheralList);
        } else {
            refresh(); //confirmed we are empty, refresh will cause "discover" button to appear for first time on startup
        }
    }

    public void peripheralConnected(BluetoothDevice peripheral) {
        Log.v(LOG_TAG, "peripheralConnected: " + peripheral);
        ItemFragment itemFragment = itemPageAdapter.getItemByPeripheralAddress(peripheral.getAddress());
        if (itemFragment != null && itemFragment.isVisible())
        {
            Log.v(LOG_TAG, "Peripheral Fragment Exists");
            itemFragment.setConnectionState(ItemFragment.CONNECTED);
            refreshPeripheral(peripheral);
        }
        else
        {
            Log.v(LOG_TAG, "unknown device connected, add it... " + peripheral.getAddress());
            for (int i=0; i<itemPageAdapter.getCount(); i++) {
                Log.e(LOG_TAG, "Current known peripheral: " + itemPageAdapter.getItem(i));
            }
            ArrayList<BluetoothDevice> peripherals = new ArrayList<BluetoothDevice>(1);
            peripherals.add(peripheral);
            addPages(peripherals);
     //       peripheralConnected(peripheral); //re-enter so that the connection state is updated next time
        }
    }

    public void peripheralDisconnected(BluetoothDevice peripheral) {
        Log.v(LOG_TAG, "proximityDeviceDisconnected: " + peripheral);
        ItemFragment itemFragment = itemPageAdapter.getItemByPeripheralAddress(peripheral.getAddress());
        if (itemFragment != null) { //might be null if the device has been deleted
            itemFragment.setConnectionState(ItemFragment.DISCONNECTED);
            refreshPeripheral(peripheral);
        }
        //todo maybe add a disconnected peripheral, even if we don't know about it?
    }

    public void peripheralBatteryUpdate(BluetoothDevice peripheral, int batteryPercent) {
        Log.v(LOG_TAG, "peripheralBatteryUpdate: " + peripheral);
        ItemFragment itemFragment = itemPageAdapter.getItemByPeripheralAddress(peripheral.getAddress());
        if (itemFragment != null) { //might be null if the device has been deleted
            itemFragment.setBatteryPercent(batteryPercent);
            refreshPeripheral(peripheral);
        }
    }

    public void peripheralRssiUpdate(BluetoothDevice peripheral, short rssiValue) {
        Log.v(LOG_TAG, "proximityDeviceRssiUpdate: " + peripheral + ", value: " + rssiValue);
        ItemFragment itemFragment = itemPageAdapter.getItemByPeripheralAddress(peripheral.getAddress());
        if(itemFragment != null)
        {
           itemFragment.setRssiDecibels(rssiValue);
        }
    }



    @Override
    public void onDetach() {
        Log.v(LOG_TAG, "onDetach");
        super.onDetach();
    }

    @Override
    public void onAttach(Activity activity) {
        Log.v(LOG_TAG, "onAttach: " + activity);
        super.onAttach(activity);

        try {
            mCallBack = (NavPagerInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }
}