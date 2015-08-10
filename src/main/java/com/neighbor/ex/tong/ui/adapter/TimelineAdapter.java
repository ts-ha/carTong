package com.neighbor.ex.tong.ui.adapter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.neighbor.ex.tong.R;
import com.neighbor.ex.tong.bean.TimeLineInfo;
import com.neighbor.ex.tong.ui.activity.TimelineActivity;

import java.util.List;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.TimelineHolder> {

    List<TimeLineInfo> timelineList;

    public TimelineAdapter(List<TimeLineInfo> timelinelist) {
        this.timelineList = timelinelist;
    }

    @Override
    public TimelineHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.timeline_row, parent, false);
        return new TimelineHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TimelineHolder holder, int position) {
        TimeLineInfo timeinfo = timelineList.get(position);
        holder.vId.setText(timeinfo.content_postId);
        holder.vContent.setText(timeinfo.content_comment);
        holder.vReact.setTag(timeinfo);
        holder.date.setText(timeinfo.regDate);
        holder.vReact.setText("댓글(" + timeinfo.content_rlyCount + ")");
    }

    @Override
    public int getItemCount() {
        return timelineList.size();
    }

    public static class TimelineHolder extends RecyclerView.ViewHolder {
        protected TextView vId;
        protected TextView vContent;
        protected Button vReact;
        protected TextView date;

        public TimelineHolder(View v) {
            super(v);

            vId = (TextView) v.findViewById(R.id.timeline_id);
            date = (TextView) v.findViewById(R.id.timeline_date);
            vContent = (TextView) v.findViewById(R.id.timeline_contents);
            vReact = (Button) v.findViewById(R.id.btn_show);
            vReact.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final TimeLineInfo info = (TimeLineInfo) view.getTag();

                    Intent intent = new Intent(view.getContext(), TimelineActivity.class);
                    intent.putExtra("Id", info.content_id);
                    intent.putExtra("Message", info.content_comment);
                    intent.putExtra("postId", info.content_postId);
                    intent.putExtra("roomName", info.content_roomName);
                    view.getContext().startActivity(intent);
                }
            });
        }
    }
}
