package com.project.m117;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast;

import java.net.URI;
import java.util.List;

public class ShopActivity extends AppCompatActivity {

    Button map_button;
    ImageButton attack_button;
    ImageButton defense_button;
    ImageButton health_button;
    TextView attack_count;
    TextView defense_count;
    TextView health_count;
    TextView resource_count;
    final int UPDATE_SPEED = 200;

    boolean respawning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final GlobalApplication global = (GlobalApplication)this.getApplication();

        setContentView(R.layout.activity_shop);

        map_button = (Button)findViewById(R.id.map_button);
        attack_button = (ImageButton)findViewById(R.id.attack_button);
        defense_button = (ImageButton)findViewById(R.id.defense_button);
        health_button = (ImageButton)findViewById(R.id.health_button);
        attack_count = (TextView)findViewById(R.id.attack_count);
        defense_count = (TextView)findViewById(R.id.defense_count);
        health_count = (TextView)findViewById(R.id.health_count);
        resource_count = (TextView)findViewById(R.id.resource_count);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                updatePlayerInfo(global);
                handler.postDelayed(this, UPDATE_SPEED);
            }
        }, UPDATE_SPEED);

        map_button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view){
                toMaps(view);
            }});

        updatePlayerInfo(global);

        attack_button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view){
                Toast.makeText(getApplicationContext(), "Attack +1", Toast.LENGTH_SHORT).show();
                //server: reduce resource, increase attack
                int resources = global.resources;
                if (resources >= 100){
                    buyItem(global, "a="+String.valueOf(global.attack+1)+"&r="+
                            String.valueOf(resources-100));
                }
            }});

        defense_button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view){
                Toast.makeText(getApplicationContext(), "Defense +1", Toast.LENGTH_SHORT).show();
                //server: reduce resource, increase defense
                int resources = global.resources;
                if (resources >= 100){
                    buyItem(global, "d="+String.valueOf(global.defense+1)+"&r="+
                            String.valueOf(resources-100));
                }
            }});

        health_button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view){
                Toast.makeText(getApplicationContext(), "Health +5", Toast.LENGTH_SHORT).show();
                //server: reduce resource, increase health
                int resources = global.resources;
                if (resources >= 100){
                    buyItem(global, "h="+String.valueOf(global.health+5)+"&r="+
                            String.valueOf(resources-100));
                }
            }});
    }

    private void buyItem(GlobalApplication global, String attr){
        ServerData sd = new ServerData() {
            @Override
            public void doAtEnd(List<String> result) {
                ;
            }
        };
        String url = "";
        try {
            URI uri = new URI(
                    "http",
                    null,
                    "23.243.209.238",
                    9088,
                    "/game/update",
                    "app&id=" + String.valueOf(global.userID) + "&session="
                            + String.valueOf(global.sessionID) + "&" +attr,
                    null);
            url = uri.toASCIIString();
        } catch (Exception e) {
            url = "http://23.243.209.238:9088/game/update?app&id="
                    + String.valueOf(global.userID) + "&session="
                    + String.valueOf(global.sessionID) + "&" + attr;
        }
        sd.execute(url);
        try {
            List<String> result = sd.get();
            for (String line : result) {
                if (line.startsWith("NEW SESSION: ")) {
                    // return to login?
                    Toast.makeText(getApplicationContext(), "Session Ended", Toast.LENGTH_SHORT).show();
                    toLogin();
                    global.sessionID = line.substring("NEW SESSION: ".length());
                    break;
                } else if (line.startsWith("NONEXISTENT USER")) {
                    // return to login?
                    Toast.makeText(getApplicationContext(), "Session Ended", Toast.LENGTH_SHORT).show();
                    toLogin();
                } else if (line.startsWith("SELF: ")){
                    String[] data = line.substring("SELF: ".length()).split(", ");
                    if (data[2].equals("DEAD")){
                        //Toast.makeText(getApplicationContext(), "You Have Died", Toast.LENGTH_SHORT).show();
                        //toLogin();
                        toRespawn();
                    }
                    updatePlayerInfo(global, Integer.valueOf(data[3]), Integer.valueOf(data[4]),
                            Integer.valueOf(data[5]), Integer.valueOf(data[6]));
                }
            }
        } catch (Exception e){
            System.out.println(e);
//            Toast.makeText(getApplicationContext(), "Update Error", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePlayerInfo(GlobalApplication global, int h, int a, int d, int r) {
        int attack = a;        //from server, get resource
        int defense = d;
        int health = h;
        int resource = r;
        attack_count.setText(String.valueOf(attack));
        defense_count.setText(String.valueOf(defense));
        health_count.setText(String.valueOf(health));
        resource_count.setText(String.valueOf(resource));
        global.attack = attack;
        global.defense = defense;
        global.health = health;
        global.resources = resource;
    }
    private void updatePlayerInfo(GlobalApplication global) {
        ServerData sd = new ServerData() {
            @Override
            public void doAtEnd(List<String> result) {
                ;
            }
        };
        String url = "";
        try {
            URI uri = new URI(
                    "http",
                    null,
                    "23.243.209.238",
                    9088,
                    "/game/update",
                    "app&id=" + String.valueOf(global.userID) + "&session="
                            + String.valueOf(global.sessionID),
                    null);
            url = uri.toASCIIString();
        } catch (Exception e) {
            url = "http://23.243.209.238:9088/game/update?app&id="
                    + String.valueOf(global.userID) + "&session="
                    + String.valueOf(global.sessionID);
        }
        sd.execute(url);
        try {
            List<String> result = sd.get();
            for (String line : result) {
                if (line.startsWith("NEW SESSION: ")) {
                    // return to login?
                    Toast.makeText(getApplicationContext(), "Session Ended", Toast.LENGTH_SHORT).show();
                    global.sessionID = line.substring("NEW SESSION: ".length());
                    toLogin();
                    break;
                } else if (line.startsWith("NONEXISTENT USER")) {
                    // return to login?
                    Toast.makeText(getApplicationContext(), "Session Ended", Toast.LENGTH_SHORT).show();
                    toLogin();
                } else if (line.startsWith("SELF: ")){
                    String[] data = line.substring("SELF: ".length()).split(", ");
                    if (data[2].equals("DEAD")){
                        //Toast.makeText(getApplicationContext(), "You Have Died", Toast.LENGTH_SHORT).show();
                        //toLogin();
                        toRespawn();
                    }
                    updatePlayerInfo(global, Integer.valueOf(data[3]), Integer.valueOf(data[4]),
                            Integer.valueOf(data[5]), Integer.valueOf(data[6]));
                }
            }
        } catch (Exception e){
            System.out.println(e);
//            Toast.makeText(getApplicationContext(), "Update Error", Toast.LENGTH_SHORT).show();
        }
    }

    private void toMaps(View view){
        /*
        Intent map = new Intent(this, MapsActivity.class);
        map.putExtra("EXIT", true);
        startActivity(map);*/
        this.finish();
    }

    private void toLogin() {
        this.finish();
    }

    private void toRespawn() {
        if (!respawning) {
            Intent respawn = new Intent(this, OptionsActivity.class);
            startActivity(respawn);
            respawning = true;
        }
    }
}
