package com.project.m117;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.net.URI;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class OptionsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        final GlobalApplication global = (GlobalApplication)this.getApplication();

        Button zombie_button = (Button)findViewById(R.id.zombie_button);
        Button human_button = (Button)findViewById(R.id.human_button);

        Intent intent = new Intent(this, GPSLocationService.class);
        startService(intent);

        zombie_button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(final View view){

                Toast.makeText(getApplicationContext(), "Rejoining Game", Toast.LENGTH_SHORT).show();
                final AsyncTask<Void, Void, String[]> ast = new AsyncTask<Void, Void, String[]>() {
                    @Override
                    protected String[] doInBackground(Void... params) {
                        while (global.locX > -0.0001 && global.locX < 0.0001 &&
                                global.locY > -0.0001 && global.locY < 0.0001){
                            try {
                                Thread.sleep(1000);
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
                        final String username = global.name;

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
                                    "/game/update",
                                    "app&id=" + global.userID + "&name=" + global.name
                                            + "&x=" + x + "&y=" + y + "&human=false"
                                            + "&session=" + global.sessionID,
                                    null);
                            url = uri.toASCIIString();
                        } catch (Exception e){
                            url = "http://23.243.209.238:9088/game/update?app&human=false&id="
                                    + global.userID + "&name=" + global.name + "&x=" + x + "&y=" + y
                                    + "&session=" + global.sessionID;
                        }
                        System.out.println(url);
                        sd.execute(url);
                        try {
                            List<String> result = sd.get();
                            for (String line : result) {
                                if (line.startsWith("ID: ")){
                                    global.userID = Integer.valueOf(line.substring("ID: ".length()));
                                }
                                if (line.startsWith("SESSION: ")){
                                    global.sessionID = line.substring("SESSION: ".length());
                                }
                            }

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
    }

    public void toMaps(View view){
        Intent map = new Intent(this, MapsActivity.class);
        startActivity(map);
        this.finish();
    }
    private void toLogin() {
        this.finish();
    }

}
