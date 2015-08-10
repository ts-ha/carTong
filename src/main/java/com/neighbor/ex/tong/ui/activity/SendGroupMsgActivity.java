package com.neighbor.ex.tong.ui.activity;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.neighbor.ex.tong.R;
import com.neighbor.ex.tong.bean.EmoticonItem;
import com.neighbor.ex.tong.network.SendGroupPush;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class SendGroupMsgActivity extends AbsActivity {
    private LinearLayout parentLayout;
    private LinearLayout container;

    private View mPopUpView;
    private PopupWindow popupWindow;
    private boolean isKeyBoardVisible;
    private int keyboardHeight;
    private EditText mSendingMsg;
    private TextView carNo;
    private Button btnEmoticon;
    private float density;

    int previousHeightDiffrence = 0;
    private boolean isChange = false;
    private Button btnSend;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_groupmsg);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_normal);
        mToolbar.setTitle("주변 회원들에게 메세지 보내기");
        mToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.drawable.actionbar_previous_button_bg);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).
                getDefaultDisplay().getMetrics(metrics);
        density = metrics.density;
//        Log.d("SendMsgActivity", "  density " + density);

        parentLayout = (LinearLayout) findViewById(R.id.sendingParent);
        container = (LinearLayout) findViewById(R.id.emoticons);

        carNo = (TextView) findViewById(R.id.destCarNo);
        carNo.setText(getResources().getString(R.string.display_around_subscriber));

        mPopUpView = getLayoutInflater().inflate(R.layout.emoticons_sendgroup_popup, null);
        final float popUpheight = getResources().getDimension(
                R.dimen.keyboard_height);

        mSendingMsg = (EditText) findViewById(R.id.userMsg);
        mSendingMsg.requestFocus();

        btnEmoticon = (Button) findViewById(R.id.btn_emoticon);
        btnEmoticon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!popupWindow.isShowing()) {
                    popupWindow.setHeight(keyboardHeight);
                    if (isKeyBoardVisible) {
                        container.setVisibility(LinearLayout.GONE);
                    } else {
                        container.setVisibility(LinearLayout.VISIBLE);
                    }
                    popupWindow.showAtLocation(parentLayout, Gravity.BOTTOM, 0, 0);
                } else {
                    popupWindow.dismiss();
                }
            }
        });

         btnSend = (Button) findViewById(R.id.btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = mSendingMsg.getText().toString();
                if (content != null) {
                    btnSend.setEnabled(false);
                    SendGroupPush sendmsg = new SendGroupPush();
                    String tag = mSendingMsg.getTag() != null ? mSendingMsg.getTag().toString() : "";
                    if (content.equalsIgnoreCase(tag)) {
                        sendmsg.sendTong(SendGroupMsgActivity.this,
                                content);
                    } else if (mSendingMsg.getTag() != null) {
                        content += mSendingMsg.getTag().toString();
                        sendmsg.sendTong(SendGroupMsgActivity.this,
                                content);
                    } else {
                        sendmsg.sendTong(SendGroupMsgActivity.this,
                                content);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "메시지를 입력하세요.", Toast.LENGTH_LONG).show();
                }
                hideKeyboard();
            }
        });
        enablePopUpView();
        checkKeyboardHeight(parentLayout);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (popupWindow.isShowing()) {
            popupWindow.dismiss();
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

    void enablePopUpView() {

        LinearLayout highwayContainer = (LinearLayout) mPopUpView.
                findViewById(R.id.group_msg_grid);

        GridView highwayEmoticon = (GridView)
                highwayContainer.findViewById(R.id.container);

        ArrayList<EmoticonItem> highwaylist = new ArrayList<>();

        highwaylist.add(new EmoticonItem("chat_msg_moving.png", "차량을 안전한 곳으로 이동해 주세요"));
        highwaylist.add(new EmoticonItem("chat_msg_help.png", "도와주세요"));
        highwaylist.add(new EmoticonItem("chat_msg_safedriving.png", "안전운전하세요"));
        highwaylist.add(new EmoticonItem("chat_msg_luggage.png", "적재물이 낙하 되었습니다"));
        highwaylist.add(new EmoticonItem("chat_msg_landslide.png", "산사태 안전운전 하세요"));

        ImageAdapter highwayAdapter = new ImageAdapter(SendGroupMsgActivity.this, highwaylist);
        highwayEmoticon.setAdapter(highwayAdapter);
        highwayEmoticon.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final EmoticonItem item = (EmoticonItem) view.getTag();
                addImageBetweentext(item.getName(), item.getText());
            }
        });

        popupWindow = new PopupWindow(mPopUpView, ViewGroup.LayoutParams.MATCH_PARENT,
                keyboardHeight, false);
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
    public void onBackPressed() {
        finish();
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
