package com.dlps.volgjevriendenapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pim on 31-5-17.
 */

public class PhotoActivity extends AppCompatActivity{
    public static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        System.out.println("From constructor: "+DataHolder.getInstance().getPhonenumber());
        setContentView(R.layout.activity_photo);
        super.onCreate(savedInstanceState);
        dispatchTakePictureIntent();
    }

    private void dispatchTakePictureIntent() {
        System.out.println("From dispatch: "+DataHolder.getInstance().getPhonenumber());
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("From result: "+DataHolder.getInstance().getPhonenumber());
        System.out.println("Hallo Wereld!");
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            System.out.println("Hello World!");

            final JSONObject json = new JSONObject();
            try{
                System.out.println(DataHolder.getInstance().getPhonenumber());
                System.out.println(DataHolder.getInstance().getLocationUpdater());
                json.put("pid", DataHolder.getInstance().getPhonenumber());
                json.put("password", DataHolder.getInstance().getPassword());
                json.put("image", "hoi");//BitmapBase64Coder.encodeTobase64(imageBitmap));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            System.out.println(json.toString());
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    ServerConnector.postRequest(getString(R.string.ip_address)+getString(R.string.add_image_url), json);
                    return null;
                }
            }.execute();
            finish();
        }
        else{
            Toast.makeText(this, "Photo failed", Toast.LENGTH_SHORT).show();
        }
    }
}
