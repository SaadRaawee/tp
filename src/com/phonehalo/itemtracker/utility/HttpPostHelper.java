package com.phonehalo.itemtracker.utility;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adminadmin on 10/10/13.
 */
public class HttpPostHelper {

    private static final String LOG_TAG = "HttpPoserHelper";

    public static int httpPost(List<NameValuePair> nameValuePairs, String postUrl) {
        Log.v(LOG_TAG, "Posting to: " + postUrl);
        Log.v(LOG_TAG, "Posting data: " + nameValuePairs);
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(postUrl);
        int result=0;
        try {
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpclient.execute(httppost);
            Log.v(LOG_TAG, "POST response status code: " + response.getStatusLine().getStatusCode());
            if (response!=null) {
            	result = response.getStatusLine().getStatusCode();
			}
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to post registration data:"+e.getMessage());
        }
        return result;
    }

    public static int registerUser(Context context, String regId, String regEmail) {

        String regUrl = "http://www.phonehalo.com/itemtrackr/trackrregg.php";

        Log.v(LOG_TAG, "registerUser(), regId: " + regId);
        PackageInfo packageInfo;
        String appVersion = "";
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            appVersion = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(LOG_TAG, "Failed to retrieve version name for registration");
        }

        ArrayList<NameValuePair> registrationData = new ArrayList<NameValuePair>();
        registrationData.add(new BasicNameValuePair("fname", regId));
        registrationData.add(new BasicNameValuePair("femail", regEmail));
        registrationData.add(new BasicNameValuePair("fOS", Build.VERSION.RELEASE));
        registrationData.add(new BasicNameValuePair("fdate", appVersion));
        registrationData.add(new BasicNameValuePair("flocale", Build.MODEL));
        registrationData.add(new BasicNameValuePair("fdevice", context.getPackageName()));
        return HttpPostHelper.httpPost(registrationData, regUrl);
    }
}
