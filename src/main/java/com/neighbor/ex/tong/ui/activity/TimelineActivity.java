package com.neighbor.ex.tong.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.neighbor.ex.tong.CONST;
import com.neighbor.ex.tong.R;
import com.neighbor.ex.tong.Utils;
import com.neighbor.ex.tong.network.ReplyTimeline;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;


public class TimelineActivity extends AppCompatActivity {

    private final static String MEMBER_ID = "REG_MEMBER_GMAIL";
    private final static String MEMBER_RLY = "CONTENT_RLY";
    private final static String REG_DATE = "REG_DATE";

    private final static int REQUEST_OK = 1;
    private final static int REQUEST_FAIL = -1;

    private ListView vComment;
    private List<COMMENT_HOLDER> mHolder;
    private REQUEST request;
    private ReplyAdapter reactAdapter;
    private EditText addComment;
    private Button sendComment;
    private String mRoomName;
    private String mContentId;
    private String mContent;
    private String mPosterId;
    private SharedPreferences pref;


    class ReplyObserver implements Observer {

        public void update(Observable observable, Object o) {

            if (observable instanceof ReplyTimeline) {
                request = new REQUEST();
                request.setContentId(mContentId);
                request.start();
            }
        }
    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (REQUEST_OK == msg.what) {
                reactAdapter.setNotifyOnChange(true);
                reactAdapter.notifyDataSetChanged();
            }
        }
    };

    class COMMENT_HOLDER {
        String member_Id;
        String member_react;
        String regDate;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline_detail);

        Intent intent = getIntent();
        if (intent != null) {
            mRoomName = intent.getStringExtra("roomName");
            mContentId = intent.getStringExtra("Id");
            mContent = intent.getStringExtra("Message");
            mPosterId = intent.getStringExtra("postId");
        }

        pref = PreferenceManager.getDefaultSharedPreferences(TimelineActivity.this);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_normal);
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

        mHolder = new ArrayList<>();

        TextView id = (TextView) findViewById(R.id.detail_timeline_id);
        id.setText(mPosterId);

        TextView content = (TextView) findViewById(R.id.detail_timeline_contents);
        content.setText(mContent);

        vComment = (ListView) findViewById(R.id.detail_timeline_comments);

        View view = findViewById(R.id.detail_footer_layout);

        addComment = (EditText) view.findViewById(R.id.chat_content);
        sendComment = (Button) view.findViewById(R.id.buttonSend);
        sendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String timeline = addComment.getText().toString();

                request = new REQUEST();
                request.setContentId(mContentId);
                request.start();

                ReplyTimeline line = new ReplyTimeline();
                ReplyObserver obs = new ReplyObserver();
                line.addObserver(obs);
                String _id = pref.getString(CONST.ACCOUNT_ID, "");
                line.postReply(mContentId, _id, addComment.getText().toString());
                addComment.setText("");
            }
        });


        reactAdapter = new ReplyAdapter(TimelineActivity.this, 0, mHolder);
        vComment.setAdapter(reactAdapter);

        request = new REQUEST();
        request.setContentId(mContentId);
        request.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class ReplyAdapter extends ArrayAdapter<COMMENT_HOLDER> {

        private ArrayList<COMMENT_HOLDER> comment = new ArrayList<COMMENT_HOLDER>();

        public ReplyAdapter(Context context, int resource, List<COMMENT_HOLDER> objects) {
            super(context, resource, objects);
            comment = (ArrayList<COMMENT_HOLDER>) objects;
        }

        @Override
        public int getCount() {
            return comment.size();
        }


        @Override
        public COMMENT_HOLDER getItem(int position) {
            return comment.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.timeline_detail_row, null);
            }

            COMMENT_HOLDER item = getItem(position);
            if (item != null) {
                TextView vId = (TextView) v.findViewById(R.id.publisher_id);
                TextView vComment = (TextView) v.findViewById(R.id.publisher_react);
                TextView vCommentDate = (TextView) v.findViewById(R.id.publisher_date);

                if (vId != null)
                    vId.setText(item.member_Id);
                if (vComment != null)
                    vComment.setText(item.member_react);

                vCommentDate.setText(item.regDate);
            }

            return v;
        }
    }

    interface SetParam {
        void setContentId(String id);
    }

    class REQUEST extends Thread implements SetParam {
        private final static String REQUEST_URL =
                "http://211.189.132.184:8080/Tong/api/selectTongContentRlyList.do?contentId=";

        private String contentId;
        private HttpResponse response;

        @Override
        public void run() {
            try {

                String str = "";

                StringBuffer mkcheking = new StringBuffer();
                mkcheking.append(REQUEST_URL);
                mkcheking.append(contentId);

                Log.d("hts", "selectTongContentRlyList url : " + mkcheking.toString());

                String rsJson = Utils.getJSON(mkcheking.toString(), 15000);
                Log.d("ContentDetailActivity", "rsJson" + rsJson);
                JSONArray jArray = new JSONArray(rsJson);
                Log.d("ContentDetailActivity", "array size" + jArray.length());
                mHolder.clear();

                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject c = jArray.getJSONObject(i);
                    String memberID = c.getString(MEMBER_ID);
                    String memberComment = c.getString(MEMBER_RLY);
                    String regDate = c.getString(REG_DATE);

                    int pos = memberID.indexOf("@gmail.com");
                    String id = memberID.substring(0, pos);

                    COMMENT_HOLDER info = new COMMENT_HOLDER();
                    info.member_Id = id;
                    info.member_react = memberComment;
                    info.regDate = regDate;

                    mHolder.add(info);
                }

                handler.sendEmptyMessage(REQUEST_OK);

            } catch (Exception e) {
                e.printStackTrace();
                handler.sendEmptyMessage(REQUEST_FAIL);
            }
        }

        @Override
        public void setContentId(String id) {
            contentId = id;
        }
    }
}
