package com.neighbor.ex.tong.network;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;

import com.neighbor.ex.tong.CONST;
import com.neighbor.ex.tong.Utils;
import com.neighbor.ex.tong.provider.DataProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by melissa on 15. 7. 7..
 */
public class SendPush {

    private final static String DO_SEND = "http://211.189.132.184:8080/Tong/regTrafficCenterReport.do?receiveMemberCarNum=";

    private Context cont;

    public void sendTong(Context context, String receiveMemberCarNum, String msg, Location location) {

        SharedPreferences pref =
                PreferenceManager.getDefaultSharedPreferences(context);
        String senderMemberCarNum = pref.getString(CONST.ACCOUNT_LICENSE, "");
        cont = context;
        Request rsq = new Request();
        Log.d("SendPush", "+++ " + msg);
        rsq.setMethod(receiveMemberCarNum, msg, senderMemberCarNum, location);
        rsq.start();
    }

    interface checkRedundancy {
        public void setMethod(String receiveMemberCarNum, String sendWhat, String senderMemberCarNum, Location location);
    }

    class Request extends Thread implements checkRedundancy {
        String receiveMemberCarNum;
        String sendWhat;
        String senderMemberCarNum;
        private Location location;

        @Override
        public void run() {

            StringBuffer mkcheking = new StringBuffer();

            try {
                mkcheking.append(DO_SEND);
                mkcheking.append(URLEncoder.encode(receiveMemberCarNum, "utf-8"));
                mkcheking.append("&commentType=U");
                mkcheking.append("&senderMemberCarNum=");
                mkcheking.append(URLEncoder.encode(senderMemberCarNum , "utf-8"));
                mkcheking.append("&comment=");
                mkcheking.append(URLEncoder.encode(sendWhat, "utf-8"));

                if (location != null) {
                    mkcheking.append("&gpsLat=");
                    mkcheking.append(location.getLatitude());
                    mkcheking.append("&gpsLong=");
                    mkcheking.append(location.getLongitude());
                } else {
                    mkcheking.append("&gpsLat=0&gpsLong=0");
                }

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            try {
                Log.d("hts", "mkcheking : " + mkcheking.toString());
                String rsJson = Utils.getJSON(mkcheking.toString(), 15000);
                Log.d("Tong", "++++" + mkcheking.toString() + "++++++");
                JSONObject json = new JSONObject(rsJson);
                String result = json.getString("RESULT");
                String cause = json.getString("MESSAGE");
                Log.d("Tong", "++++" + result + "++++++");
                Log.d("Tong", "++++" + cause + "++++++");

                ContentValues cv = new ContentValues();
                cv.put("CONTENTS", sendWhat);
                cv.put("RECOMM_CNT", 0);
                cv.put("DoSend", "true");
                cv.put("RECEIVER", receiveMemberCarNum);
                cv.put("SENDER", senderMemberCarNum);
                cv.put("TIME", Utils.getDateTime());
                cv.put("SEQ", "0");

                cont.getContentResolver().insert(DataProvider.TONG_URI, cv);

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } finally {
                ((Activity) cont).finish();
            }
        }

        @Override
        public void setMethod(String receiveMemberCarNum, String sendWhat, String senderMemberCarNum, Location location) {
            this.receiveMemberCarNum = receiveMemberCarNum;
            this.sendWhat = sendWhat;
            this.senderMemberCarNum = senderMemberCarNum;
            this.location = location;
        }
    }

}
