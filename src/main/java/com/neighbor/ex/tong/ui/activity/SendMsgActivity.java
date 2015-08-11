package com.neighbor.ex.tong.ui.activity;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.android.volley.Request;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.neighbor.ex.tong.CONST;
import com.neighbor.ex.tong.R;
import com.neighbor.ex.tong.bean.EmoticonItem;
import com.neighbor.ex.tong.common.Common;
import com.neighbor.ex.tong.common.SharedPreferenceManager;
import com.neighbor.ex.tong.network.NetworkManager;
import com.neighbor.ex.tong.network.SendPush;
import com.neighbor.ex.tong.ui.fragment.EmoticonSegment;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


public class SendMsgActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private LinearLayout parentLayout;
    private LinearLayout container;

    private View mPopUpView;
    private PopupWindow popupWindow;
    private boolean isKeyBoardVisible;
    private int keyboardHeight;
    private EditText mSendingMsg;
    private TextView carNo;
    private boolean isAttached = false;
    private Button btnEmoticon;
    private float density;

    int previousHeightDiffrence = 0;
    private boolean isChange = false;
    private GoogleApiClient mGoogleApiClient;

    private SharedPreferences prefs;
    private EmoticonSegment seg;
    private RadioButton btn1;
    private RadioButton btn2;
    private boolean iskeyBordShow;
    Handler mHandler;
    private Button btnSend;

    {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case Common.HANDLE_FAIL_REGIST_ACCIDENT:
                        btnSend.setEnabled(true);
                        break;

                    case Common.HANDLE_SUCCESS_REGIST_ACCIDENT:
                        finish();
                        break;
                    default:
                }

            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_msg);
        prefs =
                PreferenceManager.getDefaultSharedPreferences(SendMsgActivity.this);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_normal);
        mToolbar.setTitle("메세지 보내기");

        ActionBar actionBar = getSupportActionBar();
        setSupportActionBar(mToolbar);
