package com.neighbor.ex.tong.network;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import com.neighbor.ex.tong.R;
import com.neighbor.ex.tong.Utils;
import com.neighbor.ex.tong.ui.activity.CheckPlateResponse;
import com.neighbor.ex.tong.ui.activity.RegistrationActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class CheckPlate {

    private final static String CHECK_LICENSE = "http://211.189.132.184:8080/Tong/checkCarNum.do?carNum=";

    private final static int REQUEST_OK = 1;
    private final static int REQUEST_FAIL = -1;
    private final static int REQUEST_DUPLICATE = -3;

    private final static int TYPE_INFO = 1;
    private final static int TYPE_ERROR = 3;

    private Request mReq;
    private Context mCont;
    private CheckPlateResponse checkPlateResponse;
//    private SharedPreferences pref;


    public void action(Context context, String plate,
                       RegistrationActivity checkPlateResponse) {
        mCont = context;
//        pref = PreferenceManager.getDefaultSharedPreferences(mCont);
        this.checkPlateResponse = checkPlateResponse;
        mReq = new Request();
        mReq.set(plate);
        mReq.execute();
    }

    private class Request extends AsyncTask<Void, Void, Integer> {
        String _plate;
        ProgressDialog _dlg;

        public void set(String plate) {
            _plate = plate;
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
        protected Integer doInBackground(Void... params) {

            StringBuffer mkcheking = new StringBuffer();
            mkcheking.append(CHECK_LICENSE);
            mkcheking.append(_plate);

            String rsJson = Utils.getJSON(mkcheking.toString(), 15000);
            int retValue = 0;
            try {

                JSONObject json = new JSONObject(rsJson);
                String result = json.getString("RESULT");
                String cause = json.getString("MESSAGE");
                Log.d("Tong", "++++" + result + "++++++");
                Log.d("Tong", "++++" + cause + "++++++");

                if (result.equalsIgnoreCase("FALSE")) {
                    retValue = REQUEST_DUPLICATE;
                } else
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
        protected void onPostExecute(Integer integer) {
            _dlg.dismiss();

            if (integer == REQUEST_DUPLICATE) {
                showERR(mCont.getResources().getString(R.string.error_duplicate_plate), TYPE_INFO);
                checkPlateResponse.isPlateCheckd(false);
            } else if (integer == REQUEST_OK) {
                checkPlateResponse.isPlateCheckd(true);
                showERR(mCont.getResources().getString(R.string.error_none_plate), TYPE_INFO);
            } else {
                showERR(mCont.getResources().getString(R.string.error_message), TYPE_ERROR);
                checkPlateResponse.isPlateCheckd(false);
            }

        }

        void showERR(String msg, int kind) {

            String strTitle = (kind == TYPE_INFO) ? "안내" : "!오류";

            AlertDialog.Builder alert = new AlertDialog.Builder(mCont);
            alert.setTitle(strTitle).setMessage(msg).
                    setCancelable(false).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            alert.show();
        }
    }
}
