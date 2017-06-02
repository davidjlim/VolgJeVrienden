package com.dlps.volgjevriendenapplication;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

/**
 * The Settings Activity, displays a menu so the user can choose to change his visibility or his photo
 */
public class SettingsActivity extends AppCompatActivity {
    static Preference switchVisibility;
    static Boolean visibility = null;

    /**
     * When the activity is created
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.pref_with_actionbar);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        DataHolder.getInstance().setContext(this);

        getFragmentManager().beginTransaction().replace(R.id.content_frame, new MyPreferenceFragment()).commit();

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
    }

    public void setVisibility(Boolean visibility) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        System.out.println("Setting to: " + !visibility);
        editor.putBoolean("switch_visibility", (!visibility));
        editor.apply();
    }

    public static class MyPreferenceFragment extends PreferenceFragment{
        @Override
        public void onCreate(final Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference);

            Preference button = findPreference(getString(R.string.button_photo));
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    System.out.println("Hoi");
                    System.out.println(DataHolder.getInstance().getPhonenumber());
                    startActivityForResult(new Intent(DataHolder.getInstance().getContext(), PhotoActivity.class), 0);
                    System.out.println("Doei");
                    return true;
                }
            });

            switchVisibility = findPreference("switch_visibility");
            switchVisibility.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                   @Override
                   public boolean onPreferenceChange(Preference preference, Object newValue) {
                       final Boolean visibility = (Boolean) newValue;
                       new AsyncTask<Void, Void, Void>() {
                           @Override
                           protected Void doInBackground(Void... params) {
                               String url = getString(R.string.ip_address) + getString(R.string.set_visibility_url);
                               JSONObject json = new JSONObject();
                               try {
                                   json.put("pid", DataHolder.getInstance().getPhonenumber());
                                   json.put("password",DataHolder.getInstance().getPassword());
                                   json.put("visibility", visibility);
                               } catch (JSONException e) {
                                   e.printStackTrace();
                               }
                               ServerConnector.postRequest(url,json);
                               return null;
                           }
                       }.execute();
                       return true;
                   }
               }
            );
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
