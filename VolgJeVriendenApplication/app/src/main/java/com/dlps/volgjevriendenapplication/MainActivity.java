package com.dlps.volgjevriendenapplication;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Looper;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.params.HttpConnectionParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static android.R.attr.path;
import static android.provider.ContactsContract.CommonDataKinds.Website.URL;
import static java.net.Proxy.Type.HTTP;

public class MainActivity extends AppCompatActivity {
    private UserLoginTask mAuthTask = null;

    private EditText mPhonenumberView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    public static final int HTTP_NO_CONNECTION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mPhonenumberView = (EditText) findViewById(R.id.phonenumber);
        mPasswordView = (EditText) findViewById(R.id.password);

        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        LocationUpdater locationUpdater = new LocationUpdater();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,locationUpdater);
        SharedDataHolder.getInstance().setLocationUpdater(locationUpdater);
    }

    public void signin(View view){
        if (mAuthTask != null) {
            return;
        }

        String phonenumber = mPhonenumberView.getText().toString();
        String password = sha1(mPasswordView.getText().toString());

        mAuthTask = new UserLoginTask(phonenumber, password, true);
        //mAuthTask.execute((Void) null);
        mAuthTask.execute();
    }

    private static String sha1(String s)
    {
        MessageDigest digest;
        try
        {
            digest = MessageDigest.getInstance("SHA-1");
            digest.update(s.getBytes(Charset.forName("US-ASCII")),0,s.length());
            byte[] magnitude = digest.digest();
            BigInteger bi = new BigInteger(1, magnitude);
            String hash = String.format("%0" + (magnitude.length << 1) + "x", bi);
            return hash;
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        return "";
    }

    public void signup(View view){
        if (mAuthTask != null) {
            return;
        }

        String phonenumber = mPhonenumberView.getText().toString();
        String password = sha1(mPasswordView.getText().toString());

        mAuthTask = new UserLoginTask(phonenumber, password, false);
        //mAuthTask.execute((Void) null);
        mAuthTask.execute();
    }

    public void startMap(){
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Integer> {

        private final String mPhonenumber;
        private final String mPassword;
        private final Boolean mSignin;

        UserLoginTask(String phonenumber, String password, Boolean signin) {
            mPhonenumber = phonenumber;
            mPassword = password;
            mSignin = signin;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                String url=getString(mSignin ? R.string.signin_url : R.string.signup_url);
                URL object=new URL(url);

                HttpURLConnection con = (HttpURLConnection) object.openConnection();
                con.setDoOutput(true);
                con.setDoInput(true);
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Accept", "application/json");
                con.setRequestMethod("POST");

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("pid", mPhonenumber);
                jsonParam.put("password", mPassword);

                OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
                wr.write(jsonParam.toString());
                wr.flush();

//display what returns the POST request

                StringBuilder sb = new StringBuilder();
                Integer HttpResult = con.getResponseCode();
                if (HttpResult == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(con.getInputStream(), "utf-8"));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    System.out.println("" + sb.toString());
                } else {
                    System.out.println(con.getResponseMessage());
                }

                return HttpResult;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return HTTP_NO_CONNECTION;
        }

        @Override
        protected void onPostExecute(final Integer HttpResult) {
            mAuthTask = null;
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                SharedDataHolder.getInstance().setPassword(mPassword);
                startMap();
                finish();
                return;
            }

            CharSequence text;
            switch(HttpResult){
                case HTTP_NO_CONNECTION: text = getString(R.string.no_connection); break;
                case HttpURLConnection.HTTP_NOT_FOUND : text = getString(R.string.user_not_found); break;
                case HttpURLConnection.HTTP_UNAUTHORIZED : text = getString(R.string.incorrect_password); break;
                case HttpURLConnection.HTTP_BAD_REQUEST : text = getString(R.string.phonenumber_taken); break;
                default: text = getString(R.string.something_went_wrong);
            }

            Context context = getApplicationContext();
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            //showProgress(false);
        }
    }
}
