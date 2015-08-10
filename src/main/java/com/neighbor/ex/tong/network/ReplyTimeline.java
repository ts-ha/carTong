package com.neighbor.ex.tong.network;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.neighbor.ex.tong.Utils;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.Observable;

/**
 * Created by User on 2015-07-06.
 */
public class ReplyTimeline extends Observable {

    private final static String URL =
            "http://211.189.132.184:8080/Tong/regTongContentRly.do?contentId=";

    private final static int REQUEST_OK = 1;
    private final static int REQUEST_FAIL = -1;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == REQUEST_OK) {
                setChanged();
                notifyObservers(msg.obj);
            }
        }
    };

    public void postReply(String contentId, String id, String message) {
        PostMessage post = new PostMessage();
        post.postReply(contentId, id, message);
        post.start();
    }

    interface SetTimeline {
        public void postReply(String contentId, String id, String message);
    }

    class PostMessage extends Thread implements SetTimeline {
        String TimelineID;
        String _id;
        String _message;

        @Override
        public void run() {
            try {
                StringBuffer mkcheking = new StringBuffer();
                mkcheking.append(URL);
                mkcheking.append(TimelineID);
                mkcheking.append("&regMemberGmail=");
                mkcheking.append(_id);
                mkcheking.append("&contentRly=");
                mkcheking.append(URLEncoder.encode(_message, "utf-8"));

                Log.d("hts", "regTongContentRly url : " + mkcheking.toString());

                String rsjson = Utils.getJSON(mkcheking.toString(), 15000);
                try {

                    JSONObject json = new JSONObject(rsjson);
                    String result = json.getString("RESULT");
                    String cause = json.getString("MESSAGE");
                    Log.d("Tong", "++++" + result + "++++++");
                    Log.d("Tong", "++++" + cause + "++++++");

                    if (result.equalsIgnoreCase("FALSE")) {
                        handler.sendEmptyMessage(REQUEST_FAIL);
                    } else {
                        handler.sendEmptyMessage(REQUEST_OK);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void postReply(String contentId, String id, String message) {
            TimelineID = contentId;
            _id = id;
            _message = message;
        }
    }
}
