package com.dlps.volgjevriendenapplication;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by pim on 30-5-17.
 */

public class ServerConnector {
    public static final int HTTP_NO_CONNECTION = 0;

    public static HttpResultMessage postRequest(String url, JSONObject json){
        try {
            URL object=new URL(url);
            System.out.println("SHoi");

            HttpURLConnection con = (HttpURLConnection) object.openConnection();
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestMethod("POST");

            OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
            wr.write(json.toString());
            wr.flush();

//display what returns the POST request
            System.out.println("SHai");

            StringBuilder sb = new StringBuilder();
            Integer HttpResult = con.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                System.out.println("SHi");

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(con.getInputStream(), "utf-8"));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                return new HttpResultMessage(HttpResult, sb.toString());
            } else {
                System.out.println("SHoi");
                return new HttpResultMessage(HttpResult, "");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return new HttpResultMessage(HTTP_NO_CONNECTION,"");
    }
}
