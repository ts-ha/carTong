package com.neighbor.ex.tong.ui.fragment;


import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.neighbor.ex.tong.CONST;
import com.neighbor.ex.tong.R;
import com.neighbor.ex.tong.Utils;
import com.neighbor.ex.tong.bean.GPSInfo;
import com.neighbor.ex.tong.bean.Neighbor;
import com.neighbor.ex.tong.common.SharedPreferenceManager;
import com.neighbor.ex.tong.ui.activity.MainActivity2Activity;
import com.neighbor.ex.tong.ui.activity.SendGroupMsgActivity;
import com.neighbor.ex.tong.ui.activity.SendMsgActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class MapFragment extends Fragment {

    private GoogleMap mMap;
    private ArrayList<Neighbor> neighbors;
    private Marker mMarker;
    private Timer mTimer;
    private TimerTask adTast;
    private SharedPreferences prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.activity_map, container, false);
        prefs =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
        ImageButton report = (ImageButton) mainView.findViewById(R.id.btn_report);
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity().getApplicationContext(), SendGroupMsgActivity.class);
                startActivity(intent);
            }
        });


        MapView mapView = (MapView) mainView.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMap = mapView.getMap();

        setUpMapIfNeeded();
        mMap.setOnMyLocationChangeListener(myLocationChangeListener);
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                String carNo = marker.getTitle();
                Intent intent = new Intent(getActivity().getApplicationContext(), SendMsgActivity.class);
                intent.putExtra("destNo", carNo);
                startActivity(intent);
            }
        });


        neighbors = new ArrayList<>();
//        mTimer = new Timer();
//        adTast = new TimerTask() {
//            public void run() {
//                new GetNeighbor().execute();
//            }
//        };
//        mTimer.schedule(adTast, 0, 10000);
        return mainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        neighbors = new ArrayList<>();
        mTimer = new Timer();
        adTast = new TimerTask() {
            public void run() {
                new GetNeighbor().execute();
            }
        };
        mTimer.schedule(adTast, 0, 10000);
    }

    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener =
            new GoogleMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(Location location) {
                    LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
                    GPSInfo info = GPSInfo.CreateInstance();
//                    Log.d("hts", "myLocationChangeListener : " + loc.toString());
                    info.setGPSinfo(location.getLatitude(), location.getLongitude());

                    mMarker = mMap.addMarker(new MarkerOptions().position(loc));
                }
            };

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        //getMenuInflater().inflate(R.menu.menu_map, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public void onBackPressed() {
//        this.finish();
//    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        Location location = ((MainActivity2Activity) getActivity()).getLastLocation();
        if (mMap != null && location != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()), 16.0f));
        }
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
        if (mMap != null) {
            mMap.setOnMyLocationChangeListener(null);
            mMap = null;
        }
    }

    private void setUpMapIfNeeded() {
        if (mMap != null) {
            mMap.setMyLocationEnabled(true);
//            mMap.getMyLocation();
            mMap.getUiSettings().setZoomControlsEnabled(true);
        }
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

            Double pos_x = 0.0;
            Double pos_y = 0.0;

            pos_x = gpsMap.get(CONST.GPS_LATITUDE);
            pos_y = gpsMap.get(CONST.GPS_LONGITUDE);

            if (pos_x == 0 && pos_y == 0) {
                long lonLatitude = prefs.getLong(CONST.GPS_LATITUDE, 0);
                long lonLongitude = prefs.getLong(CONST.GPS_LONGITUDE, 0);
            }

            Location lastLocation = ((MainActivity2Activity) getActivity()).getLastLocation();


            if (lastLocation != null) {
                pos_x = lastLocation.getLatitude();
                pos_y = lastLocation.getLongitude();
            } else {
                pos_x = 0.0;
                pos_y = 0.0;
            }

            StringBuilder makeUri = new StringBuilder();
            makeUri.append(URL);
            makeUri.append(pos_x);
            makeUri.append("&gpsLong=");
            makeUri.append(pos_y);
            makeUri.append("&radius=");

            String Distance = prefs.getString("radius", "1km");
            makeUri.append(Distance.substring(0, 1));

            makeUri.append("&memberGmail=");
            makeUri.append(SharedPreferenceManager.getValue(getActivity(), CONST.ACCOUNT_ID) + "@gmail.com");

            String jsonResult = Utils.getJSON(makeUri.toString(), 15000);

            try {
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

                        double locDistance = Utils.calDistance(
                                Double.parseDouble(neighbor.Latitude),
                                Double.parseDouble(neighbor.Longitude),
                                pos_x, pos_y);

                        String pattern = "###";
                        DecimalFormat dformat = new DecimalFormat(pattern);
                        neighbor.Distance = dformat.format(locDistance);
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
            if (mMap != null && neighbors != null && !neighbors.isEmpty()) {
                mMap.clear();
                for (Neighbor scriber : neighbors) {
                    MarkerOptions option = new MarkerOptions();
                    double latitude = Double.parseDouble(scriber.Latitude);
                    double longitude = Double.parseDouble(scriber.Longitude);
                    option.position(new LatLng(latitude, longitude));
                    option.title(scriber.CarNo);
                    mMap.addMarker(option);
                }
            }
        }
    }
}

