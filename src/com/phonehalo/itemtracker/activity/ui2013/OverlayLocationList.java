package com.phonehalo.itemtracker.activity.ui2013;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import com.phonehalo.itemtracker.R;
import com.phonehalo.itemtracker.utility.Log;

public class OverlayLocationList extends Activity {
    private static final String LOG_TAG = "OverlayLocationListActivity";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui2013_overlay_location_list_layout);
    }

    public void onClick(View view) {
        Log.d(LOG_TAG, "onClick: view id:" + view.getId());
        switch (view.getId()) {
            case R.id.overlayLocationList:
                finish();
                break;
            case R.id.buttonOverlayLocationToMap:
                finish();
                break;
            default:
                break;
        }
    }
}