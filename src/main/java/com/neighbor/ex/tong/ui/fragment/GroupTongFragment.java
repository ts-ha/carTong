package com.neighbor.ex.tong.ui.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.neighbor.ex.tong.CONST;
import com.neighbor.ex.tong.R;
import com.neighbor.ex.tong.Utils;
import com.neighbor.ex.tong.common.Common;
import com.neighbor.ex.tong.network.JoinGroup;
import com.neighbor.ex.tong.network.RequestMakeGroup;
import com.neighbor.ex.tong.ui.activity.GroupActivity;
import com.neighbor.ex.tong.ui.adapter.GroupAdapter;
import com.neighbor.ex.tong.ui.adapter.MyGroupAdapter;
import com.neighbor.ex.tong.ui.dialog.CommonProgressDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;


public class GroupTongFragment extends Fragment {

    private final static String MY_GROUP_LIST = "http://211.189.132.184:8080/Tong/api/selectRoomMyList.do?memberGmail=";

    private final static String ALL_GROUP_LIST = "http://211.189.132.184:8080/Tong/api/selectRoomAllList.do?memberGmail=";
    private final static String GROUP_JOIN_URL = "http://211.189.132.184:8080/Tong/regTongMemberInfo.do?roomId=";

    ViewSwitcher groupSwitcher;
    TongSegment groupBtn;
    RadioButton myGroupButton;
    RadioButton allGroupButton;
    ListView Mylist;
    ListView Grouplist;

    TextView emptyText;
    EditText groupInput;
    ImageButton btnMakeGroup;

    String mMode;
    String userID;

    ArrayList<HashMap<String, String>> myList;
    ArrayList<HashMap<String, String>> totalList;
    RequestMakeGroup mNewGroup;
    SharedPreferences pref;
    MyGroupAdapter myAdapter;
    GroupAdapter groupAdapter;
    private RadioButton mSearchGroupButton;

    class NewGroupObserver implements Observer {
        public void update(Observable observable, Object o) {
            if (observable instanceof RequestMakeGroup) {
                try {
                    CommonProgressDialog.hideProgress();
                    String uri = MakeUri(mMode);
                    Request request = new Request(getActivity(), uri);
                    request.execute();
                } catch (Exception e) {
                }
            }
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
                    CommonProgressDialog.showProgressDialog(getActivity());
                    mNewGroup = new RequestMakeGroup(getActivity(), input,
                            desc);
                    NewGroupObserver obs = new NewGroupObserver();
                    mNewGroup.addObserver(obs);
                    mNewGroup.action();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(),
                            "그룹 이름, 설명은 필수 입니다.", Toast.LENGTH_LONG).show();
                }
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }

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
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();
        String uri = MakeUri(mMode);
        Request request = new Request(getActivity().getApplication(), uri);
        request.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_group_tong, container, false);
        groupSwitcher = (ViewSwitcher) v.findViewById(R.id.group_switcher);
        groupBtn = (TongSegment) v.findViewById(R.id.segment_group);

        myGroupButton = (RadioButton) v.findViewById(R.id.button_mygroup);
        allGroupButton = (RadioButton) v.findViewById(R.id.button_group);
//        mSearchGroupButton = (RadioButton) v.findViewById(R.id.button_group_search);

        groupBtn.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Request request;
                if (checkedId == R.id.button_mygroup) {
                    groupSwitcher.showPrevious();
                    myGroupButton.setSelected(true);
                    mMode = "mylist";
                    String uri = MakeUri(mMode);
                    request = new Request(getActivity(), uri);
                    request.execute();
                } else if (checkedId == R.id.button_group) {
                    groupSwitcher.showNext();
                    allGroupButton.setSelected(true);
                    mMode = "groups";
                    String uri = MakeUri(mMode);
                    request = new Request(getActivity(), uri);
                    request.execute();
                }
            }
        });

        Mylist = (ListView) v.findViewById(android.R.id.list);
        Mylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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


        emptyText = (TextView) v.findViewById(android.R.id.empty);
        Grouplist = (ListView) v.findViewById(R.id.total_list);
        Grouplist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CommonProgressDialog.showProgressDialog(getActivity());
                userID = pref.getString(CONST.ACCOUNT_ID, "");
                HashMap<String, String> item = totalList.get(position);
                StringBuffer mkcheking = new StringBuffer();
                mkcheking.append(GROUP_JOIN_URL);
                mkcheking.append(item.get(Common.ROOM_ID));
                mkcheking.append("&joinMemberGmail=");
                mkcheking.append(userID);
                JoinGroup join = new JoinGroup();
                join.action(getActivity().getApplicationContext(), mkcheking.toString());
            }
        });
        myGroupButton.setSelected(true);

        btnMakeGroup = (ImageButton) v.findViewById(R.id.make_group);
        btnMakeGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeDialog();
            }
        });
        return v;
    }


    String MakeUri(String mode) {
        StringBuffer mkcheking = new StringBuffer();
        final String userID = pref.getString(CONST.ACCOUNT_ID, "");
        if (mode.equals("mylist")) {
            mkcheking.append(MY_GROUP_LIST);
            mkcheking.append(userID);
        } else {
            mkcheking.append(ALL_GROUP_LIST);
            mkcheking.append(userID);
        }
        Log.d("hts", "MY_GROUP_LIST url : " + mkcheking.toString());
        return mkcheking.toString().trim();
    }


    class Request extends AsyncTask<Void, Void, Void> {

        private String uri;
        private Context _cont;
        private ProgressDialog _dlg;

        public Request(Context cont, String reqURL) {
            uri = reqURL;
            _cont = cont;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            CommonProgressDialog.showProgressDialog(getActivity());
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            CommonProgressDialog.hideProgress();
            try {
                if (mMode.equalsIgnoreCase("mylist")) {
                    Mylist.setAdapter(myAdapter);
                    emptyText.setVisibility(View.GONE);
                    myAdapter.notifyDataSetChanged();
                } else {
                    Grouplist.setAdapter(groupAdapter);
                    groupAdapter.notifyDataSetChanged();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                String rsJson = Utils.getJSON(uri, 15000);
                JSONArray jArray = new JSONArray(rsJson);
                myList.clear();
                totalList.clear();
//                if (mMode.equalsIgnoreCase("mylist")) {
//                } else {
//                }

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
                    if (mMode.equalsIgnoreCase("mylist"))
                        myList.add(map);
                    else
                        totalList.add(map);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
