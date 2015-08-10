package com.neighbor.ex.tong.network;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.neighbor.ex.tong.CONST;
import com.neighbor.ex.tong.Utils;
import com.neighbor.ex.tong.bean.GPSInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Created by melissa on 15. 7. 7..
 */
public class SendGroupPush {

    private final static String DO_SEND = "http://211.189.132.184:8080/Tong/pushMsgByRadius.do?radius=";

    private Context cont;

    public void sendTong(Context context, String msg) {

        SharedPreferences pref =
                PreferenceManager.getDefaultSharedPreferences(context);
        String plateNo = pref.getString(CONST.ACCOUNT_LICENSE, "");
        cont = context;
        Request rsq = new Request();
        rsq.setParam(context, msg);
        rsq.start();
    }

    interface Param {
        public void setParam(Context cont, String msg);
    }

    class Request extends Thread implements Param {
        String message;
        Context _conxt;

        @Override
        public void run() {

            StringBuffer mkcheking = new StringBuffer();
            SharedPreferences pref =
                    PreferenceManager.getDefaultSharedPreferences(_conxt);
            GPSInfo info = GPSInfo.CreateInstance();
            HashMap<String, Double> gpsMap = info.getGPSInfo();
            String senderCarNo = pref.getString(CONST.ACCOUNT_LICENSE, "");
            String Distance = pref.getString("radius", "1km");
            try {
                mkcheking.append(DO_SEND);
                mkcheking.append(Distance.substring(0, 1));
                mkcheking.append("&gpsLat=");
                mkcheking.append(gpsMap.get(CONST.GPS_LATITUDE));
                mkcheking.append("&gpsLong=");
                mkcheking.append(gpsMap.get(CONST.GPS_LONGITUDE));
                mkcheking.append("&msg=");
                mkcheking.append(URLEncoder.encode(message, "utf-8"));
                mkcheking.append("&senderMemberCarNum=");
                mkcheking.append(URLEncoder.encode(senderCarNo, "utf-8"));

                Log.d("hts", "url pushMsgByRadius : " + mkcheking.toString());

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            try {

                String rsJson = Utils.getJSON(mkcheking.toString(), 15000);
                Log.d("Tong", "++++" + mkcheking.toString() + "++++++");
                JSONObject json = new JSONObject(rsJson);
                String result = json.getString("RESULT");
                String cause = json.getString("MESSAGE");
                Log.d("Tong", "++++" + result + "++++++");
                Log.d("Tong", "++++" + cause + "++++++");

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } finally {
                ((Activity) cont).finish();
            }
        }

        @Override
        public void setParam(Context cont, String msg) {
            _conxt = cont;
            message = msg;
        }
    }

}
