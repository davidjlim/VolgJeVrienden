package com.dlps.volgjevriendenapplication;

/**
 * Created by pim on 30-5-17.
 */

public class SharedDataHolder {
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocationUpdater getLocationUpdater() {
        return locationUpdater;
    }

    public void setLocationUpdater(LocationUpdater locationUpdater) {
        this.locationUpdater = locationUpdater;
    }

    private String password;
    private LocationUpdater locationUpdater;

    private static final SharedDataHolder holder = new SharedDataHolder();
    public static SharedDataHolder getInstance() {return holder;}
}
