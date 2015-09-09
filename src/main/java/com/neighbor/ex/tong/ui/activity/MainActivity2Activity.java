package com.neighbor.ex.tong.ui.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationServices;
import com.neighbor.ex.tong.CONST;
import com.neighbor.ex.tong.LppService;
import com.neighbor.ex.tong.R;
import com.neighbor.ex.tong.common.AppUtil;
import com.neighbor.ex.tong.common.Common;
import com.neighbor.ex.tong.common.SharedPreferenceManager;
import com.neighbor.ex.tong.network.RequestAllCarNo;
import com.neighbor.ex.tong.network.SendDeviceID;
import com.neighbor.ex.tong.provider.DataProvider;
import com.neighbor.ex.tong.service.GPSTrack;
import com.neighbor.ex.tong.ui.fragment.GroupTongFragment_test;
import com.neighbor.ex.tong.ui.fragment.MapFragment;
import com.neighbor.ex.tong.ui.fragment.MessageBoxFragment;
import com.neighbor.ex.tong.ui.fragment.SettingFragment;
import com.neighbor.ex.tong.ui.fragment.TongFragment;

import java.io.IOException;
import java.util.ArrayList;


public class MainActivity2Activity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final long BACK_KEY_PRESS_INVERVAL = 2000l;
    private long lastBackKeyPressTime = 0l;
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
    private AutoCompleteTextView inputCarNo;

    private SharedPreferences prefs;

    private DrawerLayout mDrawerLayout;
    private Button mDrawerBtn;
    private ListView mDrawerList;
    private TextView titleTextView;
    //    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mPlanetTitles;
    private int mStempCount;

    private Drawable[] drawableArray;

    private int msg_heart_rate;
    private int msg_step_cunt;
    private int seleItemPosition;
    private View drawerView;
    private Fragment fragment;
    private AutoCompleteDbAdapter autoCompleteAdapter;
    //    private SleepLogFragment sleepLViewogFragment;
    private static int fragmentIdx = 0;
    private static int IMAGE_SUCCESS = 9001;
    private static int IMAGE_FAIL = 9002;
    private LocationManager lm;
    private GoogleApiClient mGoogleApiClient;
    private TextView mUserName, mUserNickName;



    private boolean isPause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity2);
        mPlanetTitles = getResources().getStringArray(R.array.menu_array);
        drawerView = findViewById(R.id.drawer);
        mTitle = mDrawerTitle = getTitle();
        titleTextView = (TextView) findViewById(R.id.textViewTile);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        mUserName = (TextView) findViewById(R.id.textViewUserName);
        mUserNickName = (TextView) findViewById(R.id.textViewNickName);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerBtn = (Button) findViewById(R.id.left);
        findViewById(R.id.menu0).setOnClickListener(onClickListener);
        findViewById(R.id.menu1).setOnClickListener(onClickListener);
        findViewById(R.id.menu2).setOnClickListener(onClickListener);
        findViewById(R.id.menu3).setOnClickListener(onClickListener);
        findViewById(R.id.menu4).setOnClickListener(onClickListener);

        autoCompleteAdapter = new AutoCompleteDbAdapter(MainActivity2Activity.this,
                null, false);
        inputCarNo = (AutoCompleteTextView) findViewById(R.id.title);
        inputCarNo.setAdapter(autoCompleteAdapter);
        inputCarNo.setThreshold(4);
        inputCarNo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(i);
                String number = cursor.getString(cursor.getColumnIndexOrThrow("PLATE_NUMBER"));
                Intent intent = new Intent(MainActivity2Activity.this, SendMsgActivity.class);
                intent.putExtra("destNo", number);
                inputCarNo.setText("");
                startActivity(intent);
            }
        });

        inputCarNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (inputCarNo.getText().toString().trim().length() == 4) {
                    String tem = inputCarNo.getText().toString().substring(inputCarNo.getText().length() - 2, inputCarNo.getText().length());
                    String condition = "PLATE_NUMBER LIKE '%" + tem + "'";
                    Cursor query = getContentResolver().query(DataProvider.PLATE_URI, null,
                            condition, null, null);
                    if (query.getCount() == 0 && !isPause) {
                        Toast.makeText(MainActivity2Activity.this, "검색된 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });


        inputCarNo.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_SEARCH:
                        Intent intent = new Intent(MainActivity2Activity.this, CarSearchActivity.class);
                        intent.putExtra("inputCarNo", inputCarNo.getText().toString());
                        hideKeyboard();
                        isPause = true;
                        startActivity(intent);
                        break;
                    default:
                        return false;
                }
                return false;
            }
        });


        RequestAllCarNo carNumbers = new RequestAllCarNo();
        carNumbers.Action(MainActivity2Activity.this);

        ImageButton carNo = (ImageButton) findViewById(R.id.search_carNo);
        carNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                cameraIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 1);
//                startActivityForResult(cameraIntent, PICK_CAMERA_REQUEST);

