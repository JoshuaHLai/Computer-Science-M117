package com.project.m117;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;

/**
 * Created by Andrew on 3/13/2017.
 */

public class GPSLocationService extends Service {

    private LocationManager locationManager;
    private LocationListener locationListener;

    private GlobalApplication global = null;

    public double lat;
    public double lng;

    @Override
    public void onCreate() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        global = (GlobalApplication)this.getApplication();
        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lat = location.getLatitude();
                lng = location.getLongitude();
                global.locX = lat;
                global.locY = lng;
                global.locationEnabled = true;
                System.out.println("" + lat + ", " + lng);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                global.locationEnabled = false;
            }
        };
        try {
            Location l = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (l != null){
                global.locX = l.getLatitude();
                global.locY = l.getLongitude();
            }
            Criteria lowAccuracyInit=  new Criteria();
            lowAccuracyInit.setAccuracy(Criteria.ACCURACY_COARSE);
            locationManager.requestSingleUpdate(lowAccuracyInit, locationListener, null);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
        } catch (SecurityException e){
            e.printStackTrace();
        }
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
