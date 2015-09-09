package com.neighbor.ex.tong.network;


import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.neighbor.ex.tong.CONST;
import com.neighbor.ex.tong.Utils;
import com.neighbor.ex.tong.common.SharedPreferenceManager;
import com.neighbor.ex.tong.provider.DataProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class RequestAllCarNo {

    private static String All_CAR_NO_URL =
            "http://211.189.132.184:8080/Tong/api/MemberCarNumberList.do?memberGmail=";
    private Context _cont;

    public void Action(Context context) {
        _cont = context;
        String license = SharedPreferenceManager.getValue(_cont, CONST.ACCOUNT_ID) + "@gmail.com";
        new AllSubscriber(license).start();
    }

    class AllSubscriber extends Thread {

        public AllSubscriber(String license) {
            All_CAR_NO_URL += license;
        }

        @Override
        public void run() {
            try {
                String rsjson = Utils.getJSON(All_CAR_NO_URL, 15000);
                if (rsjson == null)
                    return;
                JSONObject rootObject = new JSONObject(rsjson);
                JSONArray jsonArray = (JSONArray) rootObject.get("rows");

                if (jsonArray.length() > 0) {
                    _cont.getContentResolver().delete(DataProvider.PLATE_URI, null, null);
                }

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = (JSONObject) jsonArray.get(i);
                    String id = obj.getString("MEMBER_GMAIL");
                    String carNo = obj.getString("MEMBER_CAR_NUM");
                    ContentValues cv = new ContentValues();
                    cv.put("ID", id);
                    cv.put("PLATE_NUMBER", carNo);
                    _cont.getContentResolver().insert(DataProvider.PLATE_URI, cv);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
