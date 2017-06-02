package com.dlps.volgjevriendenapplication;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {
    private UserLoginTask mAuthTask = null;

    private EditText mPhonenumberView;
    private EditText mPasswordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }

        reset();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mPhonenumberView = (EditText) findViewById(R.id.phonenumber);
        mPasswordView = (EditText) findViewById(R.id.password);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setTitle("Log in");
        myToolbar.showOverflowMenu();
        setSupportActionBar(myToolbar);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
    }

    public void signin(View view){
        System.out.println("Signing in...");
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

    private void reset() {
        DataHolder.getInstance().setContext(this);
        DataHolder.getInstance().setPassword(null);
        DataHolder.getInstance().setPhonenumber(null);
        DataHolder.getInstance().setLocationUpdater(null);
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
            JSONObject json = new JSONObject();
            try {
                json.put("pid", mPhonenumber);
                json.put("password", mPassword);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String url=getString(R.string.ip_address) + getString(mSignin ? R.string.signin_url : R.string.signup_url);

            return ServerConnector.postRequest(url,json).getHttpResult();
        }

        @Override
        protected void onPostExecute(final Integer HttpResult) {
            mAuthTask = null;
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                mPhonenumberView.setText("");
                mPasswordView.setText("");

                DataHolder.getInstance().setPassword(mPassword);
                DataHolder.getInstance().setPhonenumber(mPhonenumber);
                DataHolder.getInstance().setLocationUpdater(new LocationUpdater());
                startMap();
                return;
            }

            CharSequence text;
            switch(HttpResult){
                case ServerConnector.HTTP_NO_CONNECTION: text = getString(R.string.no_connection); break;
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
