package com.project.m117;

import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Button assault_button;
    Button shop_button;
    Button center_button;
    Button zoom_in;
    Button zoom_out;
    LatLng mCoordinate;
    TextView attack_count;
    TextView defense_count;
    TextView health_count;
    TextView resource_count;
    TextView time_remaining;
    final int UPDATE_SPEED = 1000;

    boolean respawning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final GlobalApplication global = (GlobalApplication)this.getApplication();

        setContentView(R.layout.activity_maps);

        attack_count = (TextView)findViewById(R.id.attack_count);
        defense_count = (TextView)findViewById(R.id.defense_count);
        health_count = (TextView)findViewById(R.id.health_count);
        resource_count = (TextView)findViewById(R.id.resource_count);

        time_remaining = (TextView)findViewById(R.id.time_remaining);
        time_remaining.setText(""+ global.minutes + ":" +global.seconds);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                updateMarkers(mMap);
                attack_count.setText(String.valueOf(global.attack));
                defense_count.setText(String.valueOf(global.defense));
                health_count.setText(String.valueOf(global.health));
                resource_count.setText(String.valueOf(global.resources));

                global.seconds -= 1;
                if (global.seconds < 0){
                    global.minutes -= 1;
                    global.seconds = 59;
                }

                time_remaining.setText(""+ global.minutes + ":" +global.seconds);
                handler.postDelayed(this, UPDATE_SPEED);
            }
        }, UPDATE_SPEED);

        assault_button = (Button)findViewById(R.id.assault_button);
        assault_button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view){
                toAttack(view);
            }});

        shop_button = (Button)findViewById(R.id.shop_button);
        shop_button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view){
                toShop(view);
            }});

        center_button = (Button)findViewById(R.id.center);
        center_button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view){
                mMap.moveCamera(CameraUpdateFactory.newLatLng(mCoordinate));
            }});

        zoom_in = (Button)findViewById(R.id.zoom_in);
        zoom_in.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view){
                mMap.moveCamera(CameraUpdateFactory.zoomIn());
            }});

        zoom_out = (Button)findViewById(R.id.zoom_out);
        zoom_out.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view){
                mMap.moveCamera(CameraUpdateFactory.zoomOut());
            }});
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        updateMarkers(mMap);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCoordinate, (float)21));
        final GlobalApplication global = (GlobalApplication)this.getApplication();
