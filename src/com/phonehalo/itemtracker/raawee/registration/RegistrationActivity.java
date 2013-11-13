package com.phonehalo.itemtracker.raawee.registration;

import com.phonehalo.itemtracker.Constant;
import com.phonehalo.itemtracker.R;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class RegistrationActivity extends Activity {

	Context context = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.registration_layout);
		context = this;
		PreferenceManagement.getInstance(context).saveValueForBooleanKey(Constant.IS_REGISTERED, false);
		android.app.ActionBar actionBarObj = getActionBar();
		actionBarObj.setDisplayShowCustomEnabled(true);
		actionBarObj.setDisplayShowTitleEnabled(false);
		actionBarObj.setDisplayShowHomeEnabled(false);

		LayoutInflater inflator = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflator.inflate(R.layout.action_bar_layout, null);
		actionBarObj.setCustomView(view);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.registration, menu);
		return true;
	}

	public void leftBtnAction(View view) {
		// Intent objIntent = new Intent(this, InstructionActivity.class);
		// startActivity(objIntent);
		finish();
	}

	public void rightBtnAction(View view) {
		// Intent objIntent = new Intent(this, DeviceActivity.class);
		// startActivity(objIntent);

		RegisterandNext();
	}

	private void RegisterandNext() {
		EditText firstnameTxtView = (EditText) findViewById(R.id.first_name_editTxtVw);
		String f_name = firstnameTxtView.getEditableText().toString();

		EditText lastnameTxtView = (EditText) findViewById(R.id.last_name_editTxtVw);
		String l_name = lastnameTxtView.getEditableText().toString();

		EditText emailTxtView = (EditText) findViewById(R.id.email_editTxtVw);
		String email = emailTxtView.getEditableText().toString();

		if (isDataValid(f_name, l_name, email)) {
			new RegistrationASYNC(f_name + "." + l_name, email, this)
					.execute("");
		} else {
			Toast.makeText(context, "All information is required!",
					Toast.LENGTH_LONG).show();
		}

	}

	private boolean isDataValid(String FName, String LNAme, String Email) {
		if (!FName.trim().equalsIgnoreCase("")
				&& !LNAme.trim().equalsIgnoreCase("")
				&& !Email.trim().equalsIgnoreCase("")) {
			return true;
		}
		return false;
	}
}
