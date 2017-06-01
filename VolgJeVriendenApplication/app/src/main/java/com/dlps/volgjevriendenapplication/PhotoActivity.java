package com.dlps.volgjevriendenapplication;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;

import static android.R.attr.rotation;
import static android.graphics.Bitmap.createBitmap;
import static java.lang.Thread.sleep;

/**
 * Created by pim on 31-5-17.
 */

public class PhotoActivity extends AppCompatActivity{
    public static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        System.out.println("From constructor: "+DataHolder.getInstance().getPhonenumber());
        //setContentView(R.layout.activity_photo);

        if(savedInstanceState == null) {
            dispatchTakePictureIntent();
        }
        else {
            DataHolder.getInstance().setContext(this);
            DataHolder.getInstance().setLocationUpdater(new LocationUpdater());
            DataHolder.getInstance().setPassword(savedInstanceState.getString("password"));
            DataHolder.getInstance().setPhonenumber(savedInstanceState.getString("pid"));
        }

        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("pid", DataHolder.getInstance().getPhonenumber());
        outState.putString("password", DataHolder.getInstance().getPassword());

        super.onSaveInstanceState(outState);
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
            if(android.os.Build.MANUFACTURER.equals("LGE")) {
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                imageBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getHeight(), matrix, true);
            }

            final JSONObject json = new JSONObject();
            try{
                System.out.println(DataHolder.getInstance().getPhonenumber());
                System.out.println(DataHolder.getInstance().getLocationUpdater());
                json.put("pid", DataHolder.getInstance().getPhonenumber());
                json.put("password", DataHolder.getInstance().getPassword());
                json.put("image", BitmapBase64Coder.encodeTobase64(imageBitmap));
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

    /*private Bitmap rotateIfRequired(Uri ) {
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(uri.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int rotationInDegrees = exifToDegrees(rotation);

        Matrix matrix = new Matrix();
        if (rotation != 0f) {matrix.preRotate(rotationInDegrees);}

        return createBitmap(bitmap , 0, 0, bitmap .getWidth(), bitmap .getHeight(), matrix, true);
    }

    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) { return 90; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {  return 180; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {  return 270; }
        return 0;
    }*/
}