//        mMap.getUiSettings().setScrollGesturesEnabled(false);
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                boolean self = marker.getTitle().split(",")[0].contains("(you)");
                boolean enemy = !((marker.getTitle().split(",")[0].equals("human") && global.human) ||
                        (!marker.getTitle().split(",")[0].equals("human") && !global.human));
                System.out.println(marker.getTitle().split(",")[0]);

                String enemyName = "", enemyId = "";
                if (!self) {
                    enemyName = marker.getTitle().split(",")[1];
                    enemyId = marker.getTitle().split(",")[2];
                }

                ServerData attackSd = new ServerData() {
                    @Override
                    public void doAtEnd(List<String> result) {
                        ;
                    }
                };

                if (self) {
                    Toast.makeText(getApplicationContext(), marker.getTitle().split(",")[0], Toast.LENGTH_SHORT).show();
                    return true;
                }
                else if (!enemy){
                    Toast.makeText(getApplicationContext(), "That's an ally!" + enemyName, Toast.LENGTH_SHORT).show();
                    return true;
                }

                String attackUrl;
                try {
                    URI uri = new URI(
                            "http",
                            null,
                            "23.243.209.238",
                            9088,
                            "/game/attack",
                            "app&id1=" + String.valueOf(global.userID)
                                    + "&id2=" + enemyId + "&session=" + String.valueOf(global.sessionID),
                            null);
                    attackUrl = uri.toASCIIString();
                } catch (Exception e) {
                    attackUrl = "http://23.243.209.238:9088/game/attack?app&id1="
                            + String.valueOf(global.userID) + "&id2="
                            + enemyId + "&session=" + String.valueOf(global.sessionID);
                }
                attackSd.execute(attackUrl);
                Toast.makeText(getApplicationContext(), "Attacking " + enemyName, Toast.LENGTH_SHORT).show();
                try {
                    List<String> atkResponse = attackSd.get();
                    for (String line : atkResponse){
                        if (line.startsWith("KILL: ")){
                            Toast.makeText(getApplicationContext(), line + " resources gained", Toast.LENGTH_SHORT).show();
                        }
                        if (line.startsWith("NEW HEALTH: ")){
                            Toast.makeText(getApplicationContext(), enemyName +
                                    " HP: " + line.substring("NEW HEALTH".length()), Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
//                thisAct.finish();
                return true;
            }
        });
    }

    public void updateMarkers(GoogleMap googleMap) {

        final GlobalApplication global = (GlobalApplication)this.getApplication();

        double lat = global.locX;
        double lng = global.locY;

        if (!(lat > -0.001 && lat < 0.001 && lng > -0.001 && lng < 0.001)) {
            mCoordinate = new LatLng(lat, lng);
        } else {
            mCoordinate = new LatLng(global.locX, global.locY);
        }
        MarkerOptions marker = new MarkerOptions().position(mCoordinate);
        BitmapDescriptor character = BitmapDescriptorFactory.fromResource(R.drawable.player);
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(mCoordinate));

        ServerData sd = new ServerData() {
            @Override
            public void doAtEnd(List<String> result) {
                mMap.clear();
                global.enemies = new ArrayList<GlobalApplication.Enemy>();
                try {
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

                            global.human = data[1].equals("human");
                            if (data[2].equals("DEAD")){
                                //Toast.makeText(getApplicationContext(), "You Have Died", Toast.LENGTH_SHORT).show();
                                //toLogin();
                                toRespawn();
                            }
                            global.health = Integer.valueOf(data[3]);
                            global.attack = Integer.valueOf(data[4]);
                            global.defense = Integer.valueOf(data[5]);
                            global.resources = Integer.valueOf(data[6]);
                            double lat =  Double.valueOf(data[7]);
                            double lng = Double.valueOf(data[8]);
                            if (!(lat > -0.001 && lat < 0.001 && lng > -0.001 && lng < 0.001)) {
                                global.locX = lat;
                                global.locY = lng;
                                mCoordinate = new LatLng(global.locX, global.locY);
                                MarkerOptions marker = new MarkerOptions().position(mCoordinate);
                                marker.title(data[2] + " (you)");
                                BitmapDescriptor character = BitmapDescriptorFactory.fromResource(R.drawable.player);
                                mMap.addMarker(marker).setIcon(character);
                            }
                        } else if (line.startsWith("ALLY: ") || line.startsWith("ENEMY: ")){
                            String[] data;
                            if (line.startsWith("ALLY: ")){
                                data = line.substring("ALLY: ".length()).split(", ");
                            } else {
                                data = line.substring("ENEMY: ".length()).split(", ");
                                GlobalApplication.Enemy e = new GlobalApplication.Enemy();
                                e.userID = Integer.valueOf(data[0]);
                                e.human = data[1].equals("human");
                                e.name = data[2];
                                e.health = Integer.valueOf(data[3]);
                                e.attack = Integer.valueOf(data[4]);
                                e.defense = Integer.valueOf(data[5]);
                                e.resources = Integer.valueOf(data[6]);
                                e.locX = Double.valueOf(data[7]);
                                e.locY = Double.valueOf(data[8]);
                                global.enemies.add(e);
                            }
                            double lat =  Double.valueOf(data[7]);
                            double lng = Double.valueOf(data[8]);
                            LatLng coordinate = new LatLng(lat, lng);
                            MarkerOptions marker = new MarkerOptions().position(coordinate);
                            marker.title(data[1]+","+data[2]+","+data[0]);
                            int type = (data[1].equals("human"))?R.drawable.human:R.drawable.zombie;
                            BitmapDescriptor character = BitmapDescriptorFactory.fromResource(type);
                            mMap.addMarker(marker).setIcon(character);
                        }
                    }
                } catch (Exception e){
                    System.out.println(e);
//                    Toast.makeText(getApplicationContext(), "Update Error", Toast.LENGTH_SHORT).show();
                }
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
                    "bot&app&id=" + String.valueOf(global.userID)
                            + "&session="
                            + String.valueOf(global.sessionID) + "&x=" + String.valueOf(lat)
                            + "&y=" + String.valueOf(lng),
                    null);
            url = uri.toASCIIString();
        } catch (Exception e) {
            url = "http://23.243.209.238:9088/game/update?app&bot&id="
                    + String.valueOf(global.userID) + "&session="
                    + String.valueOf(global.sessionID) + "&x=" + String.valueOf(lat)
                    + "&y=" + String.valueOf(lng);
        }
        sd.execute(url);
    }

    private void toShop(View view){
        Intent shop = new Intent(this, ShopActivity.class);
        startActivity(shop);
    }
    private void toAttack(View view){
        Intent attack = new Intent(this, AttackActivity.class);
        startActivity(attack);
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