//                inputCarNo.setText("");
                Intent cameraIntent = new Intent(MainActivity2Activity.this, OilCamera.class);
                startActivityForResult(cameraIntent, PICK_CAMERA_REQUEST);
            }
        });

        ImageButton voiceRec = (ImageButton) findViewById(R.id.search_voice);
        voiceRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });

        if (checkPlayServices()) {
//            Log.d("hts", "checkPlayServices ");
            _gcm = GoogleCloudMessaging.getInstance(this);
//            Log.d("hts", "_gcm : " + _gcm);
            if (TextUtils.isEmpty(_regId)) {
                registerInBackground();
            }
        }

        mDrawerBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mDrawerLayout.isDrawerOpen(drawerView)) {
                    mDrawerLayout.closeDrawer(drawerView);
                    hideKeyboard();
                } else {
                    hideKeyboard();
                    mUserName.setText(prefs.getString(CONST.ACCOUNT_LICENSE, ""));
                    mUserNickName.setText(prefs.getString(CONST.ACCOUNT_NAME, ""));
                    mDrawerLayout.openDrawer(drawerView);
                }
            }
        });
        if (false == checkGPS()) chkGpsService();

        Bundle bundle = getIntent().getExtras();


        if (!SharedPreferenceManager.getValue(this, SharedPreferenceManager.positionAgree)
                .equalsIgnoreCase("true")) {
            showDialogAree();
        }
        if (null != bundle) {
            String pushNoit = bundle.getString(Common.PUSH_NOIT, Common.PUSH_NOIT);
            if (pushNoit != null && !pushNoit.isEmpty()) {
                selectItem(3);
                setTitle(getResources().getStringArray(R.array.menu_array)[3]);
            }
        } else {
            selectItem(0);
            setTitle(getResources().getStringArray(R.array.menu_array)[0]);
        }
    }


    private void showDialogAree() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialog.setContentView(R.layout.dialog_agree); // custom_dialog.xml 로 저장된 layout 설정 파일을 view 설정한다.
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = LinearLayout.LayoutParams.MATCH_PARENT;
        final CheckBox accountLicense = (CheckBox) dialog.findViewById(R.id.checkBoxAgree);
        final Button agreeBtn = (Button) dialog.findViewById(R.id.buttonAgree);
        final Button agreeCancelBtn = (Button) dialog.findViewById(R.id.buttonAgreeCancel);
        agreeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (accountLicense.isChecked()) {
                    SharedPreferenceManager.setValue(MainActivity2Activity.this, SharedPreferenceManager.positionAgree, "true");
                    dialog.dismiss();
                }
            }
        });
        agreeCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPause = false;
//        inputCarNo.setText("");
        buildGoogleApiClient();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPause = true;
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getResources().getString(R.string.voice_prompt));
        startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
    }

    public void hideKeyboard() {

        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

    }

    public Location getLastLocation() {
        return LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(MainActivity2Activity.this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
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
                    if (resultData.matches(regex)) {
                        inputCarNo.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                inputCarNo.showDropDown();
                            }
                        }, 500);
//                        Toast.makeText(MainActivity2Activity.this, "resultData : " + resultData, Toast.LENGTH_SHORT).show();
                        inputCarNo.setText(resultData);
                        inputCarNo.setSelection(inputCarNo.getText().length());
                    }
                }
                break;
            case PICK_CAMERA_REQUEST:
                if (resultCode == RESULT_OK && null != data) {
                    String carNum = data.getStringExtra("carNum");
                    if (carNum.equalsIgnoreCase("미인식")) {
                        Toast.makeText(MainActivity2Activity.this, "미인식 차량입니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                        inputCarNo.setText("");
                    } else {
                        String resultCarNo = data.getStringExtra("resultCarNo");
                        Log.d("hts", "resultCarNo : " + resultCarNo);
                        resultCarNo = resultCarNo.substring(resultCarNo.length() - 4, resultCarNo.length());
                        inputCarNo.setText(resultCarNo);
                    }
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

    private boolean checkGPS() {
        lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean isGPS = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isGPS;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, GPSTrack.class);
        startService(intent);
        startService();
        bindService();
    }

    public void selectItem(int possition) {

        FragmentManager fragmentManager = getFragmentManager();
        Bundle args = new Bundle();
        switch (possition) {

            case Common.MENU_HOME:
                fragment = new TongFragment();
                fragment.setArguments(args);
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment)
                        .commit();
                break;
            case Common.MENU_LIFE_LOG:
//                fragment = new GroupTongFragment();
                fragment = new GroupTongFragment_test();
                fragment.setArguments(args);
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment)
                        .commit();
                break;
            case Common.MENU_ACTIVITY:
                fragment = new SettingFragment();
                fragment.setArguments(args);
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment)
                        .commit();
                break;
            case Common.MENU_NOTICE:
                fragment = new MapFragment();
                fragment.setArguments(args);
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment)
                        .commit();
                break;
            case Common.MENU_SLEEP:
                fragment = new MessageBoxFragment();
                fragment.setArguments(args);
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment)
                        .commit();
                break;

            default:
                break;
        }
        fragmentIdx = possition;
        setTitle(getResources().getStringArray(R.array.menu_array)[possition]);
        mDrawerLayout.closeDrawer(drawerView);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        titleTextView.setText(title);
