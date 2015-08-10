package com.neighbor.ex.tong.ui.activity;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.neighbor.ex.tong.CONST;
import com.neighbor.ex.tong.R;
import com.neighbor.ex.tong.Utils;
import com.neighbor.ex.tong.common.Common;

import org.json.JSONException;
import org.json.JSONObject;


public class LoginActivity extends Activity {

    private final static int TYPE_INFO = 1;
    private final static int TYPE_ERROR = 3;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText editEmail = (EditText) findViewById(R.id.user_email);
        final EditText editPSW = (EditText) findViewById(R.id.user_psw);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String id = prefs.getString(CONST.ACCOUNT_ID, "");
        String pw = prefs.getString(CONST.ACCOUNT_PSW, "");

        Bundle bundle = getIntent().getExtras();
        String pushNoit = "";

        if (null != bundle) {
            pushNoit = bundle.getString(Common.PUSH_NOIT, Common.PUSH_NOIT);
        }

        Button registration = (Button) findViewById(R.id.user_registration);
        registration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(intent);
            }
        });


        editPSW.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_DONE:
                        Request req = new Request();
                        req.set(LoginActivity.this, editEmail.getText().toString(),
                                editPSW.getText().toString());
                        req.execute();
                        break;
                    default:
                        return false;
                }
                return false;
            }
        });

        Button loginBtn = (Button) findViewById(R.id.user_confirm);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();

                if (editEmail.getText().length() > 0 &&
                        editPSW.getText().length() > 0) {
                    Request req = new Request();
                    req.set(LoginActivity.this, editEmail.getText().toString(),
                            editPSW.getText().toString());
                    req.execute();
                } else {
                    showERR(getResources().getString(R.string.error_empty),
                            TYPE_ERROR);
                }
            }
        });

        if (id.length() > 0 && pw.length() > 0) {
            Intent intent = new Intent(LoginActivity.this, MainActivity2Activity.class);
            Log.d("hts", "LoginActivity pushNoit : " + pushNoit);
            if (pushNoit != null && !pushNoit.isEmpty()) {
                intent.putExtra(Common.PUSH_NOIT, Common.PUSH_NOIT);
            }
            startActivity(intent);
            finish();
        }
    }

    private void hideKeyboard() {
        InputMethodManager inputManager =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    void showERR(String msg, int kind) {

        String strTitle = (kind == TYPE_INFO) ? "안내" : "!오류";

        AlertDialog.Builder alert = new AlertDialog.Builder(LoginActivity.this);
        alert.setTitle(strTitle).setMessage(msg).
                setCancelable(false).setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alert.show();
    }

    interface setParam {
        void set(Context cont, String email, String pw);
    }

    class Request extends AsyncTask<Void, Void, Integer> implements setParam {

        private final static String LOGIN = "http://211.189.132.184:8080/Tong/allow.do?gmail=";

        private final static int REQUEST_OK = 1;
        private final static int REQUEST_FAIL = -1;
        private final static int REQUST_REJECT = -2;

        String psw;
        String id;
        ProgressDialog _dlg;
        Context _context;

        @Override
        protected Integer doInBackground(Void... params) {
            StringBuffer mkcheking = new StringBuffer();
            mkcheking.append(LOGIN);
            mkcheking.append(id);
            mkcheking.append("&passwd=");
            mkcheking.append(psw);

            int retValue = 0;

            try {
                String rsJson = Utils.getJSON(mkcheking.toString(), 15000);
                JSONObject json = new JSONObject(rsJson);

                String carNum = json.getString("MEMBER_CAR_NUM");
                String devID = json.getString("MEMBER_DEVICE_ID");
                String cafeName = json.getString("ALIAS_NAME");

                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(CONST.ACCOUNT_LICENSE, carNum);
                editor.putString(CONST.ACCOUNT_DEV, devID);
                editor.putString(CONST.ACCOUNT_NAME, cafeName);
                editor.putString(CONST.ACCOUNT_ID, id);
                editor.putString(CONST.ACCOUNT_PSW, psw);

                editor.commit();
                retValue = REQUEST_OK;

            } catch (JSONException e) {
                e.printStackTrace();
                retValue = REQUST_REJECT;
            } catch (Exception e) {
                e.printStackTrace();
                retValue = REQUEST_FAIL;
            }
            return retValue;
        }

        @Override
        protected void onPreExecute() {
            _dlg = new ProgressDialog(_context);
            _dlg.setTitle("Please Wait..");
            _dlg.setMessage("Loading...");
            _dlg.setCancelable(false);
            _dlg.show();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            _dlg.dismiss();

            if (integer == REQUEST_FAIL) {
                showERR(_context.getResources().getString(R.string.error_message), TYPE_ERROR);
            } else if (integer == REQUEST_OK) {
                Intent intent = new Intent(LoginActivity.this, MainActivity2Activity.class);
                LoginActivity.this.startActivity(intent);
                ((Activity) _context).finish();
            } else if (integer == REQUST_REJECT) {
                showERR(_context.getResources().getString(R.string.error_incorrent_account),
                        TYPE_ERROR);
            }
        }

        @Override
        public void set(Context cont, String email, String pw) {
            _context = cont;
            id = email;
            psw = pw;
        }
    }
}

