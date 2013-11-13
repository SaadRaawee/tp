package com.phonehalo.itemtracker.raawee.registration;


import com.phonehalo.itemtracker.Constant;
import com.phonehalo.itemtracker.utility.HttpPostHelper;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

public class RegistrationASYNC extends AsyncTask<String, Void, Boolean> {

	private String _regId, _email;
	Context _context;
	MyProgressBar objProgressbar;

	public RegistrationASYNC(String regId, String email, Context context) {
		this._regId = regId;
		this._email = email;
		this._context = context;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		objProgressbar = new MyProgressBar(_context, "Registering user",
				"Please Wait...");
		objProgressbar.show();
	}

	@Override
	protected Boolean doInBackground(String... params) {
		if (HttpPostHelper.registerUser(_context, _regId, _email) == 200) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		objProgressbar.dismis();
		if (result)
		{
			Toast.makeText(_context, "Registration completes succesfully",
					Toast.LENGTH_LONG).show();
			 Intent objIntent = new Intent(_context, DeviceActivity.class);
			 _context.startActivity(objIntent);
			 PreferenceManagement.getInstance(_context).saveValueForBooleanKey(Constant.IS_REGISTERED, true);
		}
		else
		{
			Toast.makeText(_context, "Registration Failed", Toast.LENGTH_LONG)
					.show();
		}
	}
}
