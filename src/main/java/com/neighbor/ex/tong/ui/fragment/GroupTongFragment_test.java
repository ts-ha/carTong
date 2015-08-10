package com.neighbor.ex.tong.ui.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.neighbor.ex.tong.CONST;
import com.neighbor.ex.tong.R;
import com.neighbor.ex.tong.Utils;
import com.neighbor.ex.tong.common.Common;
import com.neighbor.ex.tong.network.NetworkManager;
import com.neighbor.ex.tong.network.RequestMakeGroup;
import com.neighbor.ex.tong.network.parser.SearchGroupList;
import com.neighbor.ex.tong.ui.activity.GroupActivity;
import com.neighbor.ex.tong.ui.activity.MainActivity2Activity;
import com.neighbor.ex.tong.ui.adapter.GroupAdapter;
import com.neighbor.ex.tong.ui.adapter.MyGroupAdapter;
import com.neighbor.ex.tong.ui.adapter.SearchGroupAdapter;
import com.neighbor.ex.tong.ui.dialog.CommonProgressDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;


public class GroupTongFragment_test extends Fragment {

    private final static String MY_GROUP_LIST = "http://211.189.132.184:8080/Tong/api/selectRoomMyList.do?memberGmail=";

    private final static String ALL_GROUP_LIST = "http://211.189.132.184:8080/Tong/api/selectRoomAllList.do?memberGmail=";
    private final static String GROUP_JOIN_URL = "http://211.189.132.184:8080/Tong/regTongMemberInfo.do?roomId=%s&joinMemberGmail=%s";
    private final static String MAKE_GROUP_URL =
            "http://211.189.132.184:8080/Tong/regTongInfo.do?roomMasterGmail=%s&roomName=%s&roomDesc=%s";
    private static final String TAG = "GroupTongFragment_test";

    private EditText groupInput;
    private String mMode, userID;

    private ArrayList<HashMap<String, String>> myList;
    private ArrayList<HashMap<String, String>> totalList;
    private RequestMakeGroup mNewGroup;
    private SharedPreferences pref;
    private MyGroupAdapter myAdapter;
    private GroupAdapter groupAdapter;
    private final int HANDLE_FAIL_REG_TONG_MEMBER_INFO = 1000;
    private final int HANDLE_SUCCESS_REG_TONG_MEMBER_INFO = HANDLE_FAIL_REG_TONG_MEMBER_INFO + 1;
    private final int HANDLE_SUCCESS_REG_TONG_INFO = HANDLE_SUCCESS_REG_TONG_MEMBER_INFO + 1;
    private final int HANDLE_FAIL_REG_TONG_INFO = HANDLE_SUCCESS_REG_TONG_INFO + 1;
    private final int HANDLE_SUCCESS_SEARCH_ROOM_LIST_ID = HANDLE_FAIL_REG_TONG_INFO + 1;
    private final int HANDLE_FAIL_SEARCH_ROOM_LIST_ID = HANDLE_SUCCESS_SEARCH_ROOM_LIST_ID + 1;


    private LinearLayout mAllGroupLinear, mSearchGroupLinear, mMyGroupLinear;
    private RadioButton mMyGroupRadioBtn, mSearchGroupRadioBtn, mAllGroupRadioBtn;
    private ListView mAllList, mMyList, mSearchList;
    private TextView mResultText;


