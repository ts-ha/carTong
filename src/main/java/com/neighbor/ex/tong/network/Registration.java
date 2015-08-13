package com.neighbor.ex.tong.network;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.neighbor.ex.tong.CONST;
import com.neighbor.ex.tong.R;
import com.neighbor.ex.tong.Utils;
import com.neighbor.ex.tong.ui.activity.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;

/**
 * Created by melissa on 15. 7. 5..
 */
public class Registration {

    private final static String REGIST = "http://211.189.132.184:8080/Tong/regMemberInfo.do?memberGmail=";

    private final static int REQUEST_OK = 1;
    private final static int REQUEST_FAIL = -1;

    private final static int TYPE_INFO = 1;
    private final static int TYPE_ERROR = 3;

    private Request mReq;
    private Context mCont;
    private SharedPreferences pref;
    private String mUID;

    interface setParam {
        void set(String email, String pw, String plate, String name, String uid);
    }

    public void action(Context conxt, String _id, String _pw, String _plate, String _name) {
        mCont = conxt;
        pref = PreferenceManager.getDefaultSharedPreferences(mCont);
        mUID = pref.getString(CONST.ACCOUNT_DEV,
                "APA91bFz6mBqYeiczpAVux9xVR2LwZm0PZbk7y9kHmJFe7g_cJpA58w-ek0pRDgNIjRIXV2yXIm9EWg_ldwgCFWa_LH44Y6SpXjduz_hwYzKzFyhfAcFOpOPEa3Aa2f6paUl7QHPrlSZ");

        mReq = new Request();
        mReq.set(_id, _pw, _plate, _name, mUID);
        mReq.execute();
    }

    class Request extends AsyncTask<Void, Void, Integer> implements setParam {
        String id = "";
        String psw = "";
        String license = "";
        String DeviceId = "";
        String NickName1 = "";
        ProgressDialog _dlg;
        int retValue = 0;

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                StringBuffer mkcheking = new StringBuffer();
                mkcheking.append(REGIST);
                mkcheking.append(URLEncoder.encode(id, "utf-8"));
//            mkcheking.append(id);
                mkcheking.append("&memberPw=");
                mkcheking.append(psw);
                mkcheking.append("&memberCarNum=");
                mkcheking.append(URLEncoder.encode(license, "utf-8"));

                mkcheking.append("&memberDeviceId=");
                mkcheking.append(DeviceId);
                mkcheking.append("&aliasName=");
                mkcheking.append(URLEncoder.encode(NickName1, "utf-8"));

                String rsjson = Utils.getJSON(mkcheking.toString(), 15000);
                Log.d("Tong", "++++" + mkcheking.toString() + "++++++");


                JSONObject json = new JSONObject(rsjson);
                String result = json.getString("RESULT");
                String cause = json.getString("MESSAGE");
                Log.d("Tong", "++++" + result + "++++++");
                Log.d("Tong", "++++" + cause + "++++++");
                if (result.equalsIgnoreCase("FALSE"))
                    retValue = REQUEST_FAIL;
                else
                    retValue = REQUEST_OK;

            } catch (JSONException e) {
                e.printStackTrace();
                retValue = REQUEST_FAIL;
            } catch (NullPointerException e) {
                e.printStackTrace();
                retValue = REQUEST_FAIL;
            } catch (Exception e) {
                e.printStackTrace();
                retValue = REQUEST_FAIL;
            }
            return retValue;
        }

        @Override
        protected void onPreExecute() {
            _dlg = new ProgressDialog(mCont);
            _dlg.setTitle("Please Wait..");
            _dlg.setMessage("Loading...");
            _dlg.setCancelable(false);
            _dlg.show();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            _dlg.dismiss();

            if (integer == REQUEST_FAIL) {
                showERR(mCont.getResources().getString(R.string.error_message), TYPE_ERROR);
            } else if (integer == REQUEST_OK) {
                Intent intent = new Intent(mCont, LoginActivity.class);
                mCont.startActivity(intent);
                ((Activity) mCont).finish();
            }
        }

        @Override
        public void set(String email, String pw, String plate, String name, String uid) {
            id = email;
            psw = pw;
            license = plate;
            NickName1 = name;
            DeviceId = uid;
        }

        void showERR(String msg, int kind) {

            String strTitle = (kind == TYPE_INFO) ?
                    mCont.getResources().getString(R.string.info_label) :
                    mCont.getResources().getString(R.string.error_title);

            AlertDialog.Builder alert = new AlertDialog.Builder(mCont);
            alert.setTitle(strTitle).setMessage(msg).
                    setCancelable(false).setPositiveButton(
                    mCont.getResources().getString(R.string.info_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
            alert.show();
        }
    }
}
