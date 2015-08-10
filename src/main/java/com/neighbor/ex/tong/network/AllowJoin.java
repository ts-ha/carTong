package com.neighbor.ex.tong.network;

import android.util.Log;
import com.neighbor.ex.tong.Utils;
import org.json.JSONObject;


public class AllowJoin {

    public  void action (String id) {
        AllowMember join = new AllowMember();
        join.setParam(id);
        join.start();
    }

    interface  SetParam {
       public void setParam(String arg0);
    }

    class AllowMember extends Thread implements SetParam {
        private final String ALLOW_URL =
                "http://211.189.132.184:8080/Tong/selectRoomRequestList.do?roomMasterGmail=";
        private String _owerId;

        @Override
        public void run() {
            StringBuilder sb = new StringBuilder();
            sb.append(ALLOW_URL);
            sb.append(_owerId);

            String rsJson = Utils.getJSON(sb.toString(),15000);
            Log.d("AllowJoin", "rsJson" + rsJson);
            try {
                // Getting Array of Contacts
                JSONObject json = new JSONObject(rsJson);
                String result = json.getString("RESULT");
                String cause = json.getString("MESSAGE");
                Log.d("Tong", "++++" + result +"++++++");
                Log.d("Tong", "++++" + cause +"++++++");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void setParam(String _ownerId) {
            _owerId =  _ownerId;
        }
    }
}
