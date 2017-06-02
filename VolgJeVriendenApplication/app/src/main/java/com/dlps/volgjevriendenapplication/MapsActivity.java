package com.dlps.volgjevriendenapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Collection;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final long TIME_BETWEEN_REFRESH = 10000;
    private GoogleMap mMap;
    private Boolean locationUnknown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }

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

        setCurrentVisibility();
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
        } else if(id == R.id.action_friends) {
            Intent intent = new Intent(this, FriendsActivity.class);
            startActivity(intent);
            return true;
        }
        else if(id == R.id.action_logout) {
            finish();
            setResult(R.id.action_logout);
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
            Boolean zoomIn = true;
            @Override
            public void run() {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        findFriends(zoomIn);
                        zoomIn = false;
                        return null;
                    }
                }.execute();
                handler.postDelayed(this, TIME_BETWEEN_REFRESH);
            }
        };
        handler.postDelayed(myRunnable, 0);

        if(DataHolder.getInstance().getLocationUpdater() == null)
            DataHolder.getInstance().setLocationUpdater(new LocationUpdater());
        Location currentLocation = DataHolder.getInstance().getLocationUpdater().getLastKnownLocation();
        if(currentLocation != null) {
            locationUnknown = false;
        }
        else{
            errorLocation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                findFriends(true);
                return null;
            }
        }.execute();
    }

    public void findFriends(final Boolean zoomIn){
        final JSONObject login = new JSONObject();
        try {
            login.put("pid", DataHolder.getInstance().getPhonenumber());
            login.put("password", DataHolder.getInstance().getPassword());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final HttpResultMessage result = ServerConnector.postRequest(getString(R.string.ip_address)
                + getString(R.string.get_friends_url), login);

        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    drawScreen(new JSONArray(result.getHttpMessage()), zoomIn);
                } catch (JSONException e) {
                    Toast.makeText(DataHolder.getInstance().getContext(), getString(R.string.no_connection), Toast.LENGTH_SHORT);
                }
            }
        });
    }

    private void drawScreen(JSONArray friends, Boolean zoomIn){
        mMap.clear();
        Collection<Marker> markers = new ArrayList<>();

        try {
            for(int i=0; i<friends.length(); i++){
                JSONObject friend = friends.getJSONObject(i);
                if(friend.isNull("gpsLat") || friend.isNull("gpsLong")){
                    System.out.println("Location not found");
                    continue;
                }
                double friendLat = friend.optDouble("gpsLat");
                double friendLong = friend.optDouble("gpsLong");
                LatLng friendLatLng = new LatLng(friendLat, friendLong);
                Marker marker = mMap.addMarker(new MarkerOptions().position(friendLatLng)
                        .title(friend.getString("pid"))
                        .icon(friend.isNull("image") ? BitmapDescriptorFactory.defaultMarker() :
                                BitmapDescriptorFactory.fromBitmap(
                                        BitmapBase64Coder.decodeBase64(friend.optString("image")))));
                markers.add(marker);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Location currentLocation = DataHolder.getInstance().getLocationUpdater().getLastKnownLocation();
        if(currentLocation != null) {
            locationUnknown = false;
            LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            Marker marker = mMap.addMarker(new MarkerOptions().position(currentLatLng)
                    .title("This is you!")
                    .zIndex(1.0f));
            markers.add(marker);
        }
        else {
            errorLocation();
        }

        if(zoomIn) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Marker marker : markers) {
                builder.include(marker.getPosition());
            }
            LatLngBounds bounds = builder.build();
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));
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

    private void setCurrentVisibility() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                String url = getString(R.string.ip_address) + getString(R.string.get_visibility);
                JSONObject json = new JSONObject();
                try {
                    json.put("pid", DataHolder.getInstance().getPhonenumber());
                    json.put("password", DataHolder.getInstance().getPassword());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                HttpResultMessage httpResultMessage = ServerConnector.postRequest(url, json);
                JSONObject jsonVisibility = null;
                try {
                    jsonVisibility = new JSONObject(httpResultMessage.getHttpMessage());
                    Boolean visibility = jsonVisibility.getInt("visibility") == 1;
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MapsActivity.this);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putBoolean("switch_visibility", visibility);
                    editor.apply();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }
}
