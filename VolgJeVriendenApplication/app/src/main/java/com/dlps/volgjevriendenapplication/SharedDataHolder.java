package com.dlps.volgjevriendenapplication;

import android.content.Context;

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

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    private String phonenumber;
    private String password;
    private LocationUpdater locationUpdater;

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    private Context context;

    private static final SharedDataHolder holder = new SharedDataHolder();
    public static SharedDataHolder getInstance() {return holder;}
}
