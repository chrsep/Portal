package com.directdev.portal.ui.journal;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.directdev.portal.R;
import com.directdev.portal.tools.datatype.ScheduleData;

import java.util.List;


public class JournalNestedRecyclerAdapter extends RecyclerView.Adapter{
    private List<ScheduleData> data;

    public JournalNestedRecyclerAdapter(List<ScheduleData> data){
        this.data = data;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int i) {
        ScheduleViewHolder scheduleHolder = (ScheduleViewHolder) holder;
        scheduleHolder.course.setText(data.get(i).coursename);
        scheduleHolder.room.setText(data.get(i).room);
        if(data.get(i).mode.equals("GSLC")){
            scheduleHolder.mode.setText(data.get(i).mode+"   ");
            scheduleHolder.typeIdentifier.setBackgroundColor(Color.parseColor("#f44336"));
            scheduleHolder.shift.setText("");
        }else {
            scheduleHolder.shift.setText(data.get(i).shift[0]);
            switch (data.get(i).type){
                case "LEC":
                    scheduleHolder.typeIdentifier.setBackgroundColor(Color.parseColor("#ffeb3b"));
                    scheduleHolder.mode.setText(data.get(i).type + "   ");
                    break;
                case "LAB":
                    scheduleHolder.typeIdentifier.setBackgroundColor(Color.parseColor("#4caf50"));
                    scheduleHolder.mode.setText(data.get(i).type + "   ");
                    break;
                case "CL":
                    scheduleHolder.typeIdentifier.setBackgroundColor(Color.parseColor("#00B0FF"));
                    scheduleHolder.mode.setText(data.get(i).type+"    ");
                    break;
                case "TUT":
                    scheduleHolder.typeIdentifier.setBackgroundColor(Color.parseColor("#00E676"));
                    scheduleHolder.mode.setText(data.get(i).type+"    ");
            }
        }
    }

    @Override
    public int getItemCount() {
        try {
            return data.size();
        }catch (NullPointerException e){
            return 0;
        }
    }

    @Override
    public ScheduleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nested_journal, parent, false);
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
            course = (TextView)itemView.findViewById(R.id.course);
            room = (TextView)itemView.findViewById(R.id.room);
            shift = (TextView)itemView.findViewById(R.id.shift);
            mode = (TextView)itemView.findViewById(R.id.mode);
            typeIdentifier = (TextView) itemView.findViewById(R.id.type_identifier);
        }
    }
}
