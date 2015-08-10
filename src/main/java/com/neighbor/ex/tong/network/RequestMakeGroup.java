package com.neighbor.ex.tong.network;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.neighbor.ex.tong.CONST;
import com.neighbor.ex.tong.Utils;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Observable;


public class RequestMakeGroup extends Observable {
    private final String TAG = "RequestMakeGroup";
    private final static String MAKE_GROUP_URL =
            "http://211.189.132.184:8080/Tong/regTongInfo.do?roomMasterGmail=";
    private String groupDesc;

    private String userID;
    private String groupName;
    private Context mContext;

    private final static int REQUEST_OK = 1;
    private final static int REQUEST_FAIL = -1;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == REQUEST_OK) {
                setChanged();
                notifyObservers(msg.obj);
            } else {
                Toast.makeText(mContext, "그룹생성에 실패 하였습니다. 다시 시도해주세요",
                        Toast.LENGTH_LONG).show();
            }
        }
    };

    public RequestMakeGroup(Context context, String newGroup, String groupDesc) {
        try {

            this.mContext = context;
            this.groupName = URLEncoder.encode(newGroup, "utf-8");
            this.groupDesc = URLEncoder.encode(groupDesc, "utf-8");

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
            this.userID = pref.getString(CONST.ACCOUNT_ID, "");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void action() {
        StringBuffer mkcheking = new StringBuffer();
        mkcheking.append(MAKE_GROUP_URL);
        mkcheking.append(userID);
        mkcheking.append("&roomName=");
        mkcheking.append(groupName);
        mkcheking.append("&roomDesc=");
        mkcheking.append(groupDesc);

        RegistNewGroup newgroup = new RegistNewGroup();

        Log.d(TAG, "action mkcheking : " + mkcheking.toString());
        newgroup.setParam(mkcheking.toString());
        newgroup.start();
    }

    interface Param {
        void setParam(String arg0);
    }

    class RegistNewGroup extends Thread implements Param {
        String uri;

        @Override
        public void run() {

            try {
                String rsjson = Utils.getJSON(uri, 15000);
                JSONObject json = new JSONObject(rsjson);
                String result = json.getString("RESULT");
                String cause = json.getString("MESSAGE");

                Message msg = handler.obtainMessage();
                msg.obj = json;
                msg.what = REQUEST_OK;
                handler.sendMessage(msg);

            } catch (Exception e) {
                e.printStackTrace();
                Message msg = handler.obtainMessage();
                msg.what = REQUEST_FAIL;
                handler.sendMessage(msg);
            }
        }

        @Override
        public void setParam(String arg0) {
            this.uri = arg0;
        }
    }
}