    private Handler
            mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ((MainActivity2Activity) getActivity()).hideKeyboard();
            switch (msg.what) {
                case HANDLE_FAIL_REG_TONG_MEMBER_INFO:
                    break;
                case HANDLE_SUCCESS_REG_TONG_MEMBER_INFO:
                    mMode = "groups";
                    Request request = new Request(getActivity(), MakeUri(mMode));
                    request.execute();
                    break;
                case HANDLE_SUCCESS_REG_TONG_INFO:
                    setView(1);
                    break;
                case HANDLE_FAIL_REG_TONG_INFO:
                    break;
                case HANDLE_SUCCESS_SEARCH_ROOM_LIST_ID:
                    meditKeywordText.setText("");
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    SearchGroupList searchGroupList = gson.fromJson((String) msg.obj, SearchGroupList.class);
                    Log.d("hts", "msg.obj" + (String) msg.obj);
                    if (searchGroupList.getRows() != null && searchGroupList.getRows().size() > 0) {
                        Log.d("hts", "getRows" + searchGroupList.getRows().toString());
                        mResultText.setVisibility(View.GONE);
                        SearchGroupAdapter searchGroupAdapter
                                = new SearchGroupAdapter(getActivity(), 0, 0, searchGroupList.getRows(), userID);
                        mSearchList.setAdapter(searchGroupAdapter);
                    } else {
                        mSearchList.setAdapter(null);
                        mResultText.setVisibility(View.VISIBLE);
                    }
                    break;
                case HANDLE_FAIL_SEARCH_ROOM_LIST_ID:
                    meditKeywordText.setText("");
                    break;
                default:
            }

        }
    };
    //    private Spinner mSearchTypeSpinner;
    private EditText meditKeywordText;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        userID = pref.getString(CONST.ACCOUNT_ID, "");
        myList = new ArrayList<>();
        totalList = new ArrayList<>();
        mMode = "mylist";
        myAdapter = new MyGroupAdapter(getActivity(), 0, myList);
        groupAdapter = new GroupAdapter(getActivity(), 0, totalList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_group_tong_test, container, false);
        RadioGroup radioGroup = (RadioGroup) v.findViewById(R.id.radioGroupMain);
        mMyGroupRadioBtn = (RadioButton) v.findViewById(R.id.radioButtonMyGroup);
        mSearchGroupRadioBtn = (RadioButton) v.findViewById(R.id.radioButtonSearchGroup);
        mAllGroupRadioBtn = (RadioButton) v.findViewById(R.id.radioButtonAllGroup);
        mAllGroupLinear = (LinearLayout) v.findViewById(R.id.linearLayoutAllGroupList);
        mMyGroupLinear = (LinearLayout) v.findViewById(R.id.linearLayoutMyGroupList);
        mSearchGroupLinear = (LinearLayout) v.findViewById(R.id.linearLayoutSearchGroup);
        mAllList = (ListView) v.findViewById(R.id.listViewAllGroup);
        mMyList = (ListView) v.findViewById(R.id.listViewMyGroup);
        mSearchList = (ListView) v.findViewById(R.id.listViewSearchGroup);
        mResultText = (TextView) v.findViewById(R.id.textViewResult);
        meditKeywordText = (EditText) v.findViewById(R.id.editTextKeyword);
        meditKeywordText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_DONE:
                        searchKeyword();
                        break;
                    default:
                        return false;
                }
                return false;
            }
        });

        ((Button) v.findViewById(R.id.buttonSearch)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchKeyword();
            }
        });

        ((ImageButton) v.findViewById(R.id.make_group)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeDialog();
            }
        });


        mMyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final HashMap<String, String> item = myList.get(i);
                if (item.get(Common.USE_FLAG) != null && !item.get(Common.USE_FLAG).equalsIgnoreCase("0")) {
                    Intent intent = new Intent(getActivity(), GroupActivity.class);
                    intent.putExtra("roomName", item.get(Common.ROOM_NAME));
                    intent.putExtra("roomId", item.get(Common.ROOM_ID));
                    intent.putExtra("ROOM_MASTER_GMAIL", item.get(Common.ROOM_OWNER));
                    intent.putExtra(Common.ROOM_DESC, item.get(Common.ROOM_DESC));
                    intent.putExtra(Common.ROOM_MASTER_CAR_NUM, item.get(Common.ROOM_MASTER_CAR_NUM));
                    intent.putExtra(Common.REG_DATE, item.get(Common.REG_DATE));
                    intent.putExtra(Common.MEMBER_COUNT, item.get(Common.MEMBER_COUNT));
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity().getApplicationContext(),
                            "가입승인 필요합니다,", Toast.LENGTH_LONG).show();
                }
            }
        });
        mSearchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SearchGroupList.Row item = (SearchGroupList.Row) adapterView.getItemAtPosition(i);
                if (item.getUSE_FLAG() != null && !item.getUSE_FLAG().equalsIgnoreCase("0")) {
                    Intent intent = new Intent(getActivity(), GroupActivity.class);
                    intent.putExtra("roomName", item.getROOM_NAME());
                    intent.putExtra("roomId", item.getROOM_ID());
                    intent.putExtra("ROOM_MASTER_GMAIL", item.getROOM_MASTER_GMAIL());
                    intent.putExtra(Common.ROOM_DESC, item.getROOM_DESC());
                    intent.putExtra(Common.ROOM_MASTER_CAR_NUM, item.getROOM_MASTER_CAR_NUM());
                    intent.putExtra(Common.REG_DATE, item.getREG_DATE());
                    intent.putExtra(Common.MEMBER_COUNT, item.getMEMBER_COUNT());
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity().getApplicationContext(),
                            "가입승인 필요합니다,", Toast.LENGTH_LONG).show();
                }
            }
        });

        mAllList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                userID = pref.getString(CONST.ACCOUNT_ID, "");
                HashMap<String, String> item = totalList.get(position);
                String url = String.format(GROUP_JOIN_URL, item.get(Common.ROOM_ID),
                        pref.getString(CONST.ACCOUNT_ID, ""));
                NetworkManager.getInstance(getActivity()).requestJsonObject(getActivity(), url,
                        com.android.volley.Request.Method.GET, mHandler,
                        HANDLE_SUCCESS_REG_TONG_MEMBER_INFO, HANDLE_FAIL_REG_TONG_MEMBER_INFO);
            }
        });
        radioGroup.check(R.id.radioButtonMyGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioButtonMyGroup) {
                    setView(1);
                } else if (checkedId == R.id.radioButtonSearchGroup) {
                    setView(3);
                } else {
                    setView(2);
                }
            }
        });

