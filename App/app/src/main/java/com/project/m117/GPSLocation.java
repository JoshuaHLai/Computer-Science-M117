package com.project.m117;

import android.*;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GPSLocation {

    private LocationManager locationManager;
    private LocationListener locationListener;

    public double lat;
    public double lng;

    public static double[] getPlaceholderLoc(){
        double[] placeholder = {34.0689, -118.4452};
        return placeholder;
    }

    public double[] getLoc(){
        double[] placeholder = {lat, lng};
        return placeholder;
    }

    public GPSLocation(final Activity a){
        locationManager = (LocationManager) a.getSystemService(a.LOCATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (a.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && a.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                a.requestPermissions(new String[]{
                        android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.INTERNET
                }, 10);
                return;
            }
        }
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lat = location.getLatitude();
                lng = location.getLongitude();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                a.startActivity(intent);
            }
        };
        updateLocation();
    }

    public void updateLocation(){
        try {
            locationManager.requestLocationUpdates("gps", 1000, 0, locationListener);
        } catch (SecurityException e){
            System.out.println(e);
        }
    }

}