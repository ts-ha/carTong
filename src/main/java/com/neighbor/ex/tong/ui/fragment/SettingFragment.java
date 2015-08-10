package com.neighbor.ex.tong.ui.fragment;


import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.neighbor.ex.tong.CONST;
import com.neighbor.ex.tong.R;
import com.neighbor.ex.tong.common.Common;
import com.neighbor.ex.tong.network.NetworkManager;
import com.neighbor.ex.tong.ui.activity.LoginActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingFragment extends Fragment {
    private List<String> rdStting;
    private SharedPreferences pref;
    private TextView mNickName;
    private TextView carNumber;
    Handler mHandler;

    {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                }
            }
        };
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final String[] radiusArray = getResources().getStringArray(R.array.radius_array);
        rdStting = new ArrayList<String>(Arrays.asList(radiusArray));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View mainView = inflater.inflate(R.layout.fragment_setting, container, false);

        carNumber = (TextView) mainView.findViewById(R.id.user_carNo);
        Button mLogoutButton = (Button) mainView.findViewById(R.id.buttonLogout);
        ImageButton mPwButton = (ImageButton) mainView.findViewById(R.id.pw_setting);
        ImageButton mChangNickNameButton = (ImageButton) mainView.findViewById(R.id.buttonChangNickName);
        mChangNickNameButton.setOnClickListener(onClickListener);
        mLogoutButton.setOnClickListener(onClickListener);
        mPwButton.setOnClickListener(onClickListener);

        mNickName = (TextView) mainView.findViewById(R.id.user_nickName);

        carNumber.setText(pref.getString(CONST.ACCOUNT_LICENSE, ""));
        mNickName.setText(pref.getString(CONST.ACCOUNT_NAME, ""));

        Spinner radiusSetting = (Spinner) mainView.findViewById(R.id.radius_setting);
        ArrayAdapter<String> ad = new ArrayAdapter(getActivity(), R.layout.spinner_row, rdStting);
        radiusSetting.setAdapter(ad);
        radiusSetting.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String txRadius = rdStting.get(i);
                SharedPreferences.Editor edittor = pref.edit();
                edittor.putString("radius", txRadius);
                edittor.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }

        });

        int index = rdStting.indexOf(pref.getString("radius", "1km"));
        radiusSetting.setSelection(index);

        return mainView;
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.buttonLogout:
                    SharedPreferences.Editor edittor = pref.edit();
                    edittor.putString(CONST.ACCOUNT_ID, "");
                    edittor.putString(CONST.ACCOUNT_PSW, "");
                    edittor.commit();
                    Intent intent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                    break;
                case R.id.buttonChangNickName:
                    showChangNickNameDialog();
                    break;
                case R.id.pw_setting:
                    showChangPwDialog();
                default:
            }
        }
    };

    private void showChangPwDialog() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_password, null);
        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setMessage("변경할 비밀번호를 입력하세요.");
        alert.setView(view);
        final EditText accountLicense = (EditText) view.findViewById(R.id.group_input);
        final EditText desc = (EditText) view.findViewById(R.id.group_desc);

        alert.setCancelable(false);
        alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alert.setPositiveButton("변경", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (accountLicense.getText().length() > 0) {
                    if (accountLicense.getText().toString().equalsIgnoreCase(desc.getText().toString())) {
                        SharedPreferences.Editor edittor = pref.edit();
                        edittor.putString(CONST.ACCOUNT_PSW, accountLicense.getText().toString());
                        edittor.commit();
                        String url = "http://211.189.132.184:8080/Tong/api/changeMemberInfo.do?memberPw=%s&aliasName=&memberGmail=%s";
                        url = String.format(url, accountLicense.getText().toString(),
                                pref.getString(CONST.ACCOUNT_ID, ""));
                        NetworkManager.getInstance(getActivity()).requestJsonObject(getActivity(), url,
                                Request.Method.GET, mHandler,
                                Common.HANDLE_SUCCESS_REGIST_ACCIDENT, Common.HANDLE_FAIL_REGIST_ACCIDENT);
                    } else {
                        Toast.makeText(getActivity().getApplication(), "비밀번호가 일치하지 않습니다. 다시확인하세요.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getActivity().getApplication(), "변경할 미빌번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    private void showChangNickNameDialog() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_make_group, null);
        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setMessage("변경할 차량번호를 입력하세요.");
        alert.setView(view);
        final EditText accountLicense = (EditText) view.findViewById(R.id.group_input);
        accountLicense.setHint(carNumber.getText().toString());
        alert.setCancelable(false);
        alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alert.setPositiveButton("변경", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (accountLicense.getText().length() > 0) {
                    SharedPreferences.Editor edittor = pref.edit();
                    edittor.putString(CONST.ACCOUNT_LICENSE, accountLicense.getText().toString());
                    edittor.commit();
                    carNumber.setText(accountLicense.getText().toString());
                    String url = "http://211.189.132.184:8080/Tong/api/changeMyCarNum.do?memberGmail=%s&carNum=%s";
                    url = String.format(url, pref.getString(CONST.ACCOUNT_ID, ""),
                            pref.getString(CONST.ACCOUNT_LICENSE, ""));
                    Log.d("hts", "changeMyCarNum : " + url);
                    NetworkManager.getInstance(getActivity()).requestJsonObject(getActivity(), url,
                            Request.Method.GET, mHandler,
                            Common.HANDLE_SUCCESS_REGIST_ACCIDENT, Common.HANDLE_FAIL_REGIST_ACCIDENT);
                } else {
                    Toast.makeText(getActivity().getApplication(), "닉네임이 비어 있습니다.", Toast.LENGTH_LONG).show();
                }
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }


}
