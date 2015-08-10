package com.neighbor.ex.tong.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.neighbor.ex.tong.R;
import com.neighbor.ex.tong.common.Common;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by User on 2015-06-04.
 */
public class GroupAdapter extends ArrayAdapter<HashMap<String, String>> {

    private final static String GROUP_JOIN_URL = "http://211.189.132.184:8080/Tong/regTongMemberInfo.do?roomId=";
    protected ArrayList<HashMap<String, String>> groupList;
    protected Context context;

    private static final String ROOM_OWNER = "ROOM_MASTER_GMAIL";
    private static final String ROOM_ID = "ROOM_ID";
    private static final String ROOM_NAME = "ROOM_NAME";

    public GroupAdapter(Context context, int textViewResourceId,
                        ArrayList<HashMap<String, String>> cafeList) {
        super(context, textViewResourceId, cafeList);
//        this.groupList = new ArrayList<HashMap<String, String>>();
//        this.groupList.addAll(cafeList);
        groupList = cafeList;
        this.context = context;
    }

    @Override
    public void add(HashMap<String, String> object) {
        super.add(object);
        groupList.add(object);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.all_group_row, null);
        }

        if (groupList.size() > 0) {
            HashMap<String, String> row = groupList.get(position);
            TextView roomName = (TextView) convertView.findViewById(R.id.group_name);
            TextView roomDesc = (TextView) convertView.findViewById(R.id.textViewDesc);
            TextView roomDate = (TextView) convertView.findViewById(R.id.textViewDate);
            TextView mygroupOwner = (TextView) convertView.findViewById(R.id.mygroup_owner);
            roomName.setText(row.get(ROOM_NAME));

            ImageView ownGroupBtn = (ImageView) convertView.findViewById(R.id.mygroup_manager_mark);

            StringBuffer OwnMail = new StringBuffer();

            final String Owner = OwnMail.toString();
            final String RoomOwner = row.get(ROOM_OWNER);
            mygroupOwner.setText(row.get(Common.ROOM_MASTER_CAR_NUM).replace("@gmail.com", ""));
            roomDesc.setText(row.get(Common.ROOM_DESC));
            roomDate.setText(row.get(Common.REG_DATE).substring(0, 10));

            roomName.setText(row.get(ROOM_NAME));
//            ImageButton joinBtn = (ImageButton) convertView.findViewById(R.id.group_join);
//            joinBtn.setTag(row);

        }
        return convertView;
    }
}
