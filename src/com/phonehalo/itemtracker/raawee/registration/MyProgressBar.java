package com.phonehalo.itemtracker.raawee.registration;

import android.app.ProgressDialog;
import android.content.Context;

public class MyProgressBar extends ProgressDialog {

	private String title,message;
	private Context context;
	private ProgressDialog  progressbar;
	public MyProgressBar(Context context, String title, String message) {
		super(context);
		this.context=context;
		this.title=title;
		this.message=message;
	}
	
	public void show() {
		
		progressbar=new ProgressDialog (context);
		progressbar.setTitle(title);
		progressbar.setMessage(message);
		progressbar.setCancelable(false);
		progressbar.setIndeterminate(true);
		progressbar.show();
	}
	
	
	public void dismis() {
		progressbar.dismiss();
	}

}
