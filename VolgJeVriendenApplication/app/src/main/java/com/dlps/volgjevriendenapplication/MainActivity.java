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

/**
 *  The app starts in a login screen.
 */
public class MainActivity extends AppCompatActivity {
    private static final int MIN_PASSWORD_LENGTH = 1;
    private static final int MAX_PASSWORD_LENGTH = 20;
    private static final int MIN_PHONENUMBER_LENGTH = 1;
    private static final int MAX_PHONENUMBER_LENGTH = 20;
    /**
     * The task that handles the authentication
     */
    private UserLoginTask mAuthTask = null;

    /**
     * The editview in which the user is to enter his phonenumber
     */
    private EditText mPhonenumberView;
    /**
     * The editview in which the user is to enter his password
     */
    private EditText mPasswordView;

    /**
     * When the activity is created, creates the screen etc.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPhonenumberView = (EditText) findViewById(R.id.phonenumber);
        mPasswordView = (EditText) findViewById(R.id.password);

        reset();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setTitle("Log in");
        myToolbar.showOverflowMenu();
        setSupportActionBar(myToolbar);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
    }


    /**
     * Starts the signin process, passes the UserLoginTask the entered pid and hashed password
     * @param view the button which was pressed
     */
    public void signin(View view){
        signinup(true);
    }

    /**
     * Starts the signup process, passes the UserLoginTask the entered pid and hashed password
     * @param view the button which was pressed
     */
    public void signup(View view){
        signinup(false);
    }

    /**
     * Sign in or up, depending on the parametr
     * @param signin true iff you want to signin
     */
    public void signinup(Boolean signin){

        if (mAuthTask != null) {
            return;
        }

        String phonenumber = mPhonenumberView.getText().toString();
        String password = mPasswordView.getText().toString();
        if(!checkPhonenumber(phonenumber) || !checkPassword(password)) {
            System.out.println(checkPhonenumber(phonenumber));
            System.out.println(checkPassword(password));
            Toast.makeText(this, R.string.invalid_data_length, Toast.LENGTH_SHORT).show();
            return;
        }

        mAuthTask = new UserLoginTask(phonenumber, sha1(password), signin);
        mAuthTask.execute();
    }

    private boolean checkPassword(String password) {
        return (password.length() >= MIN_PASSWORD_LENGTH) &&
                (password.length() <= MAX_PASSWORD_LENGTH);
    }

    private boolean checkPhonenumber(String phonenumber) {
        return (phonenumber.length() >= MIN_PHONENUMBER_LENGTH) &&
                (phonenumber.length() <= MAX_PHONENUMBER_LENGTH);
    }

    /**
     * hashes a string
     * @param s the string to be hashed
     * @return the hashed string as string
     */
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

    /**
     * Starts the map activity
     */
    public void startMap(){
        Intent intent = new Intent(this, MapsActivity.class);
        startActivityForResult(intent, 0);
    }

    /**
     * When the user logs out and returns to the login screen, readies the login screen
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPhonenumberView = (EditText) findViewById(R.id.phonenumber);
        mPasswordView = (EditText) findViewById(R.id.password);

        reset();
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Resets the data held by DataHolder
     */
    private void reset() {
        DataHolder.getInstance().setContext(this);
        DataHolder.getInstance().setPassword(null);
        DataHolder.getInstance().setPhonenumber(null);
        DataHolder.getInstance().setLocationUpdater(null);

        mPhonenumberView.setText("");
        mPasswordView.setText("");
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Integer> {
        /**
         * The user's phonenumber
         */
        private final String mPhonenumber;
        /**
         * The user's password
         */
        private final String mPassword;
        /**
         * Whether the user wishes to signup or signin
         */
        private final Boolean mSignin;

        /**
         * Constructor
         * @param phonenumber entered phonenumber
         * @param password entered password
         * @param signin determined by which button was pressed
         */
        UserLoginTask(String phonenumber, String password, Boolean signin) {
            mPhonenumber = phonenumber;
            mPassword = password;
            mSignin = signin;
        }

        /**
         * The process of authenticating the user/
         * @param params
         * @return the resulting Http code
         */
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

        /**
         * Performed after the UserLoginTask is finished authenticating
         * @param HttpResult
         */
        @Override
        protected void onPostExecute(final Integer HttpResult) {
            mAuthTask = null;
            if (HttpResult == HttpURLConnection.HTTP_OK) {
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
                case HttpURLConnection.HTTP_UNAUTHORIZED : text = getString(R.string.error_incorrect_password); break;
                case HttpURLConnection.HTTP_BAD_REQUEST : text = getString(R.string.phonenumber_taken); break;
                default: text = getString(R.string.something_went_wrong);
            }

            Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
        }

        /**
         * When the task is cancelled
         */
        @Override
        protected void onCancelled() {
            mAuthTask = null;
            //showProgress(false);
        }
    }
}
