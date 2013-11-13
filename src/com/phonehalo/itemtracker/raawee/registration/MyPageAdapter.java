package com.phonehalo.itemtracker.raawee.registration;

import java.util.List;

import android.support.v13.app.FragmentPagerAdapter;
import android.app.Fragment;
import android.app.FragmentManager;

//import android.support.v4.app.FragmentPagerAdapter;

public class MyPageAdapter extends FragmentPagerAdapter {

	private List<Fragment> fragments;

	public MyPageAdapter(FragmentManager fm,
			List<android.app.Fragment> fragments2) {
		super(fm);
		this.fragments = fragments2;
	}

	@Override
	public Fragment getItem(int position) {

		return this.fragments.get(position);
	}

	@Override
	public int getCount() {

		return this.fragments.size();

	}

}
