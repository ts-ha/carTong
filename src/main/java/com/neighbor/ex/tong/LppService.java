package com.neighbor.ex.tong;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lpp.client.aidl.MIXED_DATA;
import com.neighbor.ex.tong.common.Common;
import com.neighbor.ex.tong.common.ToastMaster;
import com.neighbor.ex.tong.network.parser.Push;
import com.neighbor.ex.tong.provider.DataProvider;
import com.neighbor.ex.tong.ui.activity.LoginActivity;
import com.neighbor.lpm.LpmBaseService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LppService extends LpmBaseService {

    private static final String TAG = "LppService";

    // ���� Ǫ�� ���� ���̵�
//	private final static String SERVICE_ID = "SampleSDS1";
////	private final static String SERVICE_ID = "CICWork";
//	private final static String SERVICE_PWD = "sdslpp";
//	private final static String SERVER_IP = "70.12.230.154";
//	private final static int SERVER_PORT = 2889;

    private final static String SERVICE_ID = "NBR1";
    private final static String SERVICE_PWD = "123456";
    private final static String SERVER_IP = "211.189.132.177";
    private final static int SERVER_PORT = 2889;

//	private final static String SERVICE_ID = "CIWork";
//	private final static String SERVICE_PWD = "123456";
//	private final static String SERVER_IP = "121.252.241.78";
//	private final static int SERVER_PORT = 2889;

    private final static String FILE_PATH = Environment.getExternalStorageDirectory().getPath() + File.separator
            + "LppClient" + File.separator + SERVICE_ID + File.separator;

    private Handler toastHandler;
    private String toastMessage;

    public LppService() {
        super(LppService.class, SERVICE_ID, SERVICE_PWD, SERVER_IP, SERVER_PORT, FILE_PATH);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (toastHandler == null) {
            toastHandler = new Handler();
        }

        Log.i(TAG, "[LppService] onStartCommand()");

        // Service�� ���� ����Ǿ��� ��� �ý����� �ٽ� Service�� ����� ���� ������
        // intent ���� null�� �ʱ�ȭ ���Ѽ� ����� �մϴ�.
        // return START_NOT_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    // ========================================================================
    // ========================================================================

    @Override
    protected void onServerStatus(Context context, int status, int result) {
        if (mLocalCallback != null) {
            mLocalCallback.serverStatus(status, result);
        }
    }

    @Override
    protected void onServiceType(Context context, int pushType, int dataType) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onReceiveText(Context context, boolean bNotify, String strGroupId, String strMessageId, String strSenderId, String strReceiverId, String strMessage) {
//        allMessage(context, strGroupId, strMessageId, strSenderId, strReceiverId, strMessage);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final Push pushMessage = gson.fromJson(strMessage, Push.class);

        boolean isRunning = false;

        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> services = am.getRunningTasks(Integer.MAX_VALUE);
        if (services.get(0).topActivity.getPackageName().toString()
                .equalsIgnoreCase(this.getPackageName().toString())) {
            isRunning = true;
        }

        if (pushMessage.getMsg() != null) {
            sendGCMIntent(context, pushMessage);
            if (false == isRunning) {
                Uri path = getSoundPath(pushMessage.getMsg());
                int NOTI_ID = 0;
                int icon = R.drawable.icon;
                String title = context.getString(R.string.app_name);
                Intent intent = new Intent(context, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra(Common.PUSH_NOIT, Common.PUSH_NOIT);

                PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                Notification notification = new Notification.Builder(context)
                        .setContentTitle(title)
                        .setContentText(pushMessage.getMsg())
                        .setSmallIcon(icon)
                        .setContentIntent(pIntent)
                        .setAutoCancel(true)
                        .setSound(path)
                        .getNotification();
                NotificationManager mNotiManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotiManager.notify(NOTI_ID, notification);
            } else {
//                toastMessage = strMessage;
                toastHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        ToastMaster.showLong(getApplicationContext(), pushMessage.getSendCarNum() + " : " + pushMessage.getMsg());
                    }
                });
            }
        }
    }

    private void sendGCMIntent(Context ctx, Push message) {

        SharedPreferences pref =
                PreferenceManager.getDefaultSharedPreferences(ctx);

        ContentValues cv = new ContentValues();

        cv.put("CONTENTS", message.getMsg());
        cv.put("RECOMM_CNT", 0);
        cv.put("DoSend", "false");
        cv.put("RECEIVER", pref.getString(CONST.ACCOUNT_LICENSE, ""));
        cv.put("SENDER", message.getSendCarNum());
        cv.put("TIME", Utils.getDateTime());
        cv.put("SEQ", message.getSeq());
        ctx.getContentResolver().insert(DataProvider.TONG_URI, cv);

        Intent broadcast = new Intent(CONST.TONG_MESSAGE_UPDATE);

        sendBroadcast(broadcast);
    }

    private void allMessage(Context context, String strGroupId, String strMessageId, String strSenderId, String strReceiverId, String strMessage) {
        generateNotification(context, "메세지 수신 : " + toastMessage);
    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    @SuppressWarnings("deprecation")
    private void generateNotification(Context context, String message) {


        boolean isRunning = false;
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> services = am.getRunningTasks(Integer.MAX_VALUE);
        if (services.get(0).topActivity.getPackageName().toString()
                .equalsIgnoreCase(this.getPackageName().toString())) {
            isRunning = true;
        }

        if (false == isRunning) {
            Uri path = getSoundPath(message);
            int NOTI_ID = 0;
            int icon = R.drawable.icon;
            String title = context.getString(R.string.app_name);

            Intent intent = new Intent(context, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            Notification notification = new Notification.Builder(context)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setSmallIcon(icon)
                    .setContentIntent(pIntent)
                    .setAutoCancel(true)
                    .getNotification();
            NotificationManager mNotiManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotiManager.notify(NOTI_ID, notification);
        } else {
//            toastMessage = strMessage;
//            toastHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    ToastMaster.showLong(getApplicationContext(), toastMessage);
//                }
//            });
        }

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

    @Override
    protected void onReceiveFile(Context context, boolean bNotify, String strGroupId, String strMessageId, String strSenderId, String strReceiverId,
                                 int nFileType, int fileSize, String strFileExt, String strFilePath) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onReceiveMixed(Context context, boolean bNotify, String strGroupId, String strMessageId, String strSenderId, String strReceiverId,
                                  List<MIXED_DATA> mixedInfo) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onSendDataResult(Context context, String strMessageId, int result, int totalSendCount, int ackUserCount) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onDataNotify(Context context, int dataType, String strGroupId, String strSenderId, String strMessageId) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onDataDownloadFail(Context context, int dataType, String strMessageId, int nErrorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onError(Context context, int errorId) {
        // TODO Auto-generated method stub

    }

    // ========================================================================
    // ========================================================================

    private final IBinder localBinder = new LocalBinder();
    private LocalCallback mLocalCallback = null;

    public class LocalBinder extends Binder {
        public LppService getService() {
            return LppService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }

    public void setRegistCallback(LocalCallback localCallback) {
        mLocalCallback = localCallback;
    }

    public interface LocalCallback {
        /* Ǫ�� ���� ���� ���� */
        void serverStatus(int status, int result);

        void tokenChange(boolean isRequest, String strToken);
    }

    @Override
    protected void onAddGroupNotify(Context context, String strGroupId, String strMessageId, int nErrorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onAddGroupUserNotify(Context context, String strGroupId, String strMessageId, int nErrorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onDeleteGroupNotify(Context context, String strGroupId, String strMessageId, int nErrorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onDeleteGroupUserNotify(Context context, String strGroupId, String strMessageId, int nErrorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onPushEnableNotify(boolean bEnable) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onGroupUser(int result, ArrayList<String> listUser) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onLppToken(boolean isNewToken, boolean isRequest, String token) {
        if (mLocalCallback != null) mLocalCallback.tokenChange(isRequest, token);
    }

    @Override
    protected void onDeleteAccount() {
        if (mLocalCallback != null) mLocalCallback.serverStatus(getLpmStatus(), 0);
         /*
         * 관리자가 해당 계정(토큰)을 삭제한 경우 올라오는 콜백으로
		 * 서비스를 계속 이용하고자 한다면  아래 코드의 주석을 해제하여,
		 * 서비스 재등록을 위해 토큰을 다시 요청한다.
		 */
        //getLppToken();
    }
}
