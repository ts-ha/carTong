package com.neighbor.ex.tong;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.neighbor.ex.tong.common.Common;
import com.neighbor.ex.tong.provider.DataProvider;
import com.neighbor.ex.tong.ui.activity.LoginActivity;

import java.net.URLDecoder;
import java.util.Iterator;
import java.util.List;

public class GcmIntentService extends IntentService {

    private Handler mHandler;

    public GcmIntentService() {
        super("GcmIntentService");
        mHandler = new Handler();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                Log.e("GcmIntentService", "Send error : " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                Log.e("GcmIntentService", "Deleted messages on server : " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                String msg = "";
                String SenderCarNo = "";
                String Userseq = "";

                Iterator<String> iterator = extras.keySet().iterator();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    String value = extras.get(key).toString();
                    try {
                        if (key.equals("MESSAGE"))
                            msg = URLDecoder.decode(value, "utf-8");
                        if (key.equals("SENDER_CAR_NUM"))
                            SenderCarNo = URLDecoder.decode(value, "utf-8");
                        if (key.equals("SENDER_SEQ")) {
                            Userseq = String.valueOf(value);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (!msg.equalsIgnoreCase("UPDATE_OK") && !msg.equalsIgnoreCase("")) {

                    ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                    List<ActivityManager.RunningTaskInfo> services = am.getRunningTasks(Integer.MAX_VALUE);

                    boolean isRunning = false;

                    if (services.get(0).topActivity.getPackageName().toString()
                            .equalsIgnoreCase(this.getPackageName().toString())) {
                        isRunning = true;
                    }

                    if (false == isRunning) {
                        Uri path = getSoundPath(msg);
                        NotiMessage(msg, path);
                    } else {
                        mHandler.post(new DisplayToast(this, msg));
                    }

//                    Log.d("hts", "sendGCMIntent msg : " + msg + "\t Userseq : " + Userseq + "\t SenderCarNo : " + SenderCarNo);
                    sendGCMIntent(GcmIntentService.this, msg, null, SenderCarNo, Userseq);
                }
            }
        }

        GCMBroadCastReceiver.completeWakefulIntent(intent);
    }

    private Uri getSoundPath(String message) {
        Uri path;
        StringBuilder sb = new StringBuilder();
        sb.append("android.resource://com.neighbor.ex.tong/");

        if (message.matches(".*문.*")) {
            sb.append(R.raw.door);
        } else if (message.matches(".*적재물.*")) {
            sb.append(R.raw.drop);
        } else if (message.matches(".*라이트.*")) {
            sb.append(R.raw.light);
        } else if (message.matches(".*타이어.*")) {
            sb.append(R.raw.tire);
        } else if (message.matches(".*도와주세요.*")) {
            sb.append(R.raw.help);
            message = "도와주세요";
        } else if (message.matches(".*졸지마세요.*")) {
            sb.append(R.raw.notsleep);
            message = "졸지마세요";
        } else {
            Log.d("GCMIntentService", "message.matches(default) ");
        }
        return Uri.parse(sb.toString());
    }

    private void NotiMessage(String message, Uri uri) {

        NotificationManager nm = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Common.PUSH_NOIT, Common.PUSH_NOIT);
        PendingIntent pendingIntent = PendingIntent.
                getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());
        mBuilder.setSmallIcon(R.drawable.icon);
        mBuilder.setTicker("카통:" + message);
        mBuilder.setWhen(System.currentTimeMillis());
        mBuilder.setContentTitle("카통");
        mBuilder.setContentText(message);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setAutoCancel(true);
        mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
        mBuilder.setSound(uri);
        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        nm.notify(0, mBuilder.build());
    }

    private void sendGCMIntent(Context ctx, String message, String image,
                               String sender, String seq) {

        SharedPreferences pref =
                PreferenceManager.getDefaultSharedPreferences(ctx);

        ContentValues cv = new ContentValues();

        cv.put("CONTENTS", message);
        cv.put("RECOMM_CNT", 0);
        cv.put("DoSend", "false");
        cv.put("RECEIVER", pref.getString(CONST.ACCOUNT_LICENSE, ""));
        cv.put("SENDER", sender);
        cv.put("TIME", Utils.getDateTime());
        cv.put("SEQ", seq);
        ctx.getContentResolver().insert(DataProvider.TONG_URI, cv);

        Intent broadcast = new Intent(CONST.TONG_MESSAGE_UPDATE);

        sendBroadcast(broadcast);
    }

    class DisplayToast implements Runnable {
        private final Context mContext;
        String mText;

        public DisplayToast(Context mContext, String text) {
            this.mContext = mContext;
            mText = text;
        }

        public void run() {

            LayoutInflater inflater = LayoutInflater.from(mContext);
            View layout = inflater.inflate(R.layout.toast_layout, null);

            ImageView image = (ImageView) layout.findViewById(R.id.toast_image);
            image.setImageResource(R.drawable.icon);

            TextView textV = (TextView) layout.findViewById(R.id.toast_text);
            textV.setText(mText);

            Toast toast = new Toast(mContext);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(layout);
            toast.show();
        }
    }
}
