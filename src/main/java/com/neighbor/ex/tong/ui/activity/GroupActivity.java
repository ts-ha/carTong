package com.neighbor.ex.tong.ui.activity;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.neighbor.ex.tong.CONST;
import com.neighbor.ex.tong.R;
import com.neighbor.ex.tong.Utils;
import com.neighbor.ex.tong.bean.TimeLineInfo;
import com.neighbor.ex.tong.common.Common;
import com.neighbor.ex.tong.network.NetworkManager;
import com.neighbor.ex.tong.network.PostTimeline;
import com.neighbor.ex.tong.network.parser.RoomTongMemberInfo;
import com.neighbor.ex.tong.ui.adapter.TimelineAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;


public class GroupActivity extends AbsActivity {
    private final String TAG = "GroupActivity";
    private final static int REQUEST_START = 0;
    private final static int REQUEST_OK = 1;
    private final static int REQUEST_FAIL = -1;

    private static final String CONTENT_ID = "CONTENT_ID";
    private static final String CONTENT_COMMENT = "CONTENT_COMMENT";
    private static final String POSTED_ID = "REG_MEMBER_GMAIL";
    private static final String CONTENT_RLY = "RLY_COUNT";
    private static final String REG_DATE = "REG_DATE";

    private TimelineAdapter timelineAdapter;
    private List<TimeLineInfo> infolist;
    private String mRoomId;
    private String mRoomName;
    private String mRoomOwner;
    private EditText mTimeline;
    private Button mSendTimeline;
    private SharedPreferences pref;
    private ProgressDialog _dlg;
    private String mRoomDesc;
    private String mRoomDate;
    private TextView roomDesc;
    private Toolbar mToolbar;
    //    private Button mCheckJoinButton;
    private final int HANDLE_SUCCESS_REQUESTTONG = 20001;
    private final int HANDLE_FAIL_REQUESTTONG = HANDLE_SUCCESS_REQUESTTONG + 1;
    private final int HANDLE_SUCCESS_DELTONGINFO = HANDLE_FAIL_REQUESTTONG + 1;
    private final int HANDLE_FAIL_DELTONGINFO = HANDLE_SUCCESS_DELTONGINFO + 1;
    private final int HANDLE_SUCCESS_DROPROOMMEMBER = HANDLE_FAIL_DELTONGINFO + 1;
    private final int HANDLE_FAIL_DROPROOMMEMBER = HANDLE_SUCCESS_DROPROOMMEMBER + 1;
    private String mRoomMemberCount;
    private String mCarNum;


    class TimeLineObserver implements Observer {

        public void update(Observable observable, Object o) {

            if (observable instanceof PostTimeline) {
                GetContents getItems = new GetContents();
                getItems.setRoomId(mRoomId);
                getItems.start();
                handler.sendEmptyMessage(REQUEST_START);
            }
        }
    }

    private RoomTongMemberInfo mRoomTongMemberInfo;
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Common.HANDLE_SUCCESS_REGIST_ACCIDENT) {

            } else if (msg.what == Common.HANDLE_FAIL_REGIST_ACCIDENT) {
                Toast.makeText(GroupActivity.this, "수정 실패.",
                        Toast.LENGTH_LONG).show();
            } else if (msg.what == Common.HANDLE_SUCCESS_GET_ROOM_TONG_MEMBERINFO) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                if (msg.obj != null) {
                    mRoomTongMemberInfo = gson.fromJson((String) msg.obj, RoomTongMemberInfo.class);
//                    Log.d(TAG, "handleMessage mRoomTongMemberInfo : " + gson.toString());

                    if (mRoomOwner.equalsIgnoreCase(pref.getString(CONST.ACCOUNT_ID, "") + "@gmail.com")) {
                        if (mRoomTongMemberInfo != null && mRoomTongMemberInfo.getRowses().size() == 0) {
                            mToolbar.getMenu().clear();
                            mToolbar.inflateMenu(R.menu.group_frament_user);
                        }
                    }else{
                        mToolbar.getMenu().clear();
                        mToolbar.inflateMenu(R.menu.group_frament_out);
                    }
                }
            } else if (msg.what == Common.HANDLE_FAIL_GET_ROOM_TONG_MEMBERINFO) {
                Toast.makeText(GroupActivity.this, "다시 시도 해주세요.",
                        Toast.LENGTH_LONG).show();

            } else if (msg.what == HANDLE_SUCCESS_REQUESTTONG) {
                getTongMemberInfo();
            } else if (msg.what == HANDLE_FAIL_REQUESTTONG) {
                Toast.makeText(GroupActivity.this, "가입승인에 실패 하였습니다. 다시 확인해주세요.",
                        Toast.LENGTH_LONG).show();
                getTongMemberInfo();
            } else if (msg.what == HANDLE_SUCCESS_DELTONGINFO) {
                GroupActivity.this.finish();
            } else if (msg.what == HANDLE_FAIL_DELTONGINFO) {
                Toast.makeText(GroupActivity.this, "그룹 삭제 실패하였습니다. 다시 확인해주세요.",
                        Toast.LENGTH_LONG).show();
            } else if (msg.what == HANDLE_SUCCESS_DROPROOMMEMBER) {
                GroupActivity.this.finish();
            } else if (msg.what == HANDLE_FAIL_DROPROOMMEMBER) {
                Toast.makeText(GroupActivity.this, "그룹 탈퇴에 실패하였습니다. 다시 확인해주세요.",
                        Toast.LENGTH_LONG).show();
            }
            if (msg.what == REQUEST_OK) {
                _dlg.dismiss();
                timelineAdapter.notifyDataSetChanged();
            } else if (msg.what == REQUEST_START) {
                _dlg = new ProgressDialog(GroupActivity.this);
                _dlg.setMessage("loading..");
                _dlg.setCancelable(false);
                _dlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                _dlg.show();
            } else {
                _dlg.dismiss();
                showERR(getResources().getString(R.string.error_message));
            }
        }
    };

    private void showERR(String msg) {
        String strTitle = "!오류";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        Intent intent = getIntent();
        if (intent != null) {
            mRoomName = intent.getStringExtra("roomName");
            mRoomId = intent.getStringExtra("roomId");
            mRoomOwner = intent.getStringExtra("ROOM_MASTER_GMAIL");
            mRoomDesc = intent.getStringExtra(Common.ROOM_DESC);
            mRoomDate = intent.getStringExtra(Common.REG_DATE);
            mCarNum = intent.getStringExtra(Common.ROOM_MASTER_CAR_NUM);
            mRoomMemberCount = intent.getStringExtra(Common.MEMBER_COUNT);
        }

        pref = PreferenceManager.getDefaultSharedPreferences(GroupActivity.this);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_normal);
        mToolbar.setTitle(mRoomName);

        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationIcon(R.drawable.actionbar_previous_button_bg);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        RecyclerView listTimeline = (RecyclerView) findViewById(R.id.list_timeline);
        listTimeline.setHasFixedSize(true);

        roomDesc = (TextView) findViewById(R.id.textViewDesc);
        ((TextView) findViewById(R.id.textViewRoomOwner)).setText(mCarNum);
        TextView roomDate = (TextView) findViewById(R.id.textViewDate);
        TextView textViewMemberCount = (TextView) findViewById(R.id.textViewMemberCount);


        roomDesc.setText(mRoomDesc);
        roomDate.setText(mRoomDate);
        textViewMemberCount.setText("가입된 회원 " + mRoomMemberCount + "명");

        View view = findViewById(R.id.footer_layout);

        mTimeline = (EditText) view.findViewById(R.id.chat_content);
        mSendTimeline = (Button) view.findViewById(R.id.buttonSend);
        mSendTimeline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String timeline = mTimeline.getText().toString();
                String _id = pref.getString(CONST.ACCOUNT_ID, "");
                PostTimeline line = new PostTimeline();
                TimeLineObserver obs = new TimeLineObserver();
                line.addObserver(obs);
                line.postTimeline(mRoomId, _id, timeline);
                mTimeline.setText("");
            }
        });

        RecyclerView.LayoutManager timelineManager = new LinearLayoutManager(this);
        listTimeline.setLayoutManager(timelineManager);
        infolist = new ArrayList<>();
        timelineAdapter = new TimelineAdapter(infolist);
        listTimeline.setAdapter(timelineAdapter);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                showDialogRoomChang();
                return true;
            case R.id.action_delet:
                shwoDilaogRoomDel();
                return true;
            case R.id.action_member:
                showDialogJoinUs();
                return true;
            case R.id.action_group_out:
                showDialogSecession();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.group_frament, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        getTongMemberInfo();
        GetContents getItems = new GetContents();
        getItems.setRoomId(mRoomId);
        getItems.start();
        handler.sendEmptyMessage(REQUEST_START);
    }

    private void getTongMemberInfo() {
        String url = "http://211.189.132.184:8080/Tong/api/getTongMemberInfo.do?roomId=" + mRoomId;
        Log.d(TAG, "getTongMemberInfo url : " + url);
        NetworkManager.getInstance(GroupActivity.this).requestJsonObject(GroupActivity.this, url,
                Request.Method.GET, handler,
                Common.HANDLE_SUCCESS_GET_ROOM_TONG_MEMBERINFO, Common.HANDLE_FAIL_GET_ROOM_TONG_MEMBERINFO);
    }

//    View.OnClickListener onClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            int vId = v.getId();
//            switch (vId) {
//                case R.id.buttonCheckJoin:
//                    showDialogJoinUs();
//                    break;
//                case R.id.buttonChangRoom:
//                    showDialogRoomChang();
//                    break;
//                case R.id.buttonSecession:
//                    showDialogSecession();
//            }
//        }
//    };

    private void showDialogSecession() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("그룹에서 탈퇴하시겠습니까?");
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton("네", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String url = "http://211.189.132.184:8080/Tong/api/dropRoomMember.do?memberGmail=%s&roomId=%s";
                String userID = pref.getString(CONST.ACCOUNT_ID, "");
                url = String.format(url, userID, mRoomId);
                NetworkManager.getInstance(GroupActivity.this).requestJsonObject(GroupActivity.this, url,
                        Request.Method.GET, handler,
                        HANDLE_SUCCESS_DROPROOMMEMBER, HANDLE_FAIL_DROPROOMMEMBER);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDialogJoinUs() {
        ArrayList<String> memberGmails = new ArrayList<String>();
        for (RoomTongMemberInfo.Rows row : mRoomTongMemberInfo.getRowses()) {
            memberGmails.add(row.getMEMBER_GMAIL());
        }
        final CharSequence[] charSeqOfNames = memberGmails.toArray(new CharSequence[memberGmails.size()]);
        final ArrayList<Integer> seletedItems = new ArrayList();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("가입 승인 명단을 선택하세요.");
        builder.setMultiChoiceItems(charSeqOfNames, null,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int indexSelected,
                                        boolean isChecked) {
                        if (isChecked) {
                            seletedItems.add(indexSelected);
                        } else if (seletedItems.contains(indexSelected)) {
                            seletedItems.remove(Integer.valueOf(indexSelected));
                        }
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        for (int idx : seletedItems) {
                            String url = "http://211.189.132.184:8080/Tong/api/requestTong.do?roomId=%s&joinMemberGmail=%s";
                            url = String.format(url, mRoomId, charSeqOfNames[idx]);
                            NetworkManager.getInstance(GroupActivity.this).requestJsonObject(GroupActivity.this, url,
                                    Request.Method.GET, handler,
                                    HANDLE_SUCCESS_REQUESTTONG, HANDLE_FAIL_REQUESTTONG);
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void shwoDilaogRoomDel() {
        AlertDialog.Builder alert = new AlertDialog.Builder(GroupActivity.this);
        alert.setCancelable(false);
        alert.setMessage("그룹을 삭제하기시겠습니까?");
        alert.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                String url = "http://211.189.132.184:8080/Tong/api/delTongInfo.do?roomId=" + mRoomId;
//                Log.d("hts", "delTongInfo url : " + url);
                NetworkManager.getInstance(GroupActivity.this).requestJsonObject(GroupActivity.this, url,
                        Request.Method.GET, handler,
                        HANDLE_SUCCESS_DELTONGINFO, HANDLE_FAIL_DELTONGINFO);
            }
        });
        alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    private void showDialogRoomChang() {
        View view = LayoutInflater.from(GroupActivity.this).inflate(R.layout.dialog_make_group, null);
        final EditText groupDesc = (EditText) view.findViewById(R.id.group_input);

        groupDesc.setHint("그룹 설명을 입력하세요.");
        AlertDialog.Builder alert = new AlertDialog.Builder(GroupActivity.this);
        alert.setMessage("그룹 설명 수정");
        alert.setView(view);
        alert.setCancelable(false);
        alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
//        alert.setNeutralButton("삭제", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//                String url = "http://211.189.132.184:8080/Tong/api/delTongInfo.do?roomId=" + mRoomId;
////                GroupActivity.this.finish();
//                Log.d("hts", "delTongInfo url : " + url);
//                NetworkManager.getInstance(GroupActivity.this).requestJsonObject(GroupActivity.this, url,
//                        Request.Method.GET, handler,
//                        HANDLE_SUCCESS_DELTONGINFO, HANDLE_FAIL_DELTONGINFO);
//            }
//        });

        alert.setPositiveButton("수정", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (groupDesc.getText().toString().isEmpty()) {
                    Toast.makeText(GroupActivity.this, "그룹 설명을 입력하세요.", Toast.LENGTH_LONG).show();
                } else {
                    String desc = groupDesc.getText().toString();
                    roomDesc.setText(desc);
                    try {
                        desc = URLEncoder.encode(desc, "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    sendChangRoomInfo(desc);
                    dialog.dismiss();
                }
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    private void sendChangRoomInfo(String desc) {
        String url = String.format(Common.URL_UPDATE_TONG_INFO, mRoomId, desc);
        NetworkManager.getInstance(GroupActivity.this).requestJsonObject(GroupActivity.this, url,
                Request.Method.GET, handler,
                Common.HANDLE_SUCCESS_REGIST_ACCIDENT, Common.HANDLE_FAIL_REGIST_ACCIDENT);
    }


    interface SetParam {
        void setRoomId(String id);
    }

    class GetContents extends Thread implements SetParam {

        private final static String REQUEST_URL =
                "http://211.189.132.184:8080/Tong/selectTongContentList.do?roomId=";
        private String roomId;

        @Override
        public void run() {

            String str = "";
            StringBuffer mkcheking = new StringBuffer();
            mkcheking.append(REQUEST_URL);
            mkcheking.append(roomId);

            String rsJson = Utils.getJSON(mkcheking.toString(), 15000);

            try {
                JSONArray jArray = new JSONArray(rsJson);
                infolist.clear();
                for (int i = 0; i < jArray.length(); i++) {

                    JSONObject c = jArray.getJSONObject(i);
                    String contentID = c.getString(CONTENT_ID);
                    String contentComment = c.getString(CONTENT_COMMENT);
                    String postingID = c.getString(POSTED_ID);

                    String regDate = c.getString(REG_DATE);

                    String rlyCount = "0";
                    rlyCount = c.getInt(CONTENT_RLY) + "";

                    int pos = postingID.indexOf("@gmail.com");
                    String id = postingID.substring(0, pos);

                    TimeLineInfo info = new TimeLineInfo();
                    info.content_id = contentID;
                    info.content_comment = URLDecoder.decode(contentComment, "utf-8");
                    info.content_postId = id;
                    info.content_roomName = mRoomName;
                    info.content_rlyCount = rlyCount;
                    info.regDate = regDate;
                    infolist.add(info);
                }

                Collections.reverse(infolist);
                handler.sendEmptyMessage(REQUEST_OK);

            } catch (Exception e) {
                e.printStackTrace();
                handler.sendEmptyMessage(REQUEST_FAIL);
            }
        }

        @Override
        public void setRoomId(String id) {
            roomId = id;
        }
    }
}
