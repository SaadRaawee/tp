package com.phonehalo.itemtracker.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.phonehalo.itemtracker.R;
import com.phonehalo.itemtracker.db.PeripheralSettings;
import com.phonehalo.itemtracker.helper.IntentHelper;
import com.phonehalo.itemtracker.helper.ItemTrackerServiceHelper;
import com.phonehalo.itemtracker.utility.Log;

import java.util.ArrayList;

public class ChooseIconActivity extends Activity implements ViewPager.OnPageChangeListener {
    private static final String LOG_TAG = "ChooseIconActivity";
    private static final int REQUEST_CODE = 777; //arbitrary
    private ViewPager pager;
    private ItemIconPagerAdapter pagerAdapter;
    private View rightArrowView;
    private View leftArrowView;
    public static final String RESOURCE_URI_PREFIX = "android.resource://com.phonehalo.itemtracker/";
    private PeripheralSettings peripheralSettings;

    public void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        String action = getIntent().getAction();
        String peripheralAddress = getIntent().getStringExtra(IntentHelper.EXTRA_DEVICE_ADDRESS);

        peripheralSettings = new PeripheralSettings();
        peripheralSettings.setPeripheralAddress(peripheralAddress);
        peripheralSettings.setDeviceConnectionState(true);

        if (IntentHelper.ACTION_FIRST_CHOICE.equals(action)) {
            initForFirstIconChoice();
        } else if (IntentHelper.ACTION_UPDATE_CHOICE.equals(action)) {
            initForUpdateIconChoice();
        } else {
            Log.v(LOG_TAG, "Unknown action should only be seen in dev testing, when starting activity directly");
            //testing case only
            initForFirstIconChoice();
//            initForUpdateIconChoice();
        }
    }

    private void initForFirstIconChoice() {

        setContentView(R.layout.choose_first_icon_layout);
        pager = (ViewPager) findViewById(R.id.itemPager);
        pagerAdapter = new ItemIconPagerAdapter(getFragmentManager());
        pager.setAdapter(pagerAdapter);

        rightArrowView = findViewById(R.id.buttonPageRight);
        leftArrowView = findViewById(R.id.buttonPageLeft);
        leftArrowView.setVisibility(View.INVISIBLE);//starting with item 0, no left arrow at start

        //set the name from the intent
        TextView itemNameTextView = (TextView) findViewById(R.id.itemName);
        itemNameTextView.setText(getIntent().getStringExtra(IntentHelper.EXTRA_DEVICE_NAME));

        //set the page change listener
        pager.setOnPageChangeListener(this);
    }

    private void initForUpdateIconChoice() {
        setContentView(R.layout.choose_updated_icon_layout);
    }

    public void onClick(View view) {
        Log.v(LOG_TAG, "onClick: " + view.getId());

        //cases for first icon choice
        switch (view.getId()) {
            case R.id.buttonDone:
                Uri chosenIconUri = pagerAdapter.getImageUriForItem(pager.getCurrentItem());
                peripheralSettings.setPeripheralImageUri(chosenIconUri);
                saveAndFinish();
                break;
            case R.id.buttonTakePhoto:
                findOrTakePhoto();
                break;
            case R.id.itemImageChoice:
                Log.v(LOG_TAG, "icon has tag: " + view.getTag());
                break;
            case R.id.buttonPageRight:
                pager.setCurrentItem(pager.getCurrentItem() + 1);
                break;
            case R.id.buttonPageLeft:
                pager.setCurrentItem(pager.getCurrentItem() - 1);
                break;


            //cases for updated icon choice
            case R.id.imageBriefcase:
            case R.id.textBriefcase:
                peripheralSettings.setPeripheralImageUri(Uri.parse(RESOURCE_URI_PREFIX + R.drawable.item_icon_briefcase));
                saveAndFinish();
                break;
            case R.id.imageCamera:
            case R.id.textCamera:
                peripheralSettings.setPeripheralImageUri(Uri.parse(RESOURCE_URI_PREFIX + R.drawable.item_icon_camera));
                saveAndFinish();
                break;
            case R.id.imageKeys:
            case R.id.textKeys:
                peripheralSettings.setPeripheralImageUri(Uri.parse(RESOURCE_URI_PREFIX + R.drawable.item_icon_car_key_fob));
                saveAndFinish();
                break;
            case R.id.imagePurse:
            case R.id.textPurse:
                peripheralSettings.setPeripheralImageUri(Uri.parse(RESOURCE_URI_PREFIX + R.drawable.item_icon_purse));
                saveAndFinish();
                break;
            case R.id.imageWallet:
            case R.id.textWallet:
                peripheralSettings.setPeripheralImageUri(Uri.parse(RESOURCE_URI_PREFIX + R.drawable.item_icon_wallet));
                saveAndFinish();
                break;
            case R.id.imageTakePhoto:
            case R.id.textTakePhoto:
                findOrTakePhoto();
                break;
        }
    }

    private void saveAndFinish() {
        ItemTrackerServiceHelper.getInstance().persistPeripheralSettings(peripheralSettings);
        finish();
    }

    private void findOrTakePhoto() {
        Intent pickIntent = new Intent();
        pickIntent.setType("image/*");
        pickIntent.setAction(Intent.ACTION_GET_CONTENT);
        pickIntent.addCategory(Intent.CATEGORY_OPENABLE);

        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        Intent chooserIntent = Intent.createChooser(pickIntent, getString(R.string.takeOrSelectPhoto));
        chooserIntent.putExtra
                (
                        Intent.EXTRA_INITIAL_INTENTS,
                        new Intent[] { takePhotoIntent }
                );

        startActivityForResult(chooserIntent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(LOG_TAG, "resultCode: " + resultCode + ", intent/data: " + data);
        //we have a resulting image from the camera, or it was cancelled
        //if not cancelled, do something
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            //save bitmap location
            //it will be checked and loaded by the ItemFragment class
            peripheralSettings.setPeripheralImageUri(data.getData());

            //finish() in any case where we get a result
            //the user re-enters the update icon activity if they want to change it
            saveAndFinish();
        }
        //do nothing if the user has cancelled
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {
        //don't care
    }

    @Override
    public void onPageSelected(int i) {
        //make arrows visible properly
        if (pager.getCurrentItem() > 0) {
            leftArrowView.setVisibility(View.VISIBLE);
        } else {
            leftArrowView.setVisibility(View.INVISIBLE);
        }

        if (pager.getCurrentItem() < pagerAdapter.getCount() - 1) {
            rightArrowView.setVisibility(View.VISIBLE);
        } else {
            rightArrowView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {
        //don't care
    }

    private class ItemIconPagerAdapter extends FragmentStatePagerAdapter {

        private static final int NUM_PAGES = 5;

        ArrayList<Integer> itemIconResources;
        ArrayList<ItemIconFragment> itemIconFragmentArrayList;
        private final ArrayList<Uri> itemIconResourceUris;

        public ItemIconPagerAdapter(FragmentManager fm) {
            super(fm);
            itemIconResources = new ArrayList<Integer>(NUM_PAGES);
            itemIconFragmentArrayList = new ArrayList<ItemIconFragment>(NUM_PAGES);
            itemIconResourceUris = new ArrayList<Uri>(NUM_PAGES);

            itemIconResources.add(R.drawable.item_icon_purse);
            itemIconResourceUris.add(Uri.parse(RESOURCE_URI_PREFIX + R.drawable.item_icon_purse));

            itemIconResources.add(R.drawable.item_icon_briefcase);
            itemIconResourceUris.add(Uri.parse(RESOURCE_URI_PREFIX + R.drawable.item_icon_briefcase));

            itemIconResources.add(R.drawable.item_icon_car_key_fob);
            itemIconResourceUris.add(Uri.parse(RESOURCE_URI_PREFIX + R.drawable.item_icon_car_key_fob));

            itemIconResources.add(R.drawable.item_icon_wallet);
            itemIconResourceUris.add(Uri.parse(RESOURCE_URI_PREFIX + R.drawable.item_icon_wallet));

            itemIconResources.add(R.drawable.item_icon_camera);
            itemIconResourceUris.add(Uri.parse(RESOURCE_URI_PREFIX + R.drawable.item_icon_camera));
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Override
        public Fragment getItem(int i) {
            Log.v(LOG_TAG, "getItem: " + i);
            ItemIconFragment itemIconFragment = null;
            if (itemIconFragmentArrayList.size() > i) {
                itemIconFragment = itemIconFragmentArrayList.get(i);
            }
            if (itemIconFragment == null) {
                itemIconFragment = new ItemIconFragment(itemIconResources.get(i), itemIconResourceUris.get(i));
                itemIconFragmentArrayList.add(i, itemIconFragment);
            }

            return itemIconFragment;
        }

        public Uri getImageUriForItem(int item) {
            return itemIconResourceUris.get(item);
        }
    }

    public class ItemIconFragment extends Fragment {

        private final int imageResource;
        private final Uri imageUri;

        private ItemIconFragment(int imageResource, Uri imageUri) {
            this.imageResource = imageResource;
            this.imageUri = imageUri;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Log.v(LOG_TAG, "onCreateView");
            ViewGroup rootView =
                    (ViewGroup) inflater.inflate(R.layout.choose_first_icon_fragment_layout, container, false);

            ImageView imageView = (ImageView) rootView.findViewById(R.id.itemImageChoice);
            imageView.setTag(imageUri);
            imageView.setImageResource(imageResource);

            return rootView;
        }
    }
}