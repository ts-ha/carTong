package com.neighbor.ex.tong.ui.activity;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CursorAdapter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.neighbor.ex.tong.CONST;
import com.neighbor.ex.tong.R;
import com.neighbor.ex.tong.network.RequestAllCarNo;
import com.neighbor.ex.tong.network.SendDeviceID;
import com.neighbor.ex.tong.provider.DataProvider;
import com.neighbor.ex.tong.service.GPSTrack;

import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private final static int PICK_CAMERA_REQUEST = 1;
    private final int REQ_CODE_SPEECH_INPUT = 100;

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String SENDER_ID = "976777486925";

    final static int[] to = new int[]{android.R.id.text1};
    final static String[] from = new String[]{"PLATE_NUMBER"};


    private GoogleCloudMessaging _gcm;
    private String _regId;

    private PagerSlidingTabStrip tabs;
    private ViewPager pager;
    private MainTabAdapter adapter;
    private AutoCompleteTextView inputCarNo;
    private AutoCompleteDbAdapter autoCompleteAdapter;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);

        ImageButton carNo = (ImageButton) mToolbar.findViewById(R.id.search_carNo);
        carNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, PICK_CAMERA_REQUEST);
            }
        });

        ImageButton voiceRec = (ImageButton) mToolbar.findViewById(R.id.search_voice);
        voiceRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });

        inputCarNo = (AutoCompleteTextView) mToolbar.findViewById(R.id.search_text);

        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);

        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        pager = (ViewPager) findViewById(R.id.pager);
        adapter = new MainTabAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        tabs.setViewPager(pager);

        if (checkPlayServices()) {
            _gcm = GoogleCloudMessaging.getInstance(this);
            if (TextUtils.isEmpty(_regId))
                registerInBackground();
        }

        RequestAllCarNo carNumbers = new RequestAllCarNo();
        carNumbers.Action(MainActivity.this);

        autoCompleteAdapter = new AutoCompleteDbAdapter(MainActivity.this,
                null, false);
        inputCarNo.setAdapter(autoCompleteAdapter);
        inputCarNo.setThreshold(4);
        inputCarNo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(i);
                String number = cursor.getString(cursor.getColumnIndexOrThrow("PLATE_NUMBER"));

                Intent intent = new Intent(MainActivity.this, SendMsgActivity.class);
                intent.putExtra("destNo", number);
                startActivity(intent);
                inputCarNo.setText("");
            }
        });

        if (false == checkGPS()) chkGpsService();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, GPSTrack.class);
        startService(intent);
    }

    private void chkGpsService() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.set_gps_title));
        builder.setMessage(getResources().getString(R.string.set_gps_message));
        builder.setPositiveButton(getResources().getString(R.string.info_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                });
        Dialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    private boolean checkGPS() {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean isGPS = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isGPS;
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getResources().getString(R.string.voice_prompt));
        startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT:
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.
                            getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    String regex = "[0-9]{4}";
                    String resultData = result.get(0);

                    if (resultData.matches(regex))
                        inputCarNo.setText(resultData);
                }
                break;
            case PICK_CAMERA_REQUEST:
                if (resultCode == RESULT_OK && null != data) {
                    String mCapturePhotoPath = getRealPathFromURI(data.getData());
                    Intent intent = new Intent(MainActivity.this, PhotoSendActivity.class);
                    intent.putExtra("path", mCapturePhotoPath);
                    startActivity(intent);
                }
                break;
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        // can post image
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, // Which columns to return
                null,       // WHERE clause; which rows to return (all rows)
                null,       // WHERE clause selection arguments (none)
                null); // Order-by clause (ascending by name)
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @Override
    protected void onStop() {
        Intent intent = new Intent(this, GPSTrack.class);
        stopService(intent);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    public class MainTabAdapter extends FragmentPagerAdapter {

        private final String[] TITLES = getResources().getStringArray(R.array.menu);

        public MainTabAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
//                    TongFragment doTong = new TongFragment();
//                    return doTong;
//                case 1:
//                    MessageBoxFragment msgBox = new MessageBoxFragment();
//                    return msgBox;
//                case 2:
//                    GroupTongFragment group = new GroupTongFragment();
//                    return group;
//                case 3:
//                    SettingFragment fragmenttab3 = new SettingFragment();
//                    return fragmenttab3;
            }
            return null;
        }
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("checkPlayService", "|This device is not supported.|");
                finish();
            }
            return false;
        }
        return true;
    }

    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (_gcm == null) {
                        _gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    _regId = _gcm.register(SENDER_ID);
                    msg = _regId;
                } catch (IOException ex) {
                    msg = ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                int result = s.compareTo(prefs.getString(CONST.ACCOUNT_DEV, ""));
                if (result != 0) {
                    SharedPreferences.Editor edittor = prefs.edit();
                    edittor.putString(CONST.ACCOUNT_DEV, s);
                    edittor.commit();
                    SendDeviceID devUpdate = new SendDeviceID();
                    devUpdate.SendDevice(MainActivity.this, s);
                }
            }

        }.execute(null, null, null);
    }

    class AutoCompleteDbAdapter extends CursorAdapter implements Filterable {
        private LayoutInflater inflater;
        private ContentResolver mContent;

        public AutoCompleteDbAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mContent = context.getContentResolver();
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            return inflater.inflate(R.layout.autocomplete_item_row,
                    viewGroup, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            View v = view;
            TextView carNo = (TextView) v.findViewById(R.id.subscriber_carNo);
            if (cursor != null) {
                String plateNumber = cursor.getString(cursor.getColumnIndex("PLATE_NUMBER"));
                carNo.setText(plateNumber);
            }
        }

        @Override
        public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
            if (getFilterQueryProvider() != null) {
                return getFilterQueryProvider().runQuery(constraint);
            }
            String condition = "PLATE_NUMBER LIKE '%" + inputCarNo.getText().toString() + "'";
            return mContent.query(DataProvider.PLATE_URI, null,
                    condition, null, null);
        }

        @Override
        public CharSequence convertToString(Cursor cursor) {
            final int columnIndex = cursor.getColumnIndexOrThrow("PLATE_NUMBER");
            final String str = cursor.getString(columnIndex);
            return str;
        }
    }
}