//		getActionBar().setTitle(mTitle);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.menu1: {
                    selectItem(1);
                    break;
                }
                case R.id.menu2: {
                    selectItem(2);
                    break;
                }
                case R.id.menu3: {
                    selectItem(3);
                    break;
                }
                case R.id.menu0: {
                    selectItem(0);
                    break;
                }
                case R.id.menu4: {
                    selectItem(4);
                    break;
                }
            }
        }
    };


    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();
        if (mDrawerLayout.isDrawerOpen(drawerView)) {
            mDrawerLayout.closeDrawer(drawerView);
        }
        if (fragmentIdx == 0) {
            if (lastBackKeyPressTime == 0 || (lastBackKeyPressTime + BACK_KEY_PRESS_INVERVAL) < currentTime) {
                Toast.makeText(this, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
                lastBackKeyPressTime = currentTime;

            } else {
                super.onBackPressed();
            }
        } else {
            selectItem(0);
        }
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

    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (_gcm == null) {
                        _gcm = GoogleCloudMessaging.getInstance(MainActivity2Activity.this);
                    }
                    _regId = _gcm.register(SENDER_ID);
                    msg = _regId;
//                    Log.d("hts", "registerInBackground : " + _regId);
                } catch (IOException ex) {
                    msg = ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

//                int result = s.compareTo(prefs.getString(CONST.ACCOUNT_DEV, ""));
//                if (result != 0) {
//                    SharedPreferences.Editor edittor = prefs.edit();
//                    edittor.putString(CONST.ACCOUNT_DEV, s);
//                    edittor.commit();
//                    SendDeviceID devUpdate = new SendDeviceID();
//                    devUpdate.SendDevice(MainActivity2Activity.this, s);
//                }
            }

        }.execute(null, null, null);
    }

    private void startService() {
        String canonicalName = LppService.class.getCanonicalName();
        boolean isService = AppUtil.checkServieRunning(getApplicationContext(), canonicalName);
        if (isService == false) {
            Intent intent = null;
            try {
                intent = new Intent(getApplicationContext(), Class.forName(LppService.class.getName()));
                intent.setPackage("com.neighbor.ex.tong");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            if (intent != null) {
//                getApplicationContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
                startService(intent);
            }
        }

    }


    private int mStatus = 0;
    private LppService syncService = null;
    private LppService.LocalCallback mLocalCallback = new LppService.LocalCallback() {
        @Override
        public void serverStatus(int status, int result) {
            mStatus = status;
        }

        @Override
        public void tokenChange(boolean isRequest, String strToken) {
            Log.d("hts", "tokenChange : " + strToken);

            int result = strToken.compareTo(prefs.getString(CONST.ACCOUNT_DEV, ""));
            if (result != 0) {
                SharedPreferences.Editor edittor = prefs.edit();
                edittor.putString(CONST.ACCOUNT_DEV, strToken);
                edittor.commit();
                SendDeviceID devUpdate = new SendDeviceID();
                devUpdate.SendDevice(MainActivity2Activity.this, strToken);
            }

            if (isRequest == true) {
                syncService.registerService(strToken);
            }
        }
    };

    private void bindService() {
        Intent intent = null;
        try {
            intent = new Intent(getApplication().getApplicationContext(), Class.forName(LppService.class.getName()));
            intent.setPackage("com.neighbor.ex.tong");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (intent != null) {
            getApplicationContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private void unbindService() {
        getApplicationContext().unbindService(serviceConnection);
    }


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {

            syncService = ((LppService.LocalBinder) service).getService();
            syncService.setRegistCallback(mLocalCallback);
            syncService.getLppToken();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            syncService = null;
        }
    };


    @Override
    protected void onStop() {
//        Intent intent = new Intent(this, GPSTrack.class);
//        stopService(intent);
        super.onStop();
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
                Log.d("hts", "plateNumber : " + plateNumber);
            }
        }

        @Override
        public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
            if (getFilterQueryProvider() != null) {
                return getFilterQueryProvider().runQuery(constraint);
            }
            String condition = "PLATE_NUMBER LIKE '%" + inputCarNo.getText().toString() + "'";
            Log.d("hts", "condition : " + condition);
            Cursor query = mContent.query(DataProvider.PLATE_URI, null,
                    condition, null, null);
//            if (query == null && inputCarNo.getText().length() == 4) {
//                Toast.makeText(MainActivity2Activity.this, "검색된 결과가 없습니다.", Toast.LENGTH_SHORT).show();
//            }

            return query;
        }

        @Override
        public CharSequence convertToString(Cursor cursor) {
            final int columnIndex = cursor.getColumnIndexOrThrow("PLATE_NUMBER");
            final String str = cursor.getString(columnIndex);
//            Log.d("hts", "str ~~~~~~~~ : " + str);
//
//            if (str.isEmpty() || str == null) {
//                Toast.makeText(MainActivity2Activity.this, "검색된 결과가 없습니다.", Toast.LENGTH_SHORT).show();
//            }
            return str;
        }
    }
}
