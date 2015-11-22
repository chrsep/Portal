package com.directdev.portal.ui.main.journal;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.directdev.portal.R;
import com.directdev.portal.tools.model.Schedule;

import io.realm.RealmResults;


public class ScheduleRecyclerAdapter extends RecyclerView.Adapter {
    private RealmResults<Schedule> data;

    public ScheduleRecyclerAdapter(RealmResults<Schedule> data) {
        this.data = data;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int i) {
        ScheduleViewHolder scheduleHolder = (ScheduleViewHolder) holder;
        scheduleHolder.course.setText(data.get(i).getCourseName());
        scheduleHolder.room.setText(data.get(i).getRoom());
        if (data.get(i).getMode().equals("GSLC")) {
            scheduleHolder.mode.setText(data.get(i).getMode() + "   ");
            scheduleHolder.typeIdentifier.setBackgroundColor(Color.parseColor("#f44336"));
            scheduleHolder.shift.setText("");
        } else {
            scheduleHolder.shift.setText(data.get(i).getShift().substring(0,5));
            switch (data.get(i).getType()) {
                case "LEC":
                    scheduleHolder.typeIdentifier.setBackgroundColor(Color.parseColor("#ffeb3b"));
                    scheduleHolder.mode.setText(data.get(i).getType() + "   ");
                    break;
                case "LAB":
                    scheduleHolder.typeIdentifier.setBackgroundColor(Color.parseColor("#4caf50"));
                    scheduleHolder.mode.setText(data.get(i).getType() + "   ");
                    break;
                case "CL":
                    scheduleHolder.typeIdentifier.setBackgroundColor(Color.parseColor("#00B0FF"));
                    scheduleHolder.mode.setText(data.get(i).getType() + "    ");
                    break;
                case "TUT":
                    scheduleHolder.typeIdentifier.setBackgroundColor(Color.parseColor("#00E676"));
                    scheduleHolder.mode.setText(data.get(i).getType() + "    ");
            }
        }
    }

    @Override
    public int getItemCount() {
        try {
            return data.size();
        } catch (NullPointerException e) {
            return 0;
        }
    }

    @Override
    public ScheduleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_journal_schedule, parent, false);
        return new ScheduleViewHolder(v);
    }

    public static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        TextView course;
        TextView room;
        TextView mode;
        TextView shift;
        TextView typeIdentifier;

        ScheduleViewHolder(View itemView) {
            super(itemView);
            course = (TextView) itemView.findViewById(R.id.course);
            room = (TextView) itemView.findViewById(R.id.room);
            shift = (TextView) itemView.findViewById(R.id.shift);
            mode = (TextView) itemView.findViewById(R.id.mode);
            typeIdentifier = (TextView) itemView.findViewById(R.id.type_identifier);
        }
    }
}
