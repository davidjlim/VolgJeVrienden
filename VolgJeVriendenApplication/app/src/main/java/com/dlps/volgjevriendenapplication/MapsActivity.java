package com.dlps.volgjevriendenapplication;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final long TIME_BETWEEN_REFRESH = 10000;
    private GoogleMap mMap;
    private Boolean locationUnknown = false;
    Marker ownMarker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.showOverflowMenu();
        setSupportActionBar(myToolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if(id == R.id.action_requests) {
            Intent intent = new Intent(this, RequestsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        final Handler handler = new Handler();
        final Runnable myRunnable = new Runnable(){
            @Override
            public void run() {
                drawScreen();
                handler.postDelayed(this, TIME_BETWEEN_REFRESH);
            }
        };
        handler.postDelayed(myRunnable, 0);

        Location currentLocation = DataHolder.getInstance().getLocationUpdater().getLastKnownLocation();
        if(currentLocation != null) {
            locationUnknown = false;
            LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
        }
        else{
            errorLocation();
        }
    }

    public void drawScreen(){
        mMap.clear();
        Location currentLocation = DataHolder.getInstance().getLocationUpdater().getLastKnownLocation();
        JSONObject login = new JSONObject();
        try {
            login.put("pid", DataHolder.getInstance().getPhonenumber());
            login.put("password", DataHolder.getInstance().getPassword());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HttpResultMessage result = ServerConnector.postRequest(getString(R.string.ip_address)
                        + getString(R.string.getFriends_url), login);
        System.out.println(result.getHttpMessage());
        try {
            JSONArray friends = new JSONArray(result.getHttpMessage());
            for(int i=0; i<friends.length(); i++){
                JSONObject friend = friends.getJSONObject(i);
                Double friendLat = friend.optDouble("gpsLat");
                Double friendLong = friend.optDouble("gpsLong");
                if(friendLat == null || friendLong == null) {
                    System.out.println("No Location found");
                    continue;
                }
                LatLng friendLatLng = new LatLng(friendLat, friend.getDouble("gpsLong"));
                mMap.addMarker(new MarkerOptions().position(friendLatLng).title(friend.getString("pid")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(currentLocation != null) {
            locationUnknown = false;
            LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            ownMarker = mMap.addMarker(new MarkerOptions().position(currentLatLng).title("This is you!"));
        }
        else {
            errorLocation();
        }
    }

    private void errorLocation() {
        if(locationUnknown)
            return;
        locationUnknown = true;

        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, R.string.location_unknown, duration);
        toast.show();
    }
}
