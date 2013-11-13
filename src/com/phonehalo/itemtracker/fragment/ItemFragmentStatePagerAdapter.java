package com.phonehalo.itemtracker.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Parcelable;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import com.phonehalo.itemtracker.utility.Log;

import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;

/**
 * The android.support.v13 support library is not used in this case for backward-compatibility with newer API level classes,
 * but rather for its useful classes that were not, for some reason, released in the newer SDK API levels.
 *
 */
public class ItemFragmentStatePagerAdapter extends FragmentStatePagerAdapter {

    private static final String LOG_TAG = "ItemFragmentStatePagerAdapter";

    //these ArrayLists must correspond to each other
    private ArrayList<ItemFragment> itemFragmentList = new ArrayList<ItemFragment>();
    private ArrayList<String> peripheralAddressList = new ArrayList<String>();
    private final FragmentManager fragmentManager;

    /**
     *
     * @param fm the FragmentManager
     *
     */
    public ItemFragmentStatePagerAdapter(FragmentManager fm) {
        super(fm);
        this.fragmentManager = fm;
        Log.d(LOG_TAG, "constructor");
    }

    /**
     *
     * @param i ItemFragment index
     * @return the ItemFragment for that index
     *
     */
    @Override
    public Fragment getItem(int i) {
        Log.d(LOG_TAG, MessageFormat.format("getItem: {0}", i));
        return itemFragmentList.get(i);
    }

    //called often!
    @Override
    public int getCount() {
//        Log.d(LOG_TAG, MessageFormat.format("getCount: returning {0}", itemFragmentList.size()));
        return itemFragmentList.size();
    }

    public boolean addItem(ItemFragment itemFragment) {
        Log.d(LOG_TAG, MessageFormat.format("addItem: {0}", itemFragment));
        boolean itemAdded = false;
        if (!peripheralAddressList.contains(itemFragment.getPeripheral().getAddress())) {
            peripheralAddressList.add(itemFragment.getPeripheral().getAddress());
            itemFragmentList.add(itemFragment);

            //add fragment to fragment manager
            fragmentManager.dump("add peripheral ", null, new PrintWriter(System.out, true), null);

            itemAdded = true;
            notifyDataSetChanged();
        } else {
            Log.v(LOG_TAG, "Peripheral is already added");
        }
        if (peripheralAddressList.size() != itemFragmentList.size()) {
            Log.e(LOG_TAG, "Peripheral tracking lists are corrupted");
        }
        return itemAdded;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Log.v(LOG_TAG, "instantiateItem: " + position);
        return super.instantiateItem(container, position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        Log.v(LOG_TAG, "destroyItem: " + position);
        super.destroyItem(container, position, object);
    }

    @Override
    public void startUpdate(ViewGroup container) {
        Log.v(LOG_TAG, "startUpdate");
        super.startUpdate(container);
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        Log.v(LOG_TAG, "setPrimaryItem: " + position);
        super.setPrimaryItem(container, position, object);
    }

    @Override
    public void finishUpdate(ViewGroup container) {
        Log.v(LOG_TAG, "finishUpdate");
        super.finishUpdate(container);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        Log.v(LOG_TAG, "isViewFromObject");
        return super.isViewFromObject(view, object);
    }

    @Override
    public Parcelable saveState() {
        Log.v(LOG_TAG, "saveState");
        fragmentManager.dump("before save state ", null, new PrintWriter(System.out, true), null);
        return super.saveState();
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        Log.v(LOG_TAG, "restoreState");
        super.restoreState(state, loader);
    }

    /**
     *
     * @param btAddress address of peripheral
     * @return ItemFragment may be null
     */
    public ItemFragment getItemByPeripheralAddress(String btAddress) {
        int itemIndex = peripheralAddressList.indexOf(btAddress);
        ItemFragment resultFragment = null;
        if (itemIndex > -1) {
            resultFragment = itemFragmentList.get(itemIndex);
        }
        if (resultFragment == null) {
            Log.v(LOG_TAG, "unknown peripheral: " + btAddress);
        }
        return resultFragment;
    }

    public int getItemIndexByPeripheral(String peripheralAddress) {
        return peripheralAddressList.indexOf(peripheralAddress);
    }

    public void deletePeripheral(String btAddress) {
        Log.v(LOG_TAG, "deletePeripheral: " + btAddress);
        Log.v(LOG_TAG, "peripheral addresses: " + peripheralAddressList);
        int indexToRemove = peripheralAddressList.indexOf(btAddress);
        if (indexToRemove > -1) {
            peripheralAddressList.remove(indexToRemove);
            ItemFragment itemFragmentToRemove = itemFragmentList.get(indexToRemove);
            itemFragmentList.remove(indexToRemove);

            //remove the Fragment
            FragmentManager fm = itemFragmentToRemove.getFragmentManager();
            try
            {
            fm.dump("deletePeripheral ", null, new PrintWriter(System.out, true), null);
            }
            catch (NullPointerException e)
            {
                e.printStackTrace();
            }

            //cause pager to refresh
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemPosition(Object object) {
        Log.v(LOG_TAG, "getItemPosition: " + object);
        int itemIndex = itemFragmentList.indexOf(object);
        if (itemIndex > -1) {
            return itemIndex;
        } else {
            return POSITION_NONE;
        }
    }
}
