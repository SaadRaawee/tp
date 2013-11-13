package com.phonehalo.itemtracker.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.google.api.client.auth.oauth.*;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.phonehalo.itemtracker.Constant;
import com.phonehalo.itemtracker.db.ExternalAlertSettings;
import com.phonehalo.itemtracker.db.ItemTrackerPersistenceHelper;
import com.phonehalo.itemtracker.utility.Log;

import java.io.IOException;

@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
public class TwitterAuthActivity extends Activity {
    private static final String LOG_TAG = "TwitterAuthActivity";
    final OAuthHmacSigner signer = new OAuthHmacSigner();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        WebView webview = new WebView(this);
        webview.setWebViewClient(new TwitterAuthWebViewClient());
        webview.getSettings().setJavaScriptEnabled(true);
        webview.setVisibility(View.VISIBLE);
        setContentView(webview);

        LoadUrlTask loadUrlTask = new LoadUrlTask();
        loadUrlTask.execute(webview);
    }

    private class LoadUrlTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... objects) {
            final WebView webview = (WebView) objects[0];

            signer.clientSharedSecret = Constant.CONSUMER_SECRET;

            OAuthGetTemporaryToken temporaryToken = new OAuthGetTemporaryToken(Constant.REQUEST_URL);
            temporaryToken.transport = new ApacheHttpTransport();
            temporaryToken.signer = signer;
            temporaryToken.consumerKey = Constant.CONSUMER_KEY;
            temporaryToken.callback = Constant.OAUTH_CALLBACK_URL;

            try {

                OAuthCredentialsResponse tempCredentials = temporaryToken.execute();
                signer.tokenSharedSecret = tempCredentials.tokenSecret;

                OAuthAuthorizeTemporaryTokenUrl authorizeUrl = new OAuthAuthorizeTemporaryTokenUrl(Constant.AUTHORIZE_URL);
                authorizeUrl.temporaryToken = tempCredentials.token;
                final String authorizationUrl = authorizeUrl.build();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        webview.loadUrl(authorizationUrl);

                    }
                });
            } catch (IOException e) {
                Log.e(LOG_TAG, "Could not create temporary OAuth credentials", e);
            }
            return null;
        }
    }

    private class RetrieveOauthTokensTask extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... objects) {
            Log.v(LOG_TAG, "RetrieveOauthTokensTask.doInBackground");
            String url = (String) objects[0];
            Uri uri = Uri.parse(url);
            String requestToken = uri.getQueryParameter("oauth_token");
            String verifier = uri.getQueryParameter("oauth_verifier");

            signer.clientSharedSecret = Constant.CONSUMER_SECRET;

            OAuthGetAccessToken accessToken = new OAuthGetAccessToken(Constant.ACCESS_URL);
            accessToken.transport = new ApacheHttpTransport();
            accessToken.temporaryToken = requestToken;
            accessToken.signer = signer;
            accessToken.consumerKey = Constant.CONSUMER_KEY;
            accessToken.verifier = verifier;

            try {
                OAuthCredentialsResponse credentials = accessToken.execute();
                signer.tokenSharedSecret = credentials.tokenSecret;

                ItemTrackerPersistenceHelper persistenceHelper = ItemTrackerPersistenceHelper.getInstance(getApplicationContext());
                ExternalAlertSettings externalAlertSettings = persistenceHelper.getExternalAlertSettings();
                externalAlertSettings.setTwitterTokenAndSecret(credentials.token, credentials.tokenSecret);
                persistenceHelper.persistExternalAlertSettings(externalAlertSettings);
                Log.v(LOG_TAG, "externalAlertSettings updated with tokens: " + externalAlertSettings);

            } catch (IOException e) {
                Log.e(LOG_TAG, "Failed to retrieve credentials for Twitter", e);
            }
            return null;
        }
    }

    private class TwitterAuthWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap bitmap) {
            Log.v(LOG_TAG, "onPageStarted : " + url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            Log.v(LOG_TAG, "onPageFinished : " + url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.v(LOG_TAG, "onPageFinished : " + url);
            boolean result = super.shouldOverrideUrlLoading(view, url);
            if (url.startsWith(Constant.OAUTH_CALLBACK_URL)) {

                if (url.indexOf("oauth_token=") != -1) {
                    Log.v(LOG_TAG, "Callback with oauth_token: " + url);
                    RetrieveOauthTokensTask retrieveOauthTokensTask = new RetrieveOauthTokensTask();
                    retrieveOauthTokensTask.execute(url);

                } else if (url.indexOf("error=") != -1) {
                    Log.e(LOG_TAG, "URL reports error: " + url);
                }
                result = true;
                finish();

            }
            return result;
        }
    }
}