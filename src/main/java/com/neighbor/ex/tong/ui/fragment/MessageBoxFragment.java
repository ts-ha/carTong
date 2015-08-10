package com.neighbor.ex.tong.ui.fragment;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.neighbor.ex.tong.CONST;
import com.neighbor.ex.tong.R;
import com.neighbor.ex.tong.Utils;
import com.neighbor.ex.tong.bean.TongMessage;
import com.neighbor.ex.tong.provider.DataProvider;
import com.neighbor.ex.tong.ui.dialog.CommonProgressDialog;

public class MessageBoxFragment extends Fragment {

    protected Context context;

    private ListView tongList;
    private TextView emptyMsg;
    private SharedPreferences pref;
    private MessageAdapter mAdapter;
//    private TextView recCnt;

    private final BroadcastReceiver MsgUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Cursor cs = context.getContentResolver().query(DataProvider.TONG_URI,
                    null, null, null, "TIME DESC");
            cs.moveToFirst();
            mAdapter = new MessageAdapter(context, cs, false);
            tongList.setAdapter(mAdapter);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.fragment_do_tong, container, false);
        tongList = (ListView) mainView.findViewById(android.R.id.list);
        emptyMsg = (TextView) mainView.findViewById(android.R.id.empty);
        Cursor cs = getActivity().getContentResolver().query(DataProvider.TONG_URI,
                null, null, null, "TIME DESC");
        cs.moveToFirst();
        if (cs.getCount() > 0)
            emptyMsg.setVisibility(View.GONE);
        else
            emptyMsg.setVisibility(View.VISIBLE);

        mAdapter = new MessageAdapter(getActivity(), cs, false);
        tongList.setAdapter(mAdapter);
        return mainView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(MsgUpdateReceiver,
                new IntentFilter(CONST.TONG_MESSAGE_UPDATE));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(MsgUpdateReceiver);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }


    class MessageAdapter extends CursorAdapter {

        private final LayoutInflater mLayoutInflater;
        private Context _cont;
        private Cursor _cs;


        public MessageAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
            _cs = c;
            _cont = context;

            mLayoutInflater = LayoutInflater.from(context);

        }

        @Override
        public int getCount() {
            return _cs.getCount();
        }


        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            try {

                TextView tt = (TextView) view.findViewById(R.id.sender_name);
                TextView bt = (TextView) view.findViewById(R.id.sender_message);
                ImageView mMsgImg = (ImageView) view.findViewById(R.id.imageViewMsgImg);
                ImageButton rec = (ImageButton) view.findViewById(R.id.btn_recommend);
//                recCnt = (TextView) view.findViewById(R.id.recommend_cnt);
                int cout = cursor.getInt(cursor.getColumnIndex("RECOMM_CNT"));
                Log.d("hts", "RECOMM_CNT : " + cout);


                TongMessage msg = new TongMessage();
                msg.INDEX = cursor.getInt(cursor.getColumnIndex("_id"));
                msg.CNT_RECOMM = cursor.getInt(cursor.getColumnIndex("RECOMM_CNT"));
                msg.CONTENT = cursor.getString(cursor.getColumnIndex("CONTENTS"));
                msg.IS_SENDIG = cursor.getString(cursor.getColumnIndex("DoSend"));
                msg.RECEIVER = cursor.getString(cursor.getColumnIndex("RECEIVER"));
                msg.SENDER = cursor.getString(cursor.getColumnIndex("SENDER"));
                msg.SEQ = cursor.getString(cursor.getColumnIndex("SEQ"));
                msg.TIME = cursor.getString(cursor.getColumnIndex("TIME"));

                if (cout == 0) {
                    rec.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            TongMessage item = (TongMessage) view.getTag();
                            CommonProgressDialog.showProgressDialog(_cont);
                            new UpdateRecommand(getActivity(), item).execute();
                        }
                    });
                    rec.setImageResource(R.drawable.recommend_selector);
                } else {
                    rec.setOnClickListener(null);
                    rec.setImageResource(R.drawable.btn_tong_recommend_finish);
                }

                int path = Utils.getEmoticonPathByMessage(msg.CONTENT, getActivity());

                if (msg.IS_SENDIG.equalsIgnoreCase("true")) {
                    tt.setText("  " + msg.RECEIVER);
                    rec.setVisibility(View.GONE);
                    tt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.btn_msg_out, 0, 0, 0);
                } else {
                    tt.setText("  " + msg.SENDER);
                    tt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.btn_msg_in, 0, 0, 0);
                    rec.setVisibility(View.VISIBLE);
                    rec.setTag(msg);
                    if (msg.SENDER.equalsIgnoreCase("운영자")) {
                        rec.setVisibility(View.GONE);
                    } else {
                        rec.setVisibility(View.VISIBLE);
                    }
                }
                if (path == 0) {
                    bt.setText(msg.CONTENT);
                    mMsgImg.setImageResource(R.drawable.icon_tong_user132);
                } else {
                    mMsgImg.setImageResource(path);
                    mMsgImg.setVisibility(View.VISIBLE);
                    bt.setText(msg.CONTENT.replace(".", ""));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//        @SuppressLint("NewApi")
//        void addImageBetweentext(String path, TextView view) {
//            try {
//                Drawable drawable = Drawable.createFromStream(_cont.getAssets().open(path), null);
//                Log.d("SendMsgActivity", "width " + drawable.getIntrinsicWidth() +
//                        "height " + drawable.getIntrinsicHeight());
//                view.setBackground(drawable);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            return mLayoutInflater.inflate(R.layout.messagebox_row, viewGroup, false);
        }
    }

    class UpdateRecommand extends AsyncTask<Void, Void, Void> {

        private final String RECOMMAND_URL =
                "http://211.189.132.184:8080/Tong/updateRecommendCount.do?seq=";

        private Context _context;
        private TongMessage item;
        private String userID;

        public UpdateRecommand(Context cont, TongMessage tongItem) {
            _context = cont;
            item = tongItem;
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(_context);
            userID = pref.getString(CONST.ACCOUNT_ID, "");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            item.CNT_RECOMM++;

            ContentValues cv = new ContentValues();
            cv.put("CONTENTS", item.CONTENT);
            cv.put("RECOMM_CNT", item.CNT_RECOMM);
            cv.put("DoSend", item.IS_SENDIG);
            cv.put("RECEIVER", item.RECEIVER);
            cv.put("SENDER", item.SENDER);
            cv.put("TIME", item.TIME);
            cv.put("SEQ", item.SEQ);
            _context.getContentResolver().update(DataProvider.TONG_URI, cv, "_id =?",
                    new String[]{String.valueOf(item.INDEX)});

            Cursor cs = _context.getContentResolver().query(DataProvider.TONG_URI,
                    null, null, null, "TIME DESC");
            cs.moveToFirst();
            mAdapter.changeCursor(cs);
            CommonProgressDialog.hideProgress();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                StringBuffer sb = new StringBuffer();
                sb.append(RECOMMAND_URL);
                sb.append(item.SEQ);
                sb.append("&memberGmail=");
                sb.append(userID);
                Log.d("hts", "updateRecommendCount url : " + sb.toString());

                String rsJson = Utils.getJSON(sb.toString(), 15000);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
