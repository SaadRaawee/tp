package com.phonehalo.itemtracker.raawee.registration;

import com.phonehalo.itemtracker.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class DeviceActivity extends Activity {

	Button btnYes = null;
	Button btnNo = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_layout);

		btnYes = (Button) findViewById(R.id.btn_yes);
		btnYes.setOnClickListener(listener);

		btnNo = (Button) findViewById(R.id.btn_no);
		btnNo.setOnClickListener(listener);
	}

	View.OnClickListener listener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.btn_no:
				DeviceNo();
				break;
			case R.id.btn_yes:
				DeviceYes();
				break;
			default:
				break;
			}
		}
	};

	private void DeviceYes() {
		//Toast.makeText(DeviceActivity.this, "Yes", Toast.LENGTH_LONG).show();
		Intent intent = new Intent(DeviceActivity.this, InstructionActivity.class);
		startActivity(intent);
	}

	private void DeviceNo() {
		//Toast.makeText(DeviceActivity.this, "No", Toast.LENGTH_LONG).show();
		Intent intent = new Intent(DeviceActivity.this, AboutUsActivity.class);
		startActivity(intent);	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.device, menu);
		return true;
	}

	public void goToInstruction(View view) {
		Intent ObjIntent = new Intent(this, InstructionActivity.class);
		startActivityForResult(ObjIntent, 1);
	}

	public void goToBuy(View view) {
		Intent ObjIntent = new Intent(this, AboutUsActivity.class);
		startActivityForResult(ObjIntent, 2);
	}

}
