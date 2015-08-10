package com.neighbor.ex.tong.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.neighbor.ex.tong.CONST;
import com.neighbor.ex.tong.Utils;
import org.json.JSONException;
import org.json.JSONObject;


public class SendDeviceID {

    private final static String _SEND_DEVICEID_URL ="http://211.189.132.184:8080/Tong/api/ChangeDeviceId.do?device_id=";

    private  Context cont;
    private  SharedPreferences pref;

    public void SendDevice(Context context, String device){
       pref = PreferenceManager.getDefaultSharedPreferences(context);
       cont = context;
       Request rsq = new Request();
       rsq.upDateDeviceId(device);
       rsq.start();
    }

    interface setParam {
        public void upDateDeviceId(String dev);
    }

    class Request extends Thread implements setParam {
        String DeviceID;

        @Override
        public void run() {

            StringBuffer mkcheking  = new StringBuffer();
            mkcheking.append(_SEND_DEVICEID_URL);
            mkcheking.append(DeviceID);
            mkcheking.append("&gmail=");
            mkcheking.append(pref.getString(CONST.ACCOUNT_ID,"")+"@gmail.com");
            Log.d("hts", "ChangeDeviceId url : " + mkcheking.toString());

            try {
                String rsJson = Utils.getJSON(mkcheking.toString(), 15000);
                Log.d("Tong", "++++" + mkcheking.toString() + "++++++");
                JSONObject jsObj = new JSONObject(rsJson);

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e){
                e.printStackTrace();
            }
        }

        @Override
        public void upDateDeviceId(String dev) {
            DeviceID = dev;
        }
    }
}
