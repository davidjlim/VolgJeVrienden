package com.dlps.volgjevriendenapplication;

import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;

/**
 * The friends activity displays a list of all friends
 */

public class FriendsActivity extends AppCompatActivity {
    /**
     * The JSONArray with all requests to the user
     */
    JSONArray friends;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

        final String url = getString(R.string.ip_address) + getString(R.string.get_friends_url);
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

                friends = null;
                try {
                    friends = new JSONArray(httpResultMessage.getHttpMessage());
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.requests_menu, menu);
        return true;
    }

    /**
     * When a connectionerror has occurred
     */
    private void errorInternet() {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(FriendsActivity.this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Draws all the entries in the list
     */
    public void drawElements(){
        ArrayList<String> list = new ArrayList<String>();
        for(int i=0; i<friends.length(); i++){
            try {
                JSONObject friend = friends.getJSONObject(i);
                list.add(friend.getString("pid"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        //instantiate custom adapter
        FriendListAdapter adapter = new FriendListAdapter(list, FriendsActivity.this);

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
            case R.id.action_add:
                addRequest();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void addRequest() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
// ...Irrelevant code for customizing the buttons and title
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_request_editor, null);
        dialogBuilder.setView(dialogView);

        final EditText editText = (EditText) dialogView.findViewById(R.id.phonenumber);
        editText.setHint("Enter the phonenumber");

        // set dialog message
        dialogBuilder.setCancelable(false).setPositiveButton("Send", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                System.out.println("The edit text is " + editText.getText());
            }
        });

        // create alert dialog
        AlertDialog alertDialog = dialogBuilder.create();
        // show it
        alertDialog.show();
    }
}