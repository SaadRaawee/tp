package com.phonehalo.itemtracker.raawee.registration;

import java.util.ArrayList;
import java.util.List;

import com.phonehalo.itemtracker.R;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
//import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.app.FragmentManager;
//import android.support.v7.app.ActionBar;
//import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AboutUsActivity extends Activity {

	MyPageAdapter pageAdapter;
	public static final String DOMAIN_PHONE_HALO = "http://www.phone-halo.com/";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_us_layout);

		List<android.app.Fragment> fragments = getFragments();
		FragmentManager fm = getFragmentManager();

		pageAdapter = new MyPageAdapter(fm, fragments);
		ViewPager pager = (ViewPager) findViewById(R.id.viewpager);
		pager.setAdapter(pageAdapter);

		ActionBar actionBarObj = getActionBar();
		actionBarObj.setDisplayShowCustomEnabled(true);
		actionBarObj.setDisplayShowTitleEnabled(false);
		actionBarObj.setDisplayShowHomeEnabled(false);

		LayoutInflater inflator = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflator.inflate(R.layout.action_bar_layout, null);

		TextView titleTV = (TextView) view.findViewById(R.id.title);
		titleTV.setText(R.string.featured_txt);

		Button left_btn = (Button) view.findViewById(R.id.lft_btn);
		left_btn.setText(R.string.cancel_text);

		Button rght_btn = (Button) view.findViewById(R.id.rght_btn);
		rght_btn.setText(R.string.buy_txt);

		actionBarObj.setCustomView(view);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.about_us, menu);
		return true;
	}

	private List<android.app.Fragment> getFragments() {
		List<android.app.Fragment> fList = new ArrayList<android.app.Fragment>();

		fList.add(MyFragment.newInstance("screen_one",
				this.getString(R.string.feature_screen_1_text)));
		fList.add(MyFragment.newInstance("itemtrackr_ph_logos",
				this.getString(R.string.feature_screen_2_text)));
		fList.add(MyFragment.newInstance("screen_five",
				this.getString(R.string.feature_screen_3_text)));
		fList.add(MyFragment.newInstance("screen_two",
				this.getString(R.string.feature_screen_4_text)));
		fList.add(MyFragment.newInstance("screen_three",
				this.getString(R.string.feature_screen_5_text)));
		fList.add(MyFragment.newInstance("screen_four",
				this.getString(R.string.feature_screen_6_text)));
		fList.add(MyFragment.newInstance("screen_six",
				this.getString(R.string.feature_screen_7_text)));
		return fList;
	}

	public void leftBtnAction(View view) {
		finish();
	}

	public void rightBtnAction(View view) {

		Intent browserIntent = new Intent(Intent.ACTION_VIEW,
				Uri.parse(DOMAIN_PHONE_HALO));
		startActivity(browserIntent);
	}

	public void test(View view) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW,
				Uri.parse(DOMAIN_PHONE_HALO));
		startActivity(browserIntent);
	}

}
