package com.neighbor.ex.tong.bean;

import android.util.Log;

import com.neighbor.ex.tong.CONST;

import java.util.HashMap;

public class GPSInfo {

    private static GPSInfo info;
    private static HashMap<String ,Double> gpsinfo;

    public static GPSInfo CreateInstance(){
        if (info == null) {
            info = new GPSInfo();
            gpsinfo = new HashMap();
            gpsinfo.put(CONST.GPS_LATITUDE, 0.0);
            gpsinfo.put(CONST.GPS_LONGITUDE, 0.0);
        }
        return info;
    }

    public void setGPSinfo (Double lat, Double lon) {
//        Log.d("GPSInfo", "+++ setGPSinfo" + "lat:" + lat );
//        Log.d("GPSInfo", "+++ setGPSinfo" + "lon:" + lon );

        gpsinfo.put(CONST.GPS_LATITUDE, lat);
        gpsinfo.put(CONST.GPS_LONGITUDE, lon);
    }

    public HashMap<String,Double> getGPSInfo (){
        return gpsinfo;
    }
}
