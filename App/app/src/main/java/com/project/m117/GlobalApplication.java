package com.project.m117;

import android.app.Application;
import android.os.StrictMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrew on 3/10/2017.
 */

public class GlobalApplication extends Application {
    public String sessionID;

    public int userID;
    public String name;
    public boolean human;
    public int health;
    public int attack;
    public int defense;
    public int resources;
    public double locX;
    public double locY;

    public boolean locationEnabled = false;
    public long minutes;
    public long seconds;

    public static class Enemy{
        public int userID;
        public String name;
        public boolean human;
        public int health;
        public int attack;
        public int defense;
        public int resources;
        public double locX;
        public double locY;

        public Enemy(){

        }
    }
    public List<Enemy> enemies = new ArrayList<Enemy>();
}
