package com.dlps.volgjevriendenapplication;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final long TIME_BETWEEN_REFRESH = 1000;
    private GoogleMap mMap;
    Marker ownMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
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

        mMap.moveCamera(CameraUpdateFactory.newLatLng(ownMarker.getPosition()));
    }

    public void drawScreen(){
        mMap.
        Location currentLocation = SharedDataHolder.getInstance().getLocationUpdater().getCurrentLocation();
        LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        ownMarker = mMap.addMarker(new MarkerOptions().position(currentLatLng).title("This is you!"));
    }
}
