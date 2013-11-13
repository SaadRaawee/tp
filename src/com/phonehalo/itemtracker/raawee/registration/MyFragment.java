package com.phonehalo.itemtracker.raawee.registration;

import com.phonehalo.itemtracker.R;

import android.app.Fragment;
//import android.support.v4.app.Fragment;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class MyFragment extends Fragment{
	public static final String EXTRA_MESSAGE = "IMAGE_RESORCE";
	public static final String SUB_TEXT = 	    "DETAIL";

	 
	 public static final MyFragment newInstance(String message, String txt)
	 {
	   MyFragment f = new MyFragment();
	   Bundle bdl = new Bundle(1);
	   bdl.putString(EXTRA_MESSAGE, message);
	   bdl.putString(SUB_TEXT, txt);
	   f.setArguments(bdl);
	   return f;
	 }
	 
	 @Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container,
	   Bundle savedInstanceState) {
	   String message = getArguments().getString(EXTRA_MESSAGE);
	   String txt = getArguments().getString(SUB_TEXT);
	   View view;
	   if (message.contentEquals("screen_six"))
		   view = inflater.inflate(R.layout.learn_more_layout, container, false);
	   else
		   view = inflater.inflate(R.layout.fragment_layout, container, false);
	   
	   TextView messageTextView = (TextView)view.findViewById(R.id.fragment_txtView);
	   messageTextView.setText(txt);
	   
	   ImageView imageView = (ImageView)view.findViewById(R.id.imageView);

	   Resources res = getResources();
	   String mDrawableName = message;
	   int resID = res.getIdentifier(mDrawableName , "drawable", "com.phonehalo.itemtracker");
	   Drawable drawable = res.getDrawable(resID );
	   imageView.setImageDrawable(drawable);
	   return view;
	 
	 }
}
