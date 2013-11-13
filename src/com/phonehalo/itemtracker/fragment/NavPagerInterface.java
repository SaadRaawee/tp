package com.phonehalo.itemtracker.fragment;

import android.bluetooth.BluetoothDevice;

/**
 * Created with IntelliJ IDEA.
 * User: adminadmin
 * Date: 4/22/13
 * Time: 10:41 PM
 * To change this template use File | Settings | File Templates.
 */
public interface NavPagerInterface
{
    public boolean ringDevice(BluetoothDevice device);
    public boolean silenceDevice(BluetoothDevice device);
}
