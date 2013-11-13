package com.phonehalo.itemtracker.raawee.registration;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PreferenceManagement {
	
	private Context 			context		=	null;
	private SharedPreferences 	prefs 		= 	null;
	private static PreferenceManagement preferenceManagement = null;
	
	public static PreferenceManagement getInstance(Context context) {
		if (preferenceManagement == null) {
			preferenceManagement = new PreferenceManagement(context);
		}
		return preferenceManagement;
	}
	
	private PreferenceManagement(Context context) {
		this.context =  context;		
	}
		
	public void saveValueForBooleanKey(String key,boolean value)
	{
		prefs = context.getSharedPreferences(key,Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putBoolean(key, value);
		editor.commit();		
	}
	
	public boolean getValueForKey_boolean(String key)
	{
		boolean result = false;
		prefs = context.getSharedPreferences(key,Context.MODE_PRIVATE);
		result = prefs.getBoolean(key, false);
		return result;
	}
	
	public boolean isKeyPresent(String key)
	{
		boolean result = false;
		prefs = context.getSharedPreferences(key,Context.MODE_PRIVATE);
		if(prefs.contains(key))
		{
			result = true;
		}
		return result;
	}
	
}
