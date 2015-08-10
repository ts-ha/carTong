package com.neighbor.ex.tong.network;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.neighbor.ex.tong.Utils;
import com.neighbor.ex.tong.ui.dialog.CommonProgressDialog;

import org.json.JSONObject;


public class JoinGroup {

    private Context context;
    private NotiJoinRequest notiJoinRequestItf;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            CommonProgressDialog.hideProgress();

            if (msg.what == 0) {
                Toast.makeText(context, "가입 요청을 하였습니다.", Toast.LENGTH_LONG).show();
                notiJoinRequestItf.isGroupjoin(true);
            } else {
                Toast.makeText(context, "전송에 실패하였습니다. 잠시후 다시 실행해 주세요.",
                        Toast.LENGTH_LONG).show();
            }
        }
    };


    public void action(Context context, String url) {
//
        JoinRequest join = new JoinRequest();
        join.setParam(url);
        Log.d("hts", "JoinRequest url ~~~~~~~~~~~~ " + url);
        this.context = context;
        join.start();
    }

    public interface SetParam {
        public void setParam(String arg0);
    }

    public interface NotiJoinRequest {
        void isGroupjoin(boolean flg);
    }

    class JoinRequest extends Thread implements SetParam {
        String Uri;

        @Override
        public void run() {

            String rsJson = Utils.getJSON(Uri, 15000);
            try {
                // Getting Array of Contacts
                JSONObject json = new JSONObject(rsJson);
                String result = json.getString("RESULT");
                String cause = json.getString("MESSAGE");
                Log.d("Tong", "++++" + result + "++++++");
                Log.d("Tong", "++++" + cause + "++++++");
                mHandler.sendEmptyMessage(0);
            } catch (Exception e) {
                e.printStackTrace();
                mHandler.sendEmptyMessage(1);
            }
        }

        @Override
        public void setParam(String arg0) {
            Uri = arg0;
        }
    }
}
