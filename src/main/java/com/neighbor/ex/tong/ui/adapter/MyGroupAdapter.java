package com.neighbor.ex.tong.ui.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.neighbor.ex.tong.CONST;
import com.neighbor.ex.tong.R;
import com.neighbor.ex.tong.common.Common;

import java.util.ArrayList;
import java.util.HashMap;


public class MyGroupAdapter extends ArrayAdapter<HashMap<String, String>> {
    protected ArrayList<HashMap<String, String>> myList;
    protected Context context;
    protected String OwnID;

    private static final String ROOM_ID = "ROOM_ID";
    private static final String ROOM_NAME = "ROOM_NAME";
    private static final String ROOM_OWNER = "ROOM_MASTER_GMAIL";

    public MyGroupAdapter(Context context, int textViewResourceId,
                          ArrayList<HashMap<String, String>> cafeList) {
        super(context, textViewResourceId, cafeList);
        this.myList = cafeList;
//        this.myList.addAll(cafeList);
        this.context = context;
        SharedPreferences pref = PreferenceManager.
                getDefaultSharedPreferences(context);

        this.OwnID = pref.getString(CONST.ACCOUNT_ID, "");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.my_group_row, null);
        }

        if (myList.size() > 0) {
            HashMap<String, String> row = myList.get(position);
            TextView roomName = (TextView) convertView.findViewById(R.id.mygroup_name);
            TextView roomDesc = (TextView) convertView.findViewById(R.id.textViewDesc);
            TextView roomDate = (TextView) convertView.findViewById(R.id.textViewDate);
            TextView mygroupOwner = (TextView) convertView.findViewById(R.id.mygroup_owner);
            roomName.setText(row.get(ROOM_NAME));

            ImageView ownGroupBtn = (ImageView) convertView.findViewById(R.id.mygroup_manager_mark);

            StringBuffer OwnMail = new StringBuffer();
            OwnMail.append(OwnID);
            OwnMail.append("@gmail.com");

            final String Owner = OwnMail.toString();
            final String RoomOwner = row.get(ROOM_OWNER);
            mygroupOwner.setText(row.get(Common.ROOM_MASTER_CAR_NUM));
            roomDesc.setText(row.get(Common.ROOM_DESC));
            roomDate.setText(row.get(Common.REG_DATE).substring(0, 10));


            if (RoomOwner.equalsIgnoreCase(Owner)) {
                ownGroupBtn.setVisibility(View.VISIBLE);
                ownGroupBtn.setImageResource(R.drawable.icon_group_manager);
            } else if (row.get(Common.USE_FLAG).equalsIgnoreCase("0")) {
                ownGroupBtn.setImageResource(R.drawable.icon_group_confirm);
                ownGroupBtn.setVisibility(View.VISIBLE);
            } else {
                ownGroupBtn.setVisibility(View.INVISIBLE);
            }
        }
        return convertView;
    }
}
