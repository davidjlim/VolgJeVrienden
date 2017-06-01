package com.dlps.volgjevriendenapplication;


import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;

public class RequestsActivity extends ListActivity {
    String[] listItems = {"exploring", "android",
            "list", "activities"};
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

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
        list.add("item1");
        list.add("item2");

        //instantiate custom adapter
        RequestListAdapter adapter = new RequestListAdapter(list, RequestsActivity.this);

        //handle listview and assign adapter
        ListView lView = (ListView)findViewById(android.R.id.list);
        lView.setAdapter(adapter);
    }
}