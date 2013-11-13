package com.phonehalo.itemtracker;

import com.phonehalo.itemtracker.R;
import com.phonehalo.itemtracker.activity.NavigationActivity;
import com.phonehalo.itemtracker.raawee.registration.PreferenceManagement;
import com.phonehalo.itemtracker.raawee.registration.RegistrationActivity;
//import com.phonehalo.itemtracker.raawee.registration.RegistrationActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
//import android.support.v7.app.ActionBar;
//import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.View;

public class GetStartedActivity extends Activity {
Context context = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.get_started_layout);
		context = this;
		android.app.ActionBar actionBar = getActionBar(); // getSupportActionBar();
		actionBar.hide();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		return false;
	}

	public void getStarted(View view) {
		boolean isDeviceAlreadyRegistered = PreferenceManagement.getInstance(context).getValueForKey_boolean(Constant.IS_REGISTERED);
		if (isDeviceAlreadyRegistered) {
			finish();
			Intent i = new Intent(context, NavigationActivity.class);
			startActivity(i);
		} else {
			Intent objIntent = new Intent(context, RegistrationActivity.class);
			startActivity(objIntent);
		}
	}

}
