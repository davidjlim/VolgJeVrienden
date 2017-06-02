package com.dlps.volgjevriendenapplication;

import android.content.Context;

/**
 * Singleton Object, used to hold on to information for the app to easily access,
 * such as the user's phonenumber and passwordhash, the locationupdater etc.
 * Created by pim on 31-5-17.
 */

public class DataHolder {
    /**
     * Gets the held password
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the held password
     *
     * @param password the password to be held
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the held location updater
     *
     * @return the held locationupdater
     */
    public LocationUpdater getLocationUpdater() {
        return locationUpdater;
    }

    /**
     * Sets the held location updater
     *
     * @param locationUpdater the locationupdater to be held
     */
    public void setLocationUpdater(LocationUpdater locationUpdater) {
        this.locationUpdater = locationUpdater;
    }

    /**
     * Gets the held phonenumber
     *
     * @return the held phonenumber
     */
    public String getPhonenumber() {
        return phonenumber;
    }

    /**
     * Sets the held phonenumber
     *
     * @param phonenumber the phonenumber to be held
     */
    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    /**
     * The held phonenumber
     */
    private String phonenumber;
    /**
     * The held password
     */
    private String password;
    /**
     * The held locationupdater
     */
    private LocationUpdater locationUpdater;

    /**
     * Gets the held context
     * @return the held context
     */
    public Context getContext() {
        return context;
    }

    /**
     * Sets the held contexr
     * @param context the context to be held
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * The held context
     */
    private Context context;

    /**
     * The single instance of the DataHolder
     */
    private static final DataHolder ourInstance = new DataHolder();

    /**
     * To get the instance of the DataHolder
     * @return the one DataHolder
     */
    public static DataHolder getInstance() {
        return ourInstance;
    }

    /**
     * Private Constructor
     */
    private DataHolder() {
    }
}
