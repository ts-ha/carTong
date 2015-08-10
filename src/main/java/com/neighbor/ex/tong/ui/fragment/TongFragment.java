package com.neighbor.ex.tong.ui.fragment;


import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.android.volley.Request;
import com.neighbor.ex.tong.CONST;
import com.neighbor.ex.tong.R;
import com.neighbor.ex.tong.Utils;
import com.neighbor.ex.tong.bean.GPSInfo;
import com.neighbor.ex.tong.bean.Neighbor;
import com.neighbor.ex.tong.bean.RecentMsg;
import com.neighbor.ex.tong.common.Common;
import com.neighbor.ex.tong.common.SharedPreferenceManager;
import com.neighbor.ex.tong.network.NetworkManager;
import com.neighbor.ex.tong.provider.DataProvider;
import com.neighbor.ex.tong.ui.activity.MainActivity2Activity;
import com.neighbor.ex.tong.ui.activity.SendMsgActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class TongFragment extends Fragment {

    private RadioGroup segGroup;
    private ViewSwitcher segSwitcher;

    private ListView subscriberlist;
    private ListView messagelist;

    private ArrayList<Neighbor> neighbors;
    private ArrayList<RecentMsg> recentTexts;

    private NeighborAdapter adapter;
    private RecentMsgAdapter recentAdapter;

    private Timer mTimer;
    private TimerTask adTast;


    private boolean isTongFragment = true;
    private SharedPreferences prefs;
    //    private LinearLayout mReportLinear;
//    private Button mRegistAccident;
    Handler mHandler;
    private String msg;

    {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case Common.HANDLE_FAIL_REGIST_ACCIDENT:
                        Toast.makeText(getActivity(), "접수 실패 하였습니다. 다시 시도 해주세요.",
                                Toast.LENGTH_LONG).show();
                        break;

                    case Common.HANDLE_SUCCESS_REGIST_ACCIDENT:
                        Toast.makeText(getActivity(), "정상접수 되었습니다.",
                                Toast.LENGTH_LONG).show();
                        break;
                    default:
                }

            }
        };
    }

    private final BroadcastReceiver MsgUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            getRecentMessadeIndex();
            recentAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        neighbors = new ArrayList<Neighbor>();
        recentTexts = new ArrayList<RecentMsg>();

        getRecentMessadeIndex();

        recentAdapter = new RecentMsgAdapter(getActivity(), 0, recentTexts);
        messagelist.setAdapter(recentAdapter);

        mTimer = new Timer();
        adTast = new TimerTask() {
            public void run() {
                new GetNeighbor().execute();
            }
        };
        mTimer.schedule(adTast, 0, 10000);
    }


    void getRecentMessadeIndex() {

        try {
            Cursor cs = getActivity().getContentResolver().query(DataProvider.TONG_URI,
                    null, null, null, "TIME DESC");
            cs.moveToFirst();
            do {
                String isSending = cs.getString(cs.getColumnIndex("DoSend"));
                String receiver = cs.getString(cs.getColumnIndex("RECEIVER"));
                String sender = cs.getString(cs.getColumnIndex("SENDER"));
                String date = cs.getString(cs.getColumnIndex("TIME"));
                RecentMsg item = new RecentMsg();
                item.date = date;
                item.isSend = isSending;
                item.receiver = receiver;
                item.sender = sender;

                boolean isFound = false;

                for (RecentMsg receItem : recentTexts) {
                    if (receItem.isSend.equalsIgnoreCase("true")) {
                        int result = receItem.receiver.compareToIgnoreCase(item.receiver);
                        if (result == 0) {
                            isFound = true;
                            break;
                        } else
                            continue;
                    } else {
                        int result = receItem.sender.compareToIgnoreCase(item.sender);
                        if (result == 0) {
                            isFound = true;
                            break;
                        } else
                            continue;
                    }
                }
                if (!isFound) {
                    recentTexts.add(item);
                }

            } while (cs.moveToNext());
            cs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        prefs =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
        View mainView = inflater.inflate(R.layout.fragment_tong, container, false);

        segSwitcher = (ViewSwitcher) mainView.findViewById(R.id.sort_menu);
        ImageButton imageButtonLandslide = (ImageButton) mainView.findViewById(R.id.imageButtonLandslide);
        ImageButton imageButtonFlooding = (ImageButton) mainView.findViewById(R.id.imageButtonFlooding);
        ImageButton imageButtonHelp = (ImageButton) mainView.findViewById(R.id.imageButtonHelp);
        ImageButton imageButtonAccident = (ImageButton) mainView.findViewById(R.id.imageButtonAccident);
        imageButtonLandslide.setOnClickListener(onClickListener);
        imageButtonFlooding.setOnClickListener(onClickListener);
        imageButtonHelp.setOnClickListener(onClickListener);
        imageButtonAccident.setOnClickListener(onClickListener);


        segGroup = (RadioGroup) mainView.findViewById(R.id.sort);
        segGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.order_distance) {
                    segSwitcher.showPrevious();
                } else {
                    segSwitcher.showNext();
                    getRecentMessadeIndex();
                    recentAdapter.setNotifyOnChange(true);
                    recentAdapter.notifyDataSetChanged();
                }
            }
        });

        subscriberlist = (ListView) mainView.findViewById(R.id.subscriber_list);
        subscriberlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Neighbor subscriber = adapter.getItem(position);
                if (!subscriber.CarNo.equalsIgnoreCase("운영자")) {
                    Intent intent = new Intent(getActivity(), SendMsgActivity.class);
                    intent.putExtra("destNo", subscriber.CarNo);
                    startActivity(intent);
                    subscriberlist.setEnabled(false);
                }
            }
        });

        messagelist = (ListView) mainView.findViewById(R.id.message_list);
        messagelist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String carNo = (String) view.getTag();
                if (!carNo.equalsIgnoreCase("운영자")) {
                    Intent intent = new Intent(getActivity(), SendMsgActivity.class);
                    intent.putExtra("destNo", carNo);
                    startActivity(intent);
                    messagelist.setEnabled(false);
                }
            }
        });
        return mainView;
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.imageButtonLandslide:
                    showDialogReport(4);
                    break;
                case R.id.imageButtonFlooding:
                    showDialogReport(3);
                    break;
                case R.id.imageButtonHelp:
                    showDialogReport(2);
                    break;
                case R.id.imageButtonAccident:
                    showDialogReport(1);
                    break;
                default:
                    break;
            }
        }
    };

    private void showDialogReport(final int msgTyp) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle("제보를 하시겠습니까?");

        if (msgTyp == 1) {
            alert.setMessage(R.string.msg_accident);
            msg = getResources().getString(R.string.msg_accident);
        } else if (msgTyp == 2) {
            alert.setMessage(R.string.msg_help);
            msg = getResources().getString(R.string.msg_help);
        } else if (msgTyp == 3) {
            alert.setMessage(R.string.msg_flooding);
            msg = getResources().getString(R.string.msg_flooding);
        } else if (msgTyp == 4) {
            alert.setMessage(R.string.msg_landslid);
            msg = getResources().getString(R.string.msg_landslid);
        }
        alert.setCancelable(false);
        alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String license = SharedPreferenceManager.getValue(getActivity().getApplicationContext(),
                        CONST.ACCOUNT_ID);
                Location lastLocation = ((MainActivity2Activity) getActivity()).getLastLocation();
                String url;
                try {
                    msg = URLEncoder.encode(msg, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (lastLocation != null) {
                    url = String.format(Common.REGIST_ACCIDENT, msg, license,
                            lastLocation.getLongitude(), lastLocation.getLatitude());
                } else {
                    url = String.format(Common.REGIST_ACCIDENT, msg, license,
                            0, 0);
                }
                NetworkManager.getInstance(getActivity()).
                        requestJsonObject(getActivity(), url,
                                Request.Method.GET, mHandler,
                                Common.HANDLE_SUCCESS_REGIST_ACCIDENT, Common.HANDLE_FAIL_REGIST_ACCIDENT);
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }


    @Override
    public void onStop() {
        super.onStop();

        if (mTimer != null) {
            mTimer.purge();
            mTimer.cancel();
        }
        mTimer = null;
        if (adTast != null)
            adTast.cancel();
        adTast = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        subscriberlist.setEnabled(true);
        messagelist.setEnabled(true);
        getActivity().registerReceiver(MsgUpdateReceiver,
                new IntentFilter(CONST.TONG_MESSAGE_UPDATE));
    }

    @Override
    public void onPause() {
        segGroup.check(R.id.order_distance);
        getActivity().unregisterReceiver(MsgUpdateReceiver);
        isTongFragment = false;
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
        }
        mTimer = null;
        if (adTast != null)
            adTast.cancel();
        adTast = null;
        super.onPause();
    }


    private class GetNeighbor extends AsyncTask<Void, Void, ArrayList> {

        private final static
        String URL = "http://211.189.132.184:8080/Tong/requestAroundMember.do?gpsLat=";

        public GetNeighbor() {
            super();
        }

        @Override
        protected ArrayList doInBackground(Void... params) {
            GPSInfo info = GPSInfo.CreateInstance();
            HashMap<String, Double> gpsMap = info.getGPSInfo();
            Location lastLocation = null;
            if (((MainActivity2Activity) getActivity()) != null) {
                lastLocation = ((MainActivity2Activity) getActivity()).getLastLocation();
            }
            double lonLatitude;
            double lonLongitude;
            if (lastLocation != null) {
                lonLatitude = lastLocation.getLatitude();
                lonLongitude = lastLocation.getLongitude();
            } else {
                lonLatitude = 0;
                lonLongitude = 0;
            }

            StringBuilder makeUri = new StringBuilder();
            makeUri.append(URL);
            makeUri.append(lonLatitude);
            makeUri.append("&gpsLong=");
            makeUri.append(lonLongitude);
            makeUri.append("&radius=");
            String Distance = prefs.getString("radius", "1km");
            makeUri.append(Distance.substring(0, 1));
            makeUri.append("&memberGmail=");
            makeUri.append(SharedPreferenceManager.getValue(getActivity().getApplicationContext(), CONST.ACCOUNT_ID) + "@gmail.com");


            String jsonResult = Utils.getJSON(makeUri.toString(), 15000);

            try {
//                Log.d("TongFragment", "jsonResult :" + jsonResult);
                JSONObject rootObject = new JSONObject(jsonResult);
                JSONArray jsonArray = (JSONArray) rootObject.get("rows");
                if (jsonArray.length() > 0) neighbors.clear();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = (JSONObject) jsonArray.get(i);

                    String carNumer = "";
                    if (obj.has("MEMBER_CAR_NUM")) {
                        carNumer = obj.getString("MEMBER_CAR_NUM");
                        String myCarNo = prefs.getString(CONST.ACCOUNT_LICENSE, "");

                        if (carNumer.equalsIgnoreCase(myCarNo)) continue;

                        String gps_lat = obj.getString("GPS_LAT");
                        String gps_lon = obj.getString("GPS_LONG");
                        String stDate = obj.getString("UP_DATE");

                        Neighbor neighbor = new Neighbor();
                        neighbor.CarNo = carNumer;
                        neighbor.Latitude = gps_lat;
                        neighbor.Longitude = gps_lon;

                        StringBuffer sb = new StringBuffer();
                        sb.append(stDate, 0, 4);
                        sb.append("-");
                        sb.append(stDate, 4, 6);
                        sb.append("-");
                        sb.append(stDate, 6, 8);
                        sb.append(" ");
                        sb.append(stDate, 8, 10);
                        sb.append(":");
                        sb.append(stDate, 10, 12);

                        neighbor.UpdateTime = sb.toString();
                        Location userLocation = new Location("");
                        userLocation.setLatitude(Double.parseDouble(neighbor.Latitude));
                        userLocation.setLongitude(Double.parseDouble(neighbor.Longitude));
                        double locDistance = lastLocation.distanceTo(userLocation);

                        String pattern = "###";
                        DecimalFormat dformat = new DecimalFormat(pattern);

                        neighbor.Distance = dformat.format(locDistance);
//                        Log.d("hts", " neighbor.Distance : " + neighbor.Distance);
                        neighbors.add(neighbor);
                    } else
                        continue;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return neighbors;
        }

        @Override
        protected void onPostExecute(ArrayList arrayList) {
            super.onPostExecute(arrayList);
            try {
                Collections.sort(arrayList, new NameAscCompare());
                adapter = new NeighborAdapter(getActivity(), 0, arrayList);
                subscriberlist.setAdapter(adapter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class NameAscCompare implements Comparator<Neighbor> {

        @Override
        public int compare(Neighbor arg0, Neighbor arg1) {
            return Integer.parseInt(arg0.Distance) < Integer.parseInt(arg1.Distance) ? 0 : 1;
//            return arg0.Distance.compareTo(arg1.Distance);
        }
    }

    class NeighborAdapter extends ArrayAdapter<Neighbor> {

        private ArrayList<Neighbor> arrData;
        private LayoutInflater inflater;

        public NeighborAdapter(Context context, int resource, ArrayList<Neighbor> objects) {
            super(context, resource, objects);
            this.arrData = objects;
            inflater = (LayoutInflater) context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public Neighbor getItem(int position) {
            return arrData.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = inflater.inflate(R.layout.neighbor_list_row, parent, false);
            }

            Neighbor data = getItem(position);
            if (data != null) {
                TextView userCarNo = (TextView) v.findViewById(R.id.neighbor_carNo);
                userCarNo.setText(data.CarNo);

                TextView userDistance = (TextView) v.findViewById(R.id.neighbor_distance);
                userDistance.setText(data.Distance + " m");

                TextView userUpdateDate = (TextView) v.findViewById(R.id.neighbor_date);
                userUpdateDate.setText(data.UpdateTime);
            }
            return v;
        }
    }

    /* RECENT MESSAGE List Adapter */
    class RecentMsgAdapter extends ArrayAdapter<RecentMsg> {
        private ArrayList<RecentMsg> arrData;
        private LayoutInflater inflater;

        public RecentMsgAdapter(Context context, int resource, ArrayList<RecentMsg> objects) {
            super(context, resource, objects);
            this.arrData = objects;
            inflater = (LayoutInflater) context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public RecentMsg getItem(int position) {
            return arrData.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = inflater.inflate(R.layout.neighbor_list_row, parent, false);
            }

            RecentMsg data = getItem(position);
            if (data != null) {
                TextView userCarNo = (TextView) v.findViewById(R.id.neighbor_carNo);
                TextView userDistance = (TextView) v.findViewById(R.id.neighbor_distance);
                TextView userUpdateDate = (TextView) v.findViewById(R.id.neighbor_date);

                StringBuffer sb = new StringBuffer();
                sb.append(data.date, 0, 4);
                sb.append("-");
                sb.append(data.date, 4, 6);
                sb.append("-");
                sb.append(data.date, 6, 8);
                sb.append(" ");
                sb.append(data.date, 8, 10);
                sb.append(":");
                sb.append(data.date, 10, 12);

                if (data.isSend.equalsIgnoreCase("true")) {
                    userCarNo.setText(data.receiver);
                    userDistance.setText("보낸메세지");
                } else {
                    userCarNo.setText(data.sender);
                    userDistance.setText("받은메세지");
                }

                userUpdateDate.setText(sb.toString());
                v.setTag(userCarNo.getText());
            }

            return v;
        }
    }
}
