package com.project.m117;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttackActivity extends Activity {

    private ListView lv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final GlobalApplication global = (GlobalApplication)this.getApplication();
        setContentView(R.layout.activity_attack);

        final AttackActivity thisAct = this;

        lv = (ListView) findViewById(R.id.enemy_list);

        final List<Map<String,String>> lmss = new ArrayList<Map<String,String>>();
        for (GlobalApplication.Enemy e : global.enemies){
            Map<String,String> map = new HashMap<String, String>();
            map.put("name", e.name.substring(0,Math.min(10,e.name.length())));
            map.put("h", String.valueOf(e.health));
            map.put("a", String.valueOf(e.attack));
            map.put("d", String.valueOf(e.defense));
            map.put("id", String.valueOf(e.userID));
            lmss.add(map);
        }
        String fromArray[]={"name", "h","a", "d", "id"};
        int to[]={R.id.name, R.id.health_count,R.id.attack_count, R.id.defense_count, R.id.userID};
        lv.setAdapter(new SimpleAdapter(this, lmss, R.layout.list_item, fromArray, to));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String enemyId = ((TextView)view.findViewById(R.id.userID)).getText().toString();
                String enemyName = ((TextView)view.findViewById(R.id.name)).getText().toString();
                ServerData attackSd = new ServerData() {
                    @Override
                    public void doAtEnd(List<String> result) {
                        ;
                    }
                };
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
                try {
                    List<String> atkResponse = attackSd.get();
                    for (String line : atkResponse){
                        if (line.startsWith("KILL: ")){
                            lmss.remove(position);
                            ((SimpleAdapter)lv.getAdapter()).notifyDataSetChanged();
                        }
                        if (line.startsWith("NEW HEALTH: ")){
                           //((Map<String,String>)((SimpleAdapter)lv.getAdapter()).getItem(position)).put("h", line.substring("OLD HEALTH: ".length()));
                           ((TextView)view.findViewById(R.id.health_count)).setText(
                                   line.substring("NEW HEALTH: ".length()));
                           //lmss.get(position).put("h", line.substring("OLD HEALTH: ".length()));
                           //((SimpleAdapter)lv.getAdapter()).notifyDataSetChanged();
                        }
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
                Toast.makeText(getApplicationContext(), "Attacking " + enemyName, Toast.LENGTH_SHORT).show();
//                thisAct.finish();
            }
        });

        Button mapButton = (Button) findViewById(R.id.map_button);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thisAct.finish();
            }
        });
    }
}
