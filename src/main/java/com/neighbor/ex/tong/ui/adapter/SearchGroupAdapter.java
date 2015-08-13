package com.neighbor.ex.tong.ui.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.neighbor.ex.tong.R;
import com.neighbor.ex.tong.network.parser.SearchGroupList;

import java.util.ArrayList;

/**
 * Created by ts.ha on 2015-08-06.
 */
public class SearchGroupAdapter extends ArrayAdapter<SearchGroupList.Row> {


    private final String userId;
    private Context context;

    public SearchGroupAdapter(Context context, int resource,
                              int textViewResourceId, ArrayList<SearchGroupList.Row> objects, String userId) {
        super(context, resource, textViewResourceId, objects);
        this.context = context;
        this.userId = userId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//        return super.getView(position, convertView, parent);

        TextView roomName, roomDesc, roomDate, mygroupOwner;
        ImageView ownGroupBtn;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.my_group_row, parent, false);
//            LayoutInflater vi = (LayoutInflater) context.getSystemService(
//                    Context.LAYOUT_INFLATER_SERVICE);
//            convertView = vi.inflate(R.layout.my_group_row, null);
            roomName = (TextView) convertView.findViewById(R.id.mygroup_name);
            roomDesc = (TextView) convertView.findViewById(R.id.textViewDesc);
            roomDate = (TextView) convertView.findViewById(R.id.textViewDate);
            mygroupOwner = (TextView) convertView.findViewById(R.id.mygroup_owner);
            ownGroupBtn = (ImageView) convertView.findViewById(R.id.mygroup_manager_mark);
            convertView.setTag(new ViewHolder(roomName, roomDesc, roomDate, mygroupOwner, ownGroupBtn));
        } else {
            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            roomName = viewHolder.roomName;
            roomDesc = viewHolder.roomDesc;
            roomDate = viewHolder.roomDate;
            mygroupOwner = viewHolder.mygroupOwner;
            ownGroupBtn = viewHolder.ownGroupBtn;
        }

        SearchGroupList.Row item = getItem(position);
        String userNem = item.getROOM_MASTER_GMAIL().replace("@gmail.com", "");

//        Log.d("hts", "userId : " + userId);
        if (userNem.equalsIgnoreCase(userId)) {
            ownGroupBtn.setVisibility(View.VISIBLE);
            ownGroupBtn.setImageResource(R.drawable.icon_group_manager);
        } else if (item.getUSE_FLAG().equalsIgnoreCase("0")) {
            ownGroupBtn.setImageResource(R.drawable.icon_group_confirm);
            ownGroupBtn.setVisibility(View.VISIBLE);
        } else {
            ownGroupBtn.setVisibility(View.INVISIBLE);
        }


        roomName.setText(item.getROOM_NAME());
        roomDesc.setText(item.getROOM_DESC());

        roomDate.setText(item.getREG_DATE());
        mygroupOwner.setText(item.getROOM_MASTER_CAR_NUM());


        return convertView;
    }

    private static class ViewHolder {
        public final TextView roomName;
        public final TextView roomDesc;
        public final TextView roomDate;
        public final TextView mygroupOwner;
        public final ImageView ownGroupBtn;

        public ViewHolder(TextView roomName, TextView roomDesc, TextView roomDate, TextView mygroupOwner, ImageView ownGroupBtn) {
            this.roomName = roomName;
            this.roomDesc = roomDesc;
            this.roomDate = roomDate;
            this.mygroupOwner = mygroupOwner;
            this.ownGroupBtn = ownGroupBtn;
        }
    }

}
