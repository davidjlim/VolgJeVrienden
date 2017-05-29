package com.dlps.volgjevriendenapplication;

import android.os.AsyncTask;
import android.os.Looper;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import org.apache.http.params.HttpConnectionParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.net.URL;
import java.net.URLEncoder;

import static android.R.attr.path;
import static android.provider.ContactsContract.CommonDataKinds.Website.URL;
import static java.net.Proxy.Type.HTTP;

public class MainActivity extends AppCompatActivity {

    private UserLoginTask mAuthTask = null;

    private EditText mPhonenumberView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mPhonenumberView = (EditText) findViewById(R.id.phonenumber);
        mPasswordView = (EditText) findViewById(R.id.password);
    }

    public void signin(View view){
        String phonenumber = mPhonenumberView.getText().toString();
        String password = mPasswordView.getText().toString();

        mAuthTask = new UserLoginTask(phonenumber, password);
        //mAuthTask.execute((Void) null);
        mAuthTask.execute();

    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mPhonenumber;
        private final String mPassword;

        UserLoginTask(String phonenumber, String password) {
            mPhonenumber = phonenumber;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            try {
                URL url;
                URLConnection urlConn;
                DataOutputStream printout;
                DataInputStream input;
                //url = new URL (getString(R.string.signup_url));
                url = new URL (getString(R.string.signup_url));
                urlConn = url.openConnection();
                urlConn.setDoInput (true);
                urlConn.setDoOutput (true);
                urlConn.setUseCaches (false);
                urlConn.setRequestProperty("Content-Type","application/json");
                //urlConn.setRequestProperty("Host", getString(R.string.host_url));
                urlConn.connect();
//Create JSONObject here
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("pid", "+31 6 37355987");
                jsonParam.put("password", "helloAkka");

                // Send POST output.
                printout = new DataOutputStream(urlConn.getOutputStream ());
                printout.writeBytes(URLEncoder.encode(jsonParam.toString(),"UTF-8"));
                printout.flush ();
                printout.close ();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            //showProgress(false);
        }
    }
}
