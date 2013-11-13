package com.phonehalo.itemtracker.helper;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Looper;
import com.phonehalo.itemtracker.Constant;
import com.phonehalo.itemtracker.db.ExternalAlertSettings;
import com.phonehalo.itemtracker.db.ItemTrackerPersistenceHelper;
import com.phonehalo.itemtracker.db.PeripheralSettings;
import com.phonehalo.itemtracker.utility.Log;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import twitter4j.*;
import twitter4j.auth.AccessToken;

import java.text.MessageFormat;
import java.util.*;

public class ExternalAlertHelper {

    private static final String LOG_TAG = "ExternalAlertHelper";
    public static final String ALERT_EMAIL_POST_URL = "http://phonehaloihalo.appspot.com/alertEmail";
    private static final String INSITE_APP_NAME = "inSite";
    private final Context context;
    private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
    private static final String PENDING_PUBLISH_KEY = "pendingPublishReauthorization";
    private boolean pendingPublishReauthorization = false;

    public ExternalAlertHelper(Context context) {
        this.context = context;
    }

    private ExternalAlertSettings getExternalAlertSettings() {
        return ItemTrackerPersistenceHelper.getInstance(context.getApplicationContext()).getExternalAlertSettings();
    }

    private void httpPost(List<NameValuePair> nameValuePairs, String postUrl) {
        Log.v(LOG_TAG, "Posting to: " + postUrl);
        Log.v(LOG_TAG, "Posting data: " + nameValuePairs);
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(postUrl);

        try {
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpclient.execute(httppost);
            Log.v(LOG_TAG, "POST response status code: " + response.getStatusLine().getStatusCode());
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to post registration data", e);
        }
    }

    private void sendEmailAlert3(PeripheralSettings peripheralSettings, Location lastKnownLocation) {
        Log.v(LOG_TAG, "sendEmailAlert3");
        Log.v(LOG_TAG, "peripheralSettings: " + peripheralSettings);
        Log.v(LOG_TAG, "lastKnownLocation: " + lastKnownLocation);
        if (getExternalAlertSettings().isEmailAlertOn()) {

            Double lat = (lastKnownLocation != null) ? lastKnownLocation.getLatitude() : 0.0;
            Double lon = (lastKnownLocation != null) ? lastKnownLocation.getLongitude() : 0.0;

            String peripheralName = (peripheralSettings != null) ? peripheralSettings.getPeripheralName() : "DefaultName";

            String peripheralImageUriString = (peripheralSettings != null && peripheralSettings.getPeripheralImageUri() != null)
                    ? peripheralSettings.getPeripheralImageUri().toString() : Uri.EMPTY.toString();
            ExternalAlertSettings externalAlertSettings = getExternalAlertSettings();
            if (externalAlertSettings.getEmailAddress() != null && externalAlertSettings.getEmailAddress().length() > 0) {

                ArrayList<NameValuePair> emailAlertData = new ArrayList<NameValuePair>();
                emailAlertData.add(new BasicNameValuePair("targetEmail", ""+externalAlertSettings.getEmailAddress()));
                emailAlertData.add(new BasicNameValuePair("targetCcEmail", ""+externalAlertSettings.getEmailCCAddress()));
                emailAlertData.add(new BasicNameValuePair("uItemName", ""+peripheralName));
                emailAlertData.add(new BasicNameValuePair("uItemType", ""+peripheralImageUriString));
                emailAlertData.add(new BasicNameValuePair("mLatitude", "" + lat));
                emailAlertData.add(new BasicNameValuePair("mLongitude", "" + lon));
                emailAlertData.add(new BasicNameValuePair("mAppName", INSITE_APP_NAME));

                httpPost(emailAlertData, ALERT_EMAIL_POST_URL);
            }
        }
    }

    private void sendEmailAlert2(PeripheralSettings peripheralSettings, Location lastKnownLocation) {
        if (Looper.getMainLooper().equals(Looper.myLooper())) {
            //call this method on another thread
            SendEmailAlertTask task = new SendEmailAlertTask();
            task.execute(peripheralSettings, lastKnownLocation);
        } else {
            sendEmailAlert3(peripheralSettings, lastKnownLocation);
        }
    }

    public void sendEmailAlert(String peripheralAddress) {
        try{
        ItemTrackerPersistenceHelper persistenceHelper = ItemTrackerPersistenceHelper.getInstance(context);
        Location lastKnownLocation = persistenceHelper.getMostRecentLocation(peripheralAddress);
        PeripheralSettings peripheralSettings = persistenceHelper.getPeripheralSettings(peripheralAddress);
        sendEmailAlert2(peripheralSettings, lastKnownLocation);
        }
        catch (NullPointerException e)
        {
            Log.e(LOG_TAG, "sendEmailAlert: no location " + e.getLocalizedMessage());
        }
    }

