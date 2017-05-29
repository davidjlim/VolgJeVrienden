package com.dlps.volgjevriendenapplication;

import android.content.Context;
import android.content.Intent;
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
import java.net.HttpURLConnection;
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

    public static final int HTTP_NO_CONNECTION = 0;

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
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mPhonenumberView.setError(null);
        mPasswordView.setError(null);

        String phonenumber = mPhonenumberView.getText().toString();
        String password = mPasswordView.getText().toString();

        mAuthTask = new UserLoginTask(phonenumber, password, true);
        //mAuthTask.execute((Void) null);
        mAuthTask.execute();
    }

    public void signup(View view){
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mPhonenumberView.setError(null);
        mPasswordView.setError(null);

        String phonenumber = mPhonenumberView.getText().toString();
        String password = mPasswordView.getText().toString();

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
            if(HttpResult == HTTP_NO_CONNECTION){
                Context context = getApplicationContext();
                CharSequence text = "The internet connection failed.";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }

            if (HttpResult == HttpURLConnection.HTTP_OK) {
                startMap();
                finish();
            } else {
                if(mSignin) {
                    if(HttpResult == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        mPasswordView.setError(getString(R.string.error_incorrect_password));
                        mPasswordView.requestFocus();
                    }
                }
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            //showProgress(false);
        }
    }
}
