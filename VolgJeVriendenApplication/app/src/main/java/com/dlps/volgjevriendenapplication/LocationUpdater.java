package com.dlps.volgjevriendenapplication;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by pim on 30-5-17.
 */

public class LocationUpdater implements android.location.LocationListener {
    private static final long MIN_TIME_BW_UPDATES = 0;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    private Location currentLocation;
    private LocationManager mLocationManager;

    public LocationUpdater() {
        LocationManager locationManager = (LocationManager)DataHolder.getInstance().getContext().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,this);
        currentLocation = getLastKnownLocation();
        updateGPS();
    }

    public Location getLastKnownLocation() {
        mLocationManager = (LocationManager)DataHolder.getInstance().getContext()
                .getSystemService(DataHolder.getInstance().getContext().LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        updateGPS();
    }

    public void updateGPS(){
        if(DataHolder.getInstance().getPhonenumber() == null)
            return;

        System.out.printf("updating ...");
        if(currentLocation == null)
            return;
        System.out.println("still updating...");

        String url = DataHolder.getInstance().getContext().getResources().getString(R.string.ip_address) +
                DataHolder.getInstance().getContext().getResources().getString(R.string.update_GPS_url);
        System.out.println(url);
        JSONObject json = new JSONObject();
        try {
            json.put("pid", DataHolder.getInstance().getPhonenumber());
            json.put("password", DataHolder.getInstance().getPassword());
            json.put("gpsLong", currentLocation.getLongitude());
            json.put("gpsLat", currentLocation.getLatitude());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ServerConnector.postRequest(url, json);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

}
