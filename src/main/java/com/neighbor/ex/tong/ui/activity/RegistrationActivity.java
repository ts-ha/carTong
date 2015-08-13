package com.neighbor.ex.tong.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.neighbor.ex.tong.R;
import com.neighbor.ex.tong.network.CheckID;
import com.neighbor.ex.tong.network.CheckPlate;
import com.neighbor.ex.tong.network.Registration;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistrationActivity extends Activity implements
        CheckPlateResponse, CheckIdResponse {

    private final int REQUEST_NOT_FORMAL = -2;
    private final int REQUEST_EMPTY = -3;
    private boolean isIdCheckd;
    private boolean isPlateCheckd;
    private EditText userID;
    private EditText userPlateNo;
    private EditText userPW;
    private EditText userNickName;

    @Override
    public void isIdCheckd(boolean flg) {
        this.isIdCheckd = flg;
    }

    @Override
    public void isPlateCheckd(boolean flg) {
        this.isPlateCheckd = flg;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == REQUEST_NOT_FORMAL) {
                showERR(getResources().getString(R.string.error_non_formal));
            } else if (REQUEST_EMPTY == msg.what) {
                showERR(getResources().getString(R.string.error_any_empty));
            }
        }
    };

    private void showERR(String msg) {

        String strTitle = getResources().getString(R.string.error_title);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(strTitle).setMessage(msg).
                setCancelable(false).setPositiveButton(getResources().getString(R.string.info_ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        alert.show();
    }

    boolean isValidID(String email) {
        boolean err = false;
        Log.d("hts", "isValidID email" + email);

//        String regex = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\\\w+\\\\.)+\\\\w+$";
        String regex = "^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$";
        Pattern p = Pattern.compile(regex);
        return Pattern.matches(regex, email);
//        if (m.matches()) {
//            err = true;
//        }
//        return err;
    }

    private void hideKeyboard() {
        InputMethodManager inputManager =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        ImageButton goHome = (ImageButton) findViewById(R.id.back_home);
        goHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        userID = (EditText) findViewById(R.id.regist_id);
        userPlateNo = (EditText) findViewById(R.id.regist_plateNumber);

        userPW = (EditText) findViewById(R.id.regist_psw);
        userNickName = (EditText) findViewById(R.id.regist_name1);

        userNickName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_DONE:
                        hideKeyboard();
                        joinStart();
                        break;
                }
                return false;
            }
        });


        Button checkID = (Button) findViewById(R.id.check_id);
        checkID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String userId = userID.getText().toString();
                if (userId.length() > 0) {
//                    if (!isValidID(userId)) {
//                        handler.sendEmptyMessage(REQUEST_NOT_FORMAL);
//                    } else {
                        new CheckID().action(RegistrationActivity.this, userId, RegistrationActivity.this);
                        hideKeyboard();
//                    }
                }
            }
        });

        Button checkPlateNo = (Button) findViewById(R.id.check_plate);
        checkPlateNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String plateNo = userPlateNo.getText().toString().trim();
                String mach = "^\\d{2}[가-힝]\\d{4}$";
                Pattern p = Pattern.compile(mach);
                Matcher m = p.matcher(userPlateNo.getText().toString().trim());
                if (m.matches()) {
                    if (plateNo.length() <= 0) {
                        handler.sendEmptyMessage(REQUEST_EMPTY);
                    } else {
                        CheckPlate plate = new CheckPlate();
                        plate.action(RegistrationActivity.this, plateNo, RegistrationActivity.this);
                    }
                } else {
                    Toast.makeText(RegistrationActivity.this,
                            "올바른 차량 번호 형식이 아닙니다.", Toast.LENGTH_LONG).show();
                }
            }
        });

        Button submit = (Button) findViewById(R.id.regist_submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                joinStart();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    private void joinStart() {
        String mach = "^\\d{2}[가-힝]\\d{4}$";
        Pattern p = Pattern.compile(mach);
        Matcher m = p.matcher(userPlateNo.getText().toString().trim());
        if (isIdCheckd && isIdCheckd) {
            if (m.matches()) {
                if ((userID.getText().length()) > 0 && (userPW.getText().length() > 0) &&
                        (userPlateNo.getText().length() > 0) &&
                        (userNickName.getText().length() > 0)) {
                    hideKeyboard();
                    String userPlateNoString = userPlateNo.getText().toString();
                    String NickNameString = userNickName.getText().toString();
//                    try {
//                        userPlateNoString = URLEncoder.encode(userPlateNoString, "utf-8");
//                        NickNameString = URLEncoder.encode(NickNameString, "utf-8");
//                    } catch (UnsupportedEncodingException e) {
//                        e.printStackTrace();
//                    }
                    new Registration().action(RegistrationActivity.this, userID.getText().toString(),
                            userPW.getText().toString(), userPlateNoString,
                            NickNameString);
                } else {
                    handler.sendEmptyMessage(REQUEST_EMPTY);
                }
            } else {
                Toast.makeText(RegistrationActivity.this,
                        "올바른 차량 번호 형식이 아닙니다.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(RegistrationActivity.this,
                    "ID, 차량번호 중복 체크를 해주세요.", Toast.LENGTH_LONG).show();
        }
    }
}