//        actionBar.setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationIcon(R.drawable.actionbar_previous_button_bg);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        Bundle bundle = getIntent().getExtras();
        String destCarNo = "";
        if (null != bundle)
            destCarNo = bundle.getString("destNo", "운영자");

        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).
                getDefaultDisplay().getMetrics(metrics);

        density = metrics.density;

        parentLayout = (LinearLayout) findViewById(R.id.sendingParent);
        container = (LinearLayout) findViewById(R.id.emoticons);

        carNo = (TextView) findViewById(R.id.destCarNo);
        carNo.setText(destCarNo);

        mPopUpView = getLayoutInflater().inflate(R.layout.emoticons_popup, null);
        final float popUpheight = getResources().getDimension(
                R.dimen.keyboard_height);

        mSendingMsg = (EditText) findViewById(R.id.userMsg);
        mSendingMsg.requestFocus();

        btnEmoticon = (Button) findViewById(R.id.btn_emoticon);

        btnEmoticon.setOnClickListener(onClickListener);

        btnSend = (Button) findViewById(R.id.btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = mSendingMsg.getText().toString();
                btnSend.setEnabled(false);
                if (content != null) {
                    String tag = mSendingMsg.getTag() != null ? mSendingMsg.getTag().toString() : "";
                    if (content.equalsIgnoreCase(tag)) {
                    } else if (mSendingMsg.getTag() != null) {
                        content += mSendingMsg.getTag().toString();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "메시지를 입력하세요.", Toast.LENGTH_LONG).show();
                }

                if (carNo.getText().toString().equalsIgnoreCase("운영자")) {
                    String license = SharedPreferenceManager.getValue(SendMsgActivity.this, CONST.ACCOUNT_ID);
                    Location lastLocation = getLastLocation();
                    String url;
                    try {
                        content = URLEncoder.encode(content, "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    if (lastLocation != null) {
                        url = String.format(Common.REGIST_ACCIDENT, content, license,
                                lastLocation.getLongitude(), lastLocation.getLatitude());
                    } else {
                        url = String.format(Common.REGIST_ACCIDENT, content, license,
                                0, 0);
                    }
                    NetworkManager.getInstance(SendMsgActivity.this).requestJsonObject(SendMsgActivity.this, url,
                            Request.Method.GET, mHandler,
                            Common.HANDLE_SUCCESS_REGIST_ACCIDENT, Common.HANDLE_FAIL_REGIST_ACCIDENT);
                } else {
                    SendPush sendmsg = new SendPush();
                    sendmsg.sendTong(SendMsgActivity.this, carNo.getText().toString(),
                            content, getLastLocation());
                }

                hideKeyboard();
            }
        });

        enablePopUpView();
        checkKeyboardHeight(parentLayout);
        buildGoogleApiClient();
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (!popupWindow.isShowing()) {
                popupWindow.setHeight(keyboardHeight + 150);
                if (isKeyBoardVisible) {
                    if (iskeyBordShow) {
                        container.setVisibility(LinearLayout.GONE);
                    }
                } else {
                    container.setVisibility(LinearLayout.VISIBLE);
                }
                popupWindow.showAtLocation(parentLayout, Gravity.BOTTOM, 0, 0);
            } else {
                if (iskeyBordShow) {
                    popupWindow.dismiss();
                }
            }
        }
    };

    private void hideKeyboard() {

        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

    }

    public Location getLastLocation() {
        return LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private void checkKeyboardHeight(final View parentLayout) {

        parentLayout.getViewTreeObserver().addOnGlobalLayoutListener(

                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {
                        Rect r = new Rect();
                        parentLayout.getWindowVisibleDisplayFrame(r);
                        int screenHeight = parentLayout.getRootView()
                                .getHeight();
                        int heightDifference = screenHeight - (r.bottom);
                        if (previousHeightDiffrence - heightDifference > 50) {
                            popupWindow.dismiss();
                        }
                        previousHeightDiffrence = heightDifference;
                        if (heightDifference > 100) {
                            isKeyBoardVisible = true;
                            changeKeyboardHeight(heightDifference);
                        } else {
                            isKeyBoardVisible = false;
                        }

                        if (isChange != isKeyBoardVisible && isChange == false) {
                            isChange = isKeyBoardVisible;
                            btnEmoticon.performClick();
                        }
                    }
                });
    }

    void changeKeyboardHeight(int height) {
        if (height > 100) {
            keyboardHeight = height;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, keyboardHeight);
            container.setLayoutParams(params);
        }
    }


    @Override
    public void onBackPressed() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        } else {
            finish();
        }
    }

    void enablePopUpView() {
        seg = (EmoticonSegment) mPopUpView.findViewById(R.id.emoticon_tab);
        final ViewSwitcher emticonTab = (ViewSwitcher) mPopUpView.findViewById(R.id.emoticon_switcher);

        btn1 = (RadioButton) mPopUpView.findViewById(R.id.menu_highway);
        btn2 = (RadioButton) mPopUpView.findViewById(R.id.menu_rest);

        seg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.menu_highway) {
                    emticonTab.showPrevious();
                    iskeyBordShow = false;
                } else if (checkedId == R.id.menu_rest) {
                    emticonTab.showNext();
                    iskeyBordShow = true;
                }
            }
        });

        LinearLayout highwayContainer = (LinearLayout) mPopUpView.
                findViewById(R.id.highway_grid);
        GridView highwayEmoticon = (GridView)
                highwayContainer.findViewById(R.id.container);

        ArrayList<EmoticonItem> highwaylist = new ArrayList<>();

        highwaylist.add(new EmoticonItem("chat_msg_opendoor.png", getResources().getString(R.string.msg_opendoor)));
        highwaylist.add(new EmoticonItem("chat_msg_troublelamp.png", getResources().getString(R.string.msg_lamp)));
        highwaylist.add(new EmoticonItem("chat_msg_puncture.png", getResources().getString(R.string.msg_tir)));
        highwaylist.add(new EmoticonItem("chat_msg_opentrunk.png", getResources().getString(R.string.msg_trunk)));
        highwaylist.add(new EmoticonItem("chat_msg_smoke.png", getResources().getString(R.string.msg_smoke)));
        highwaylist.add(new EmoticonItem("chat_msg_roadline.png", getResources().getString(R.string.msg_roadline)));
        highwaylist.add(new EmoticonItem("chat_msg_luggage.png", getResources().getString(R.string.msg_drop)));
        highwaylist.add(new EmoticonItem("chat_msg_box.png", getResources().getString(R.string.msg_box)));


        ImageAdapter highwayAdapter = new ImageAdapter(SendMsgActivity.this, highwaylist);
        highwayEmoticon.setAdapter(highwayAdapter);
        highwayEmoticon.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final EmoticonItem item = (EmoticonItem) view.getTag();
                addImageBetweentext(item.getName(), item.getText());
            }
        });

        LinearLayout RestContainer = (LinearLayout) mPopUpView.
                findViewById(R.id.rest_grid);

        GridView restEmoticon = (GridView)
                RestContainer.findViewById(R.id.container);

        ArrayList<EmoticonItem> restlist = new ArrayList<>();
        restlist.add(new EmoticonItem("chat_msg_opendoor.png", getResources().getString(R.string.msg_opendoor)));
        restlist.add(new EmoticonItem("chat_msg_light.png", getResources().getString(R.string.msg_light)));
        restlist.add(new EmoticonItem("chat_msg_puncture.png", getResources().getString(R.string.msg_tir)));
        restlist.add(new EmoticonItem("chat_msg_opentrunk.png", getResources().getString(R.string.msg_trunk)));
        restlist.add(new EmoticonItem("chat_msg_troublelamp.png", getResources().getString(R.string.msg_smoke)));
        restlist.add(new EmoticonItem("chat_msg_babyincar.png", getResources().getString(R.string.msg_baby)));

        ImageAdapter restAdapter = new ImageAdapter(SendMsgActivity.this, restlist);
        restEmoticon.setAdapter(restAdapter);
        restEmoticon.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final EmoticonItem item = (EmoticonItem) view.getTag();
                addImageBetweentext(item.getName(), item.getText());
            }
        });

        popupWindow = new PopupWindow(mPopUpView, ViewGroup.LayoutParams.MATCH_PARENT,
                keyboardHeight, false);
