package com.neighbor.ex.tong.service;


import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.neighbor.ex.tong.CONST;
import com.neighbor.ex.tong.Utils;
import com.neighbor.ex.tong.bean.GPSInfo;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;


public class GPSTrack extends Service implements LocationListener   {
    private final static String TAG = "GPSTrack";
    private final static String GPS_URL = "http://211.189.132.184:8080/Tong/updateMemberLocation.do?memberGmail=";

    // flag for GPS status
    boolean isGPSEnabled = false;

    boolean isPassiveEnabled = false;

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 30000; // 1 초
    // Declaring a Location Manager
    protected LocationManager locationManager;

    private GPSInfo info;

    private Timer timer = null;
    private String id;
    public HashMap<String, Double> gpsMap;
    private SharedPreferences prefs;

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        info = GPSInfo.CreateInstance();
        location = getLocation();
        sendGPS();
        if (location != null) {
            info.setGPSinfo(location.getLatitude(), location.getLongitude());
        } else {
            long lonLatitude = prefs.getLong(CONST.GPS_LATITUDE, 0);
            long lonLongitude = prefs.getLong(CONST.GPS_LONGITUDE, 0);

            if (lonLatitude != 0 && lonLongitude != 0) {
                double latitude = Double.longBitsToDouble(lonLatitude);
                double logitude = Double.longBitsToDouble(lonLongitude);
                info.setGPSinfo(latitude, logitude);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isPassiveEnabled = locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);

            if (!isGPSEnabled) {
                // no network provider is enabled
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                Log.d("GPS Enabled", "GPS Enabled");
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(GPSTrack.this);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        info.setGPSinfo(location.getLatitude(), location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onDestroy() {
        SharedPreferences.Editor editor = prefs.edit();
        gpsMap = info.getGPSInfo();
        editor.putLong(CONST.GPS_LATITUDE, Double.doubleToLongBits(gpsMap.get(CONST.GPS_LATITUDE)));
        editor.putLong(CONST.GPS_LONGITUDE, Double.doubleToLongBits(gpsMap.get(CONST.GPS_LONGITUDE)));
        editor.commit();

        stopUsingGPS();

        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        super.onDestroy();
    }

    void sendGPS() {

        id = prefs.getString(CONST.ACCOUNT_ID, "");

        timer = new Timer();

        TimerTask myTask = new TimerTask() {
            @Override
            public void run() {
                gpsMap = info.getGPSInfo();
                Double pos_x = gpsMap.get(CONST.GPS_LATITUDE);
                Double pos_y = gpsMap.get(CONST.GPS_LONGITUDE);

                StringBuffer mkcheking = new StringBuffer();
                mkcheking.append(GPS_URL);
                mkcheking.append(id);
                mkcheking.append("&gpsLat=");
                mkcheking.append(pos_x.toString());
                mkcheking.append("&gpsLong=");
                mkcheking.append(pos_y.toString());

                try {
                    Log.d("Tong", "++++" + pos_x + "++++++");
                    Log.d("Tong", "++++" + pos_y + "++++++");

                    if (pos_x != 0.0 && pos_y != 0.0) {
                        String jsonResult = Utils.getJSON(mkcheking.toString(), 15000);
                        Log.d("Tong", "++++" + jsonResult + "++++++");
                    }

                } catch (Exception e) {
                    e.getStackTrace();
                }

            }
        };
        timer.schedule(myTask, 0, 18000); //3분마다 갱신
    }
}