    private class SendEmailAlertTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... objects) {
            PeripheralSettings peripheralSettings = (PeripheralSettings) objects[0];
            Location location = (Location) objects[1];
            sendEmailAlert3(peripheralSettings, location);
            return null;
        }
    }

    private class SendTwitterAlertTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... objects) {
            String peripheralAddress = (String) objects[0];
            sendTwitterAlert(peripheralAddress);
            return null;
        }
    }

    private class SendFacebookAlertTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... objects) {
            String peripheralAddress = (String) objects[0];
        //    sendFacebookAlert(peripheralAddress);
            return null;
        }
    }

    public void sendExternalAlerts(String peripheralAddress) {
        sendEmailAlert(peripheralAddress);
        sendFacebookAlert(peripheralAddress);

        SendFacebookAlertTask sendFacebookAlertTask = new SendFacebookAlertTask();
        sendFacebookAlertTask.execute(peripheralAddress);

        SendTwitterAlertTask sendTwitterAlertTask = new SendTwitterAlertTask();
        sendTwitterAlertTask.execute(peripheralAddress);
    }

    public void sendFacebookAlert(String peripheralAddress) {
//        Log.e(LOG_TAG, "send Facebook Alert");
//        Session session = Session.getActiveSession();
//
//        if (session != null){
//
//            // Check for publish permissions
//            List<String> permissions = session.getPermissions();
//
//
//            Bundle postParams = new Bundle();
//            postParams.putString("name", "Facebook for Android");
//            postParams.putString("caption", "Build great social apps and get more installs.");
//            postParams.putString("description", "The Facebook SDK for Android makes it easier and faster to develop Facebook integrated Android apps.");
//            postParams.putString("link", "https://developers.facebook.com/android");
//
//            Request.Callback callback= new Request.Callback() {
//                public void onCompleted(Response response) {
//                    JSONObject graphResponse = response
//                            .getGraphObject()
//                            .getInnerJSONObject();
//                    String postId = null;
//                    try {
//                        postId = graphResponse.getString("id");
//                    } catch (JSONException e) {
//                        Log.i(LOG_TAG,
//                                "JSON error "+ e.getMessage());
//                    }
//                    FacebookRequestError error = response.getError();
//
//                }
//            };
//
//            Request request = new Request(session, "me/feed", postParams,
//                    HttpMethod.POST, callback);
//
//            RequestAsyncTask task = new RequestAsyncTask(request);
//            task.execute();
//        }
    }
    private boolean isSubsetOf(Collection<String> subset, Collection<String> superset) {
        for (String string : subset) {
            if (!superset.contains(string)) {
                return false;
            }
        }
        return true;
    }

    public void sendTwitterAlert(String peripheralAddress) {
        Log.v(LOG_TAG, "sendTwitterAlert: " + peripheralAddress);
        Status status = null;
        //if the setting is on, and we have auth tokens
        // (account may have been deleted after setting was established by UI)
        //then we tweet
        String[] twitterAuthTokens = getExternalAlertSettings().getTwitterTokenAndSecret();
        Log.v(LOG_TAG, "twitter auth tokens: " + twitterAuthTokens[0] + ", " + twitterAuthTokens[1]);

        if ( getExternalAlertSettings().isTwitterAlertOn() && twitterAuthTokens[0] != null && twitterAuthTokens[1] != null) {

            AccessToken accessToken = new AccessToken(twitterAuthTokens[0], twitterAuthTokens[1]);
            Twitter twitter = new TwitterFactory().getInstance();
            twitter.setOAuthConsumer(Constant.CONSUMER_KEY, Constant.CONSUMER_SECRET);
            twitter.setOAuthAccessToken(accessToken);

            ItemTrackerPersistenceHelper persistenceHelper = ItemTrackerPersistenceHelper.getInstance(context);
            Location location = persistenceHelper.getMostRecentLocation(peripheralAddress);
            if(location != null)
            {
            String twitterMessage = MessageFormat.format("@phonehalo inSite Alert: Item Lost at https://maps.google.com/?ll={0},{1}&spn=0.001376,0.00284&t=m&z=19&markers=color:blue%7Clabel:inSite%7C{0},{1}, -- {2}",
                    location.getLatitude(), location.getLongitude(), new Random(System.currentTimeMillis()).nextInt());
            StatusUpdate statusUpdate = new StatusUpdate(twitterMessage);


            statusUpdate.setLocation(new GeoLocation(location.getLatitude(), location.getLongitude()));

            statusUpdate.setDisplayCoordinates(true);
            try {

                Log.v(LOG_TAG, "attempting to tweet: " + statusUpdate);
                status = twitter.updateStatus(statusUpdate);
                Log.v(LOG_TAG, "Tweet Success");
            } catch (TwitterException e) {
                Log.e(LOG_TAG, "Tweet Failure", e);
            }
            }
        }
        Log.v(LOG_TAG, "Tweet Status: " + status);
    }

}
