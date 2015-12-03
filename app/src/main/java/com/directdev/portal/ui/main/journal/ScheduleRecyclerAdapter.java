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
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.course.setText(data.get(i).getCourseName());
        viewHolder.room.setText(data.get(i).getRoom());
        if (data.get(i).getMode().equals("GSLC")) {
            viewHolder.mode.setText(data.get(i).getMode() + "   ");
            viewHolder.typeIdentifier.setBackgroundColor(Color.parseColor("#f44336"));
            viewHolder.shift.setText("");
        } else {
            viewHolder.shift.setText(data.get(i).getShift().substring(0,5));
            switch (data.get(i).getType()) {
                case "LEC":
                    viewHolder.typeIdentifier.setBackgroundColor(Color.parseColor("#ffeb3b"));
                    viewHolder.mode.setText(data.get(i).getType() + "   ");
                    break;
                case "LAB":
                    viewHolder.typeIdentifier.setBackgroundColor(Color.parseColor("#4caf50"));
                    viewHolder.mode.setText(data.get(i).getType() + "   ");
                    break;
                case "CL":
                    viewHolder.typeIdentifier.setBackgroundColor(Color.parseColor("#00B0FF"));
                    viewHolder.mode.setText(data.get(i).getType() + "    ");
                    break;
                case "TUT":
                    viewHolder.typeIdentifier.setBackgroundColor(Color.parseColor("#00E676"));
                    viewHolder.mode.setText(data.get(i).getType() + "    ");
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
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_journal_schedule, parent, false);
        return new ViewHolder(v);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView course;
        TextView room;
        TextView mode;
        TextView shift;
        TextView typeIdentifier;

        ViewHolder(View itemView) {
            super(itemView);
            course = (TextView) itemView.findViewById(R.id.course);
            room = (TextView) itemView.findViewById(R.id.room);
            shift = (TextView) itemView.findViewById(R.id.shift);
            mode = (TextView) itemView.findViewById(R.id.mode);
            typeIdentifier = (TextView) itemView.findViewById(R.id.type_identifier);
        }
    }

}
