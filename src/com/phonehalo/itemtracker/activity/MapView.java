package com.phonehalo.itemtracker.activity;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.phonehalo.itemtracker.R;
import com.phonehalo.itemtracker.db.ItemTrackerPersistenceHelper;
import com.phonehalo.itemtracker.utility.Log;

public class MapView extends FragmentActivity {

    private static final String LOG_TAG = "MapView";
    private static final float DEFAULT_ZOOM_OFFSET_BELOW_MAX_LEVEL = 3.0f;

    private static final String CONNECTION_STATUS = "com.phonehalo.itemtracker.connectionstatus";
    /**
     * Note that this may be null if the Google Play services APK is not available.
     */
    private GoogleMap googleMap;
    private LocationManager locationManager;
    private BluetoothDevice peripheral;
    private int connectionStatus;
 //   private Button backButton;



    private final LocationListener listener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location)
        {
            Log.v(LOG_TAG, "Location Changed");
            locationManager.removeUpdates(listener);

            //if(connectionStatus == 2)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 14));

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            Log.v(LOG_TAG, "Location Provider status change");
        }

        @Override
        public void onProviderEnabled(String s) {
            Log.v(LOG_TAG, "Location Provider enabled");
        }

        @Override
        public void onProviderDisabled(String s) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(LOG_TAG, "OnCreate");
        setContentView(R.layout.map_view_layout);
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                .getMap();

        googleMap.setMyLocationEnabled(true);

        if(locationManager == null)
        {
            locationManager =  (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
        Criteria criteria = new Criteria();
        Location myLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        if(myLocation != null)
        {
            double lat = myLocation.getLatitude();
            double longit = myLocation.getLongitude();
            LatLng ll = new LatLng(lat,longit);
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(ll));
        }

        locationManager.requestLocationUpdates(locationManager.getBestProvider(criteria, false), 0, 0, listener);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(LOG_TAG, "resuming with intent: " + getIntent());
        googleMap.setMyLocationEnabled(true);
        Intent intent = getIntent();
        if(intent != null)
        {
            this.peripheral = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            this.connectionStatus = intent.getIntExtra(CONNECTION_STATUS, 2);
        }
        Log.v(LOG_TAG, "connection status " + this.connectionStatus);
//        if(!peripheral.isLEDeviceConnected())
//        {
            Criteria criteria = new Criteria();
            //locationManager.requestSingleUpdate(locationManager.getBestProvider(criteria, true), listener, null);
        if(locationManager == null)
        {
            locationManager =  (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
        Location myLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        if(myLocation != null)
        {
        double lat = myLocation.getLatitude();
        double longit = myLocation.getLongitude();
        LatLng ll = new LatLng(lat,longit);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(ll));
        }


        locationManager.requestLocationUpdates(locationManager.getBestProvider(criteria, false), 0, 0, listener);

        if(this.connectionStatus == 2)
        {
            try{
            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria,true));
            if(location != null)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 14));
            }catch (IllegalArgumentException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            Location location = ItemTrackerPersistenceHelper.getInstance(getApplicationContext()).getMostRecentLocation(peripheral.getAddress());
            if(location != null)
            {
            try{
            googleMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).title(peripheral.getName()));
            CameraPosition position = new CameraPosition.Builder().target(new LatLng(location.getLatitude(), location.getLongitude())).tilt(45).zoom(20).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
            }
            catch (NullPointerException e)
            {
                e.printStackTrace();
            }
            }
        }

//        }
   //     setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #googleMap} is not null.
     * <p>
     * If it isn't installed {@link com.google.android.gms.maps.MapView
     * MapView} will show a prompt for the user to install/update the Google Play services APK on
     * their device.
     * <p>
     * A user can return to this Activity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the Activity may not have been
     * completely destroyed during this process (it is likely that it would only be stopped or
     * paused), {@link #onCreate(Bundle)} may not be called again so we should call this method in
     * {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        Log.d(LOG_TAG, "setupMapIfNeeded");

        if (googleMap == null) {
            googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            if (googleMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #googleMap} is not null.
     */
    private void setUpMap() {
        Log.d(LOG_TAG, "setUpMap");
        googleMap.setMyLocationEnabled(true);

//        Location location = getIntent().getParcelableExtra(Constant.EXTRA_LOCATION);
//        Log.v(LOG_TAG, "adding marker at location: " + location);
//        if (location != null) {
//            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//            MarkerOptions markerOptions = new MarkerOptions();
//            markerOptions.position(latLng);
//            googleMap.addMarker(markerOptions);
//
//            //zoom to the marker... lowest zoom is 2.0, highest is 21.0
//            // https://developers.google.com/maps/documentation/android/reference/com/google/android/gms/maps/CameraUpdateFactory#newLatLngZoom(com.google.android.gms.maps.model.LatLng, float)
//
//
//            float defaultZoomLevel = googleMap.getMaxZoomLevel() - DEFAULT_ZOOM_OFFSET_BELOW_MAX_LEVEL;
//            Log.v(LOG_TAG, "Updating camera to zoom level: " + defaultZoomLevel);
//            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, defaultZoomLevel);
//            googleMap.animateCamera(cameraUpdate);
//        }
    }

    private void activateSatelliteLayer() {
        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
    }

    private void activateNormalLayer() {
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    private void toggleMapLayerButton(View mapLayerButton) {
        View otherButton;
        switch (mapLayerButton.getId()) {
            case R.id.buttonIdMapLayerNormal:
                otherButton = findViewById(R.id.buttonIdMapLayerSatellite);
                mapLayerButton.setVisibility(View.GONE);
                otherButton.setVisibility(View.VISIBLE);
                activateNormalLayer();
                break;
            case R.id.buttonIdMapLayerSatellite:
                activateSatelliteLayer();
                otherButton = findViewById(R.id.buttonIdMapLayerNormal);
                mapLayerButton.setVisibility(View.GONE);
                otherButton.setVisibility(View.VISIBLE);
                break;
            default:
                Log.e(LOG_TAG,"Invalid button state");
                break;
        }
    }

    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.buttonIdMapLayerNormal:
                activateNormalLayer();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        view.setVisibility(View.GONE);
                        Button replacement = (Button)findViewById(R.id.buttonIdMapLayerSatellite);
                        replacement.setVisibility(View.VISIBLE);
                    }
                });
                break;
            case R.id.buttonIdMapLayerSatellite:
                activateSatelliteLayer();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        view.setVisibility(View.GONE);
                        Button replacement = (Button)findViewById(R.id.buttonIdMapLayerNormal);
                        replacement.setVisibility(View.VISIBLE);
                    }
                });
                break;
//            case R.id.backButton:
//                Log.e(LOG_TAG, "Back Pressed");
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//
//                    }
//                });
//                break;
            default:
                Log.e(LOG_TAG,"Invalid onClick source");
                break;
        }
    }

    @Override
    protected void onPause() {
        if(locationManager != null)
        {
            locationManager.removeUpdates(listener);
        }
        super.onPause();
    }
}