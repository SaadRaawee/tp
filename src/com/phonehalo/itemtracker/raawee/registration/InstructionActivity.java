package com.phonehalo.itemtracker.raawee.registration;

import com.phonehalo.itemtracker.R;
import com.phonehalo.itemtracker.activity.NavigationActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
//import android.support.v7.app.ActionBar;
//import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class InstructionActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.instruction_layout);

		android.app.ActionBar actionBarObj = getActionBar();
		actionBarObj.setDisplayShowCustomEnabled(true);
		actionBarObj.setDisplayShowTitleEnabled(false);
		actionBarObj.setDisplayShowHomeEnabled(false);

		LayoutInflater inflator = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflator.inflate(R.layout.action_bar_layout, null);
		
        TextView titleTV = (TextView) view.findViewById(R.id.title);
        titleTV.setText(R.string.title_activity_instruction);
        
        Button left_btn = (Button) view.findViewById(R.id.lft_btn);
        left_btn.setText(R.string.back_text);
        
        Button rght_btn = (Button) view.findViewById(R.id.rght_btn);
        rght_btn.setText(R.string.continue_text);

		actionBarObj.setCustomView(view);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.instruction, menu);
		return true;
	}
	
	public void leftBtnAction(View view) {
		finish();

	}

	public void rightBtnAction(View view) {
		Intent intent = new Intent(InstructionActivity.this, NavigationActivity.class);
		startActivity(intent);		
	}
}
