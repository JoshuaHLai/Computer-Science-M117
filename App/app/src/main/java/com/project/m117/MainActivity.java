package com.project.m117;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;

import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final GlobalApplication global = (GlobalApplication)this.getApplication();

        setContentView(R.layout.activity_main);

        Button human_button = (Button) findViewById(R.id.human_button);
        Button zombie_button = (Button) findViewById(R.id.zombie_button);

        final MainActivity thisActivity = this;

        Intent intent = new Intent(this, GPSLocationService.class);
        startService(intent);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{
                        android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.INTERNET
                }, 10);
            }
        }

        human_button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(final View view) {
                Toast.makeText(getApplicationContext(), "Joining Game", Toast.LENGTH_SHORT).show();

                final AsyncTask<Void, Void, String[]> ast = new AsyncTask<Void, Void, String[]>() {
                    @Override
                    protected String[] doInBackground(Void... params) {
                        while ((global.locX > -0.0001 && global.locX < 0.0001 &&
                                global.locY > -0.0001 && global.locY < 0.0001)){
                            try {
                                Thread.sleep(1000);
                                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                            && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                                        requestPermissions(new String[]{
                                                android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                                android.Manifest.permission.INTERNET
                                        }, 10);
                                    }
                                }
                            } catch (Exception e){}
                        }
                        String x = String.valueOf(global.locX);
                        String y = String.valueOf(global.locY);
                        System.out.println(""+x + ", " + y);

                        String[] xy = {x, y};
                        return xy;
                    }

                    @Override
                    protected void onPostExecute(String[] xy) {
                        EditText username_input = (EditText) findViewById(R.id.username_input);
                        final String username = username_input.getText().toString();

                        String x = xy[0];
                        String y = xy[1];

                        ServerData sd = new ServerData() {
                            @Override
                            public void doAtEnd(List<String> result) {;}
                        };
                        String url = "";
                        try {
                            URI uri = new URI(
                                    "http",
                                    null,
                                    "23.243.209.238",
                                    9088,
                                    "/game/add",
                                    "bot&app&name=" + username_input.getText().toString()
                                            + "&x=" + x + "&y=" + y,
                                    null);
                            url = uri.toASCIIString();
                        } catch (Exception e){
                            url = "http://23.243.209.238:9088/game/add?bot&app&name="
                                    + username_input.getText().toString() + "&x=" + x + "&y=" + y;
                        }
                        System.out.println(url);
                        sd.execute(url);
                        try {
                            List<String> result = sd.get();
                            global.name = username_input.getText().toString();
                            for (String line : result) {
                                if (line.startsWith("ID: ")){
                                    global.userID = Integer.valueOf(line.substring("ID: ".length()));
                                }
                                if (line.startsWith("SESSION: ")){
                                    global.sessionID = line.substring("SESSION: ".length());
                                }
                                if (line.startsWith("TIME: ")){
                                    String time = line.substring("TIME: ".length());
                                    global.minutes = Long.valueOf(time.split(":")[0]);
                                    global.seconds = Long.valueOf(time.split(":")[1]);
                                }
                            }
                            global.human = true;
                            System.out.println("Joined Session "+global.sessionID+" as ID "+global.userID);
                            toMaps(view);
                        } catch (Exception e){
                            System.out.println(e);
                            Toast.makeText(getApplicationContext(), "Login Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                };

                ast.execute();

            }
        });

        zombie_button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(final View view) {
                System.out.println("zombie spawn");
                Toast.makeText(getApplicationContext(), "Joining Game", Toast.LENGTH_SHORT).show();
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                        requestPermissions(new String[]{
                                android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                android.Manifest.permission.INTERNET
                        }, 10);
                    }
                }

                final AsyncTask<Void, Void, String[]> ast = new AsyncTask<Void, Void, String[]>() {
                    @Override
                    protected String[] doInBackground(Void... params) {
                        while ((global.locX > -0.0001 && global.locX < 0.0001 &&
                                global.locY > -0.0001 && global.locY < 0.0001)){
                            try {
                                System.out.println("sleep");
                                Thread.sleep(1000);
                                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                            && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                                        requestPermissions(new String[]{
                                                android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                                android.Manifest.permission.INTERNET
                                        }, 10);
                                    }
                                }
                            } catch (Exception e){}
                        }
                        String x = String.valueOf(global.locX);
                        String y = String.valueOf(global.locY);
                        System.out.println(""+x + ", " + y);

                        String[] xy = {x, y};
                        return xy;
                    }

                    @Override
                    protected void onPostExecute(String[] xy) {
                        EditText username_input = (EditText) findViewById(R.id.username_input);
                        final String username = username_input.getText().toString();

                        String x = xy[0];
                        String y = xy[1];

                        ServerData sd = new ServerData() {
                            @Override
                            public void doAtEnd(List<String> result) {;}
                        };
                        String url = "";
                        try {
                            URI uri = new URI(
                                    "http",
                                    null,
                                    "23.243.209.238",
                                    9088,
                                    "/game/add",
                                    "bot&app&type=zombie&name=" + username_input.getText().toString()
                                            + "&x=" + x + "&y=" + y,
                                    null);
                            url = uri.toASCIIString();
                        } catch (Exception e){
                            url = "http://23.243.209.238:9088/game/add?bot&app&type=zombie&name="
                                    + username_input.getText().toString() + "&x=" + x + "&y=" + y;
                        }
                        System.out.println(url);
                        sd.execute(url);
                        try {
                            List<String> result = sd.get();
                            global.name = username_input.getText().toString();
                            for (String line : result) {
                                System.out.println("line");
                                if (line.startsWith("ID: ")){
                                    global.userID = Integer.valueOf(line.substring("ID: ".length()));
                                }
                                if (line.startsWith("SESSION: ")){
                                    global.sessionID = line.substring("SESSION: ".length());
                                }
                                if (line.startsWith("TIME: ")){
                                    String time = line.substring("TIME: ".length());
                                    global.minutes = Long.valueOf(time.split(":")[0]);
                                    global.seconds = Long.valueOf(time.split(":")[1]);
                                }
                            }
                            global.human = false;
                            System.out.println("Joined Session "+global.sessionID+" as ID "+global.userID);
                            toMaps(view);
                        } catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Login Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                };
                ast.execute();
            }
        });
    }
    public void toOption(View view){
        Intent option = new Intent(this, OptionsActivity.class);
        startActivity(option);
    }

    public void toMaps(View view){
        Intent map = new Intent(this, MapsActivity.class);
        startActivity(map);
    }
}