//        radioGroup.check(R.id.radioButtonMyGroup);
//        mMyGroupRadioBtn.setSelected(true);
        return v;
    }

    private void searchKeyword() {
        String keyword = meditKeywordText.getText().toString();
        if (keyword.length() > 0) {
            String url;
            url = "http://211.189.132.184:8080/Tong/api/searchRoomList.do?memberGmail=%s&keyword=%s";
            userID = pref.getString(CONST.ACCOUNT_ID, "");

            try {
                keyword = URLEncoder.encode(keyword, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            url = String.format(url, userID, keyword);
            NetworkManager.getInstance(getActivity()).requestJsonObject(getActivity(), url,
                    com.android.volley.Request.Method.GET, mHandler,
                    HANDLE_SUCCESS_SEARCH_ROOM_LIST_ID, HANDLE_FAIL_SEARCH_ROOM_LIST_ID);
        } else {
            Toast.makeText(getActivity(), "키워드를 입력하세요.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setView(int index) {
        ((MainActivity2Activity) getActivity()).hideKeyboard();
        Request request;
        Log.d(TAG, "setView " + index);
        if (index == 1) {
            mMode = "mylist";
            request = new Request(getActivity(), MakeUri(mMode));
            request.execute();
            mAllGroupLinear.setVisibility(View.GONE);
            mMyGroupLinear.setVisibility(View.VISIBLE);
            mSearchGroupLinear.setVisibility(View.GONE);
        } else if (index == 2) {
            mMode = "groups";
            request = new Request(getActivity(), MakeUri(mMode));
            request.execute();
            mMyGroupLinear.setVisibility(View.GONE);
            mAllGroupLinear.setVisibility(View.VISIBLE);
            mSearchGroupLinear.setVisibility(View.GONE);
        } else {
            mAllGroupLinear.setVisibility(View.GONE);
            mMyGroupLinear.setVisibility(View.GONE);
            mSearchGroupLinear.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        String uri = MakeUri(mMode);
        Request request = new Request(getActivity().getApplication(), uri);
        request.execute();
    }


    class Request extends AsyncTask<Void, Void, ArrayList<HashMap<String, String>>> {

        private String uri;

        public Request(Context cont, String reqURL) {
            uri = reqURL;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            myList.clear();
            totalList.clear();
            CommonProgressDialog.showProgressDialog(getActivity());
        }

        @Override
        protected void onPostExecute(ArrayList<HashMap<String, String>> aVoid) {
            super.onPostExecute(aVoid);
            CommonProgressDialog.hideProgress();
            if (aVoid.size() > 0) {
                mResultText.setVisibility(View.GONE);
            } else {
                mResultText.setVisibility(View.VISIBLE);
            }
            try {
                Log.d("hts", "mMode  : " + mMode);
                if (mMode.equalsIgnoreCase("mylist")) {
                    mMyList.setAdapter(myAdapter);
                    myAdapter.notifyDataSetChanged();
                } else {
                    totalList = aVoid;
                    groupAdapter = new GroupAdapter(getActivity(), 0, aVoid);
                    mAllList.setAdapter(groupAdapter);
                    groupAdapter.notifyDataSetChanged();

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected ArrayList<HashMap<String, String>> doInBackground(Void... params) {
            try {
                String rsJson = Utils.getJSON(uri, 15000);
                JSONArray jArray = new JSONArray(rsJson);

                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject c = jArray.getJSONObject(i);
                    String roomID = c.getString(Common.ROOM_ID);
                    String roomName = c.getString(Common.ROOM_NAME);
                    String roomOwner = c.getString(Common.ROOM_OWNER);
                    String member_count = c.getString(Common.MEMBER_COUNT);
                    String reg_date = c.getString(Common.REG_DATE);
                    String carNum = c.getString(Common.ROOM_MASTER_CAR_NUM);
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put(Common.ROOM_ID, URLDecoder.decode(roomID, "UTF-8"));
                    map.put(Common.ROOM_NAME, URLDecoder.decode(roomName, "UTF-8"));
                    map.put(Common.ROOM_OWNER, URLDecoder.decode(roomOwner, "UTF-8"));
                    map.put(Common.ROOM_MASTER_CAR_NUM, URLDecoder.decode(carNum, "UTF-8"));
                    if (!c.isNull(Common.ROOM_DESC)) {
                        String room_desc = c.getString(Common.ROOM_DESC);
                        map.put(Common.ROOM_DESC, URLDecoder.decode(room_desc, "UTF-8"));
                    } else {
                        map.put(Common.ROOM_DESC, URLDecoder.decode("", "UTF-8"));
                    }
                    map.put(Common.MEMBER_COUNT, member_count);
                    map.put(Common.REG_DATE, reg_date);
                    if (!c.isNull(Common.USE_FLAG)) {
                        map.put(Common.USE_FLAG, c.getString(Common.USE_FLAG));
                    } else {
                        map.put(Common.USE_FLAG, "0");
                    }
                    myList.add(map);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return myList;
        }

    }


    private void makeDialog() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_make_group2, null);
        groupInput = (EditText) view.findViewById(R.id.group_input);
        final EditText groupDesc = (EditText) view.findViewById(R.id.group_desc);
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setMessage("그룹명 입력");
        alert.setView(view);
        alert.setCancelable(false);
        alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                groupInput.setText("");
                dialog.dismiss();
            }
        });

        alert.setPositiveButton("만들기", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String desc = groupDesc.getText().toString().trim();
                String input = groupInput.getText().toString().trim();
                if (!input.isEmpty() && !desc.isEmpty()) {
                    dialog.dismiss();
                    String url = String.format(MAKE_GROUP_URL, pref.getString(CONST.ACCOUNT_ID, ""), input
                            , desc);
                    NetworkManager.getInstance(getActivity()).requestJsonObject(getActivity(), url,
                            com.android.volley.Request.Method.GET, mHandler,
                            HANDLE_SUCCESS_REG_TONG_INFO, HANDLE_FAIL_REG_TONG_INFO);
                } else {
                    Toast.makeText(getActivity().getApplicationContext(),
                            "그룹 이름, 설명은 필수 입니다.", Toast.LENGTH_LONG).show();
                }
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    private String MakeUri(String mode) {
        StringBuffer mkcheking = new StringBuffer();
        final String userID = pref.getString(CONST.ACCOUNT_ID, "");
        if (mode.equals("mylist")) {
            mkcheking.append(MY_GROUP_LIST);
            mkcheking.append(userID);
        } else {
            mkcheking.append(ALL_GROUP_LIST);
            mkcheking.append(userID);
        }
        return mkcheking.toString().trim();
    }
}