//        Log.d("hts", "keyboardHeight : " + keyboardHeight);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                container.setVisibility(LinearLayout.GONE);
            }
        });
    }


    private void addImageBetweentext(String path, String content) {
        try {
            if (mSendingMsg.getText().length() > 0) mSendingMsg.setText("");
            Drawable drawable = Drawable.createFromStream(getAssets().open(path), null);
            Log.d("SendMsgActivity", "width " + drawable.getIntrinsicWidth() +
                    "height " + drawable.getIntrinsicHeight());
            if (density >= 2.4)
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth() * (int) density, drawable.getIntrinsicHeight() * (int) density);
            else
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

            int selectionCursor = mSendingMsg.getSelectionStart();
            mSendingMsg.getText().insert(selectionCursor, ".");
            selectionCursor = mSendingMsg.getSelectionStart();

            SpannableStringBuilder builder = new SpannableStringBuilder(mSendingMsg.getText());
            builder.setSpan(new ImageSpan(drawable), selectionCursor - ".".length(), selectionCursor, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            mSendingMsg.setText(builder);
            mSendingMsg.setSelection(selectionCursor);
            mSendingMsg.setTag(content);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return super.onCreateOptionsMenu(menu);
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

    @Override
    public void onConnected(Bundle bundle) {


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public class ImageAdapter extends BaseAdapter {

        private List<EmoticonItem> items = new ArrayList<EmoticonItem>();
        private LayoutInflater inflater;

        public ImageAdapter(Context context, List<EmoticonItem> items) {
            inflater = LayoutInflater.from(context);
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int i) {
            return items.get(i);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View v;
            ImageView picture;

            if (view == null) {
                v = inflater.inflate(R.layout.emoticons_item, null, false);
                picture = (ImageView) v.findViewById(R.id.emoticon);
                EmoticonItem item = items.get(i);
                v.setTag(item);
                picture.setImageBitmap(getBitmapImage(item.getName()));

            } else {
                v = view;
                System.out.println("Cia");
            }
            return v;
        }

        private Bitmap getBitmapImage(String name) {
            Bitmap bit = null;
            try {
                InputStream bitmap = getAssets().open(name);
                bit = BitmapFactory.decodeStream(bitmap);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return bit;
        }
    }
}
