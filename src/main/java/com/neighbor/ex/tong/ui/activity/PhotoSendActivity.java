package com.neighbor.ex.tong.ui.activity;


import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ViewSwitcher;

import com.neighbor.ex.tong.R;
import com.neighbor.ex.tong.Utils;
import com.neighbor.ex.tong.bean.EmoticonItem;
import com.neighbor.ex.tong.ui.fragment.EmoticonSegment;
import com.neighbor.ex.tong.network.UploadFileAndMessage;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import java.util.List;


public class PhotoSendActivity extends AppCompatActivity {

    private LinearLayout  parentLayout;
    private LinearLayout  container;

    private View          mPopUpView;
    private PopupWindow   popupWindow;
    private boolean       isKeyBoardVisible;
    private int           keyboardHeight;
    private EditText      mSendingMsg;
    private ImageView     carNo;
    private boolean       isAttached = false;
    private Button        btnEmoticon;
    private boolean       isChange = false;

    int previousHeightDiffrence = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_send);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_normal);
        mToolbar.setTitle("촬영한 번호판으로 메세지 보내기");
        mToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);

        Bundle bundle = getIntent().getExtras();
        final String filePath = bundle.getString("path", "");

        parentLayout = (LinearLayout) findViewById(R.id.sendingParent);
        container    = (LinearLayout) findViewById(R.id.emotions);

        carNo = (ImageView) findViewById(R.id.userPic);
        if (filePath != null) {
            int degree = Utils.GetExifOrientation(filePath);
            Bitmap photo = getThumbnail(PhotoSendActivity.this, filePath);
            carNo.setImageBitmap(Utils.GetRotatedBitmap(photo, degree));
        }

        mPopUpView = getLayoutInflater().inflate(R.layout.emoticons_popup, null);
        final float popUpheight = getResources().getDimension(
                R.dimen.keyboard_height);

        mSendingMsg = (EditText) findViewById(R.id.userMsg);
        mSendingMsg.requestFocus();

        btnEmoticon = (Button)findViewById(R.id.btn_emoticon);
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

        Button btnSend = (Button)findViewById(R.id.btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String content = mSendingMsg.getText().length() > 1 ?
                        mSendingMsg.getText().toString(): (String) mSendingMsg.getTag();
                if (content.contains(".")&& content.length() > 1) {
                    content = (String) mSendingMsg.getTag();
                }

                new UploadFileAndMessage(filePath,
                        content,
                        PhotoSendActivity.this).execute();
            }
        });

        enablePopUpView();
        checkKeyboardHeight(parentLayout);
    }

    private Bitmap getThumbnail(Context context, String path)
    {
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.MediaColumns._ID },
                MediaStore.MediaColumns.DATA + "=?",
                new String[] { path }, null);
        if (cursor != null && cursor.moveToFirst())
        {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            cursor.close();
            return MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(),
                    id, MediaStore.Images.Thumbnails.MINI_KIND, null);
        }
        cursor.close();
        return null;
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

                        if (isChange != isKeyBoardVisible && isChange == false ){
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
                    LayoutParams.MATCH_PARENT, keyboardHeight);
            container.setLayoutParams(params);
        }
    }

    void enablePopUpView() {
        EmoticonSegment seg      = (EmoticonSegment) mPopUpView.findViewById(R.id.emoticon_tab);
        final ViewSwitcher  emticonTab = (ViewSwitcher) mPopUpView.findViewById(R.id.emoticon_switcher);

        RadioButton btn1 = (RadioButton)mPopUpView.findViewById(R.id.menu_highway);
        RadioButton btn2 = (RadioButton)mPopUpView.findViewById(R.id.menu_rest);

        seg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.menu_highway) {
                    emticonTab.showPrevious();
                } else if (checkedId == R.id.menu_rest) {
                    emticonTab.showNext();
                }
            }
        });

        LinearLayout highwayContainer = (LinearLayout)mPopUpView.
                findViewById(R.id.highway_grid);
        GridView highwayEmoticon = (GridView)
                highwayContainer.findViewById(R.id.container);

        ArrayList<EmoticonItem> highwaylist = new ArrayList<>();

        highwaylist.add(new EmoticonItem("chat_msg_opendoor.png","문이 열려 있어요"));
        highwaylist.add(new EmoticonItem("chat_msg_luggage.png","적재물이 낙하 되었습니다."));
        highwaylist.add(new EmoticonItem("chat_msg_troublelamp.png","라이트가 고장났어요"));
        highwaylist.add(new EmoticonItem("chat_msg_puncture.png","타이어가 펑크 났습니다"));
        highwaylist.add(new EmoticonItem("chat_msg_accident.png","교통사고가 났어요"));
        highwaylist.add(new EmoticonItem("chat_msg_help.png", "도와주세요"));

        ImageAdapter highwayAdapter = new ImageAdapter(PhotoSendActivity.this,highwaylist);
        highwayEmoticon.setAdapter(highwayAdapter);
        highwayEmoticon.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final EmoticonItem item = (EmoticonItem)view.getTag();
                addImageBetweentext(item.getName(),item.getText());
            }
        });

        LinearLayout RestContainer = (LinearLayout)mPopUpView.
                findViewById(R.id.rest_grid);

        GridView restEmoticon = (GridView)
                RestContainer.findViewById(R.id.container);

        ArrayList<EmoticonItem> restlist = new ArrayList<>();
        restlist.add(new EmoticonItem("chat_msg_opendoor.png","문이 열려 있어요"));
        restlist.add(new EmoticonItem("chat_msg_accident.png","사고가 났어요"));
        restlist.add(new EmoticonItem("chat_msg_babyincar.png","어린이가 차 안에 있어요"));
        restlist.add(new EmoticonItem("chat_msg_help.png","도와주세요"));
        restlist.add(new EmoticonItem("chat_msg_parking.png","차량을 옮겨주세요"));

        ImageAdapter restAdapter = new ImageAdapter(PhotoSendActivity.this,restlist);
        restEmoticon.setAdapter(restAdapter);
        restEmoticon.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final EmoticonItem item = (EmoticonItem)view.getTag();
                addImageBetweentext(item.getName(),item.getText());
            }
        });

        popupWindow = new PopupWindow(mPopUpView, LayoutParams.MATCH_PARENT,
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
            if(mSendingMsg.getText().length() > 0) mSendingMsg.setText("");
            Drawable drawable = Drawable.createFromStream(getAssets().open(path), null);
            drawable .setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

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
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
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

        private Bitmap getBitmapImage(String name){
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
