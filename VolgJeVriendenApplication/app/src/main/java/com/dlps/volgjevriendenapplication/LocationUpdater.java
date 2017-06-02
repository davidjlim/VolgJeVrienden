package com.dlps.volgjevriendenapplication;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Updates the user's location
 * Created by pim on 30-5-17.
 */

public class LocationUpdater implements android.location.LocationListener {
    /**
     * The user's current location
     */
    private Location currentLocation;
    /**
     * The locationmanager, communicates with the phone's location sensors
     */
    private LocationManager mLocationManager;

    /**
     * Constructor
     */
    public LocationUpdater() {
        LocationManager locationManager = (LocationManager)DataHolder.getInstance().getContext().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,this);
        currentLocation = getLastKnownLocation();
        updateGPS();
    }

    /**
     * To get the user's last known location
     * @return the user's last known location
     */
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

    /**
     * When the location changes
     * @param location new location
     */
    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        updateGPS();
    }

    /**
     * Sends the new GPS coordinates to the server
     */
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

    /**
     * Unused interface method
     * @param provider
     * @param status
     * @param extras
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    /**
     * Unused interface method
     * @param provider
     */
    @Override
    public void onProviderEnabled(String provider) {

    }

    /**
     * Unused interface method
     * @param provider
     */
    @Override
    public void onProviderDisabled(String provider) {

    }

}
