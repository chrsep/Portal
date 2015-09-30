package com.directdev.portal.ui.journal;


import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.directdev.portal.R;
import com.directdev.portal.tools.datatype.ScheduleData;

import org.solovyev.android.views.llm.LinearLayoutManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;


public class JournalRecyclerAdapter extends RecyclerView.Adapter {
    private List<List<ScheduleData>> data;

    public JournalRecyclerAdapter(List<List<ScheduleData>> data) {
        this.data = data;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int i) {
        Calendar c = Calendar.getInstance();
        List<ScheduleData> nestedData = data.get(i);
        ScheduleViewHolder scheduleHolder = (ScheduleViewHolder) holder;
        try {
            scheduleHolder.date = scheduleHolder.dateFormat.parse(data.get(i).get(0).date);
            c.setTime(scheduleHolder.dateFormat.parse(scheduleHolder.today));
        } catch (ParseException e) {
            Log.d("Parse", "Failed");
        }
        scheduleHolder.calendar.setTime(scheduleHolder.date);
        scheduleHolder.adapter = new JournalNestedRecyclerAdapter(nestedData);
        scheduleHolder.nestedRecycler.setAdapter(scheduleHolder.adapter);
        scheduleHolder.nestedRecycler.setLayoutManager(scheduleHolder.layoutManager);
        scheduleHolder.nestedRecycler.setNestedScrollingEnabled(false);
        scheduleHolder.dateTextview.setText(scheduleHolder.dateFormatChanger.format(scheduleHolder.date));

        c.add(Calendar.DATE, 1);
        scheduleHolder.tommorow = scheduleHolder.dateFormat.format(c.getTime());
        if (data.get(i).get(0).date.equals(scheduleHolder.today)) {
            scheduleHolder.day.setText("TODAY");
            scheduleHolder.topbar.setBackgroundColor(Color.parseColor("#00796B"));
        } else if (data.get(i).get(0).date.equals(scheduleHolder.tommorow)) {
            scheduleHolder.day.setText("TOMMOROW");
            scheduleHolder.topbar.setBackgroundColor(Color.parseColor("#00796B"));
        } else {
            scheduleHolder.topbar.setBackgroundColor(Color.parseColor("#424242"));
            switch (scheduleHolder.calendar.get(Calendar.DAY_OF_WEEK)) {
                case Calendar.MONDAY:
                    scheduleHolder.day.setText("MONDAY");
                    break;
                case Calendar.TUESDAY:
                    scheduleHolder.day.setText("TUESDAY");
                    break;
                case Calendar.WEDNESDAY:
                    scheduleHolder.day.setText("WEDNESDAY");
                    break;
                case Calendar.THURSDAY:
                    scheduleHolder.day.setText("THURSDAY");
                    break;
                case Calendar.FRIDAY:
                    scheduleHolder.day.setText("FRIDAY");
                    break;
                case Calendar.SATURDAY:
                    scheduleHolder.day.setText("SATURDAY");
                    break;
                case Calendar.SUNDAY:
                    scheduleHolder.day.setText("SUNDAY");
                    break;
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_journal, parent, false);
        return new ScheduleViewHolder(v);
    }

    public static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        JournalNestedRecyclerAdapter adapter;
        RecyclerView nestedRecycler;
        LinearLayoutManager layoutManager;
        TextView dateTextview;
        TextView day;
        RelativeLayout topbar;
        Date date;
        Calendar calendar = new GregorianCalendar();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today;
        String tommorow;
        SimpleDateFormat dateFormatChanger = new SimpleDateFormat("dd MMMM yy", Locale.getDefault());

        ScheduleViewHolder(View itemView) {
            super(itemView);
            today = dateFormat.format(new Date());
            nestedRecycler = (RecyclerView) itemView.findViewById(R.id.nested_recycler);
            layoutManager = new LinearLayoutManager(itemView.getContext());
            dateTextview = (TextView) itemView.findViewById(R.id.date);
            day = (TextView) itemView.findViewById(R.id.day);
            topbar = (RelativeLayout) itemView.findViewById(R.id.topbar);
        }
    }

}
