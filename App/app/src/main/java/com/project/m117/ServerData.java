package com.project.m117;

import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

//Run this to connect
//                ServerData s = new ServerData();
//                s.execute("URL");
//                try {s.get();} catch (Exception e) {}
//  get {uID, human, name, health, attack, defense, resource, locX, locY}
//  from the String users[][]


public abstract class ServerData extends AsyncTask<String, Void, List<String>> {

    public abstract void doAtEnd(List<String> result);

    protected List<String> doInBackground(String... addresses) {
        try {
            URL url = new URL(addresses[0]);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            InputStream in = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            List<String> lines = new ArrayList<String>();
            String line = new String();
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            conn.disconnect();
            return lines;
        } catch(Exception e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<String> result) {
        super.onPostExecute(result);
        doAtEnd(result);
    }
}