package com.dlps.volgjevriendenapplication;


import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;

public class RequestsActivity extends AppCompatActivity {
    JSONArray requests;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

        final String url = getString(R.string.ip_address) + getString(R.string.get_requests_url);
        System.out.println(url);
        final JSONObject json = new JSONObject();
        try {
            json.put("pid", DataHolder.getInstance().getPhonenumber());
            json.put("password", DataHolder.getInstance().getPassword());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                System.out.println("Getting it...");
                HttpResultMessage httpResultMessage =  ServerConnector.postRequest(url, json);
                System.out.println(httpResultMessage.getHttpResult());
                if(httpResultMessage.getHttpResult() != HttpURLConnection.HTTP_OK){
                    System.out.println("Uh-oh...");
                    errorInternet();
                    return null;
                }
                System.out.println(httpResultMessage.getHttpMessage());

                requests = null;
                try {
                    requests = new JSONArray(httpResultMessage.getHttpMessage());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    public void run() {
                        drawElements();
                    }
                });
                return null;
            }
        }.execute();
    }

    private void errorInternet() {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(RequestsActivity.this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void drawElements(){
        ArrayList<String> list = new ArrayList<String>();
        for(int i=0; i<requests.length(); i++){
            try {
                JSONObject request = requests.getJSONObject(i);
                list.add(request.getString("pid"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        //instantiate custom adapter
        RequestListAdapter adapter = new RequestListAdapter(list, RequestsActivity.this);

        //handle listview and assign adapter
        ListView lView = (ListView)findViewById(android.R.id.list);
        lView.setAdapter(adapter);
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