package com.dlps.volgjevriendenapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

/**
 * Encodes bitmaps to base64 strings and decodes base64 strings to bitmaps
 * Created by pim on 31-5-17.
 */

public class BitmapBase64Coder {
    /**
     * Encodes an image to a base64 string
     * @param image the image to be encoded
     * @return the base64 string of the image
     */
    public static String encodeTobase64(Bitmap image)
    {
        Bitmap immagex=image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immagex.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b,Base64.NO_WRAP);
        return imageEncoded;
    }

    /**
     * Decodes a base64 string to a bitmap
     * @param input the base64 string to be decoded
     * @return the bitmap resulting from the base84 string
     */
    public static Bitmap decodeBase64(String input)
    {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }
}
