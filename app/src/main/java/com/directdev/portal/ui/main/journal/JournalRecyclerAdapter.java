package com.directdev.portal.ui.main.journal;


import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.directdev.portal.R;
import com.directdev.portal.tools.model.Exam;
import com.directdev.portal.tools.model.Finance;
import com.directdev.portal.tools.model.Schedule;

import org.solovyev.android.views.llm.LinearLayoutManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;


public class JournalRecyclerAdapter extends RecyclerView.Adapter {
    private List<Date> data;
    private Context ctx;

    public JournalRecyclerAdapter(Context ctx, List<Date> data) {
        this.data = data;
        this.ctx = ctx;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int i) {
        ScheduleViewHolder scheduleHolder = (ScheduleViewHolder) holder;
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Schedule> schedules = realm.where(Schedule.class).equalTo("Date", data.get(i)).findAll();
        RealmResults<Exam> exams = realm.where(Exam.class).equalTo("ExamDate", data.get(i)).findAll();
        RealmResults<Finance> finance = realm.where(Finance.class).equalTo("ITEM_EFFECTIVE_DT", data.get(i)).equalTo("ITEM_TYPE_CD","C").findAll();


        SimpleDateFormat toDay = new SimpleDateFormat("EEEE",Locale.US);
        SimpleDateFormat date = new SimpleDateFormat("dd MMM yyyy",Locale.US);
        Calendar cal = new GregorianCalendar();
        scheduleHolder.dateTextView.setText(date.format(data.get(i)));
        final Date today = cal.getTime();
        if (date.format(today).equals(date.format(data.get(i)))) {
            scheduleHolder.dayTextView.setText("TODAY");
            scheduleHolder.topBarBg.setBackgroundColor(Color.parseColor("#009688"));
            scheduleHolder.dayTextView.setTextColor(Color.parseColor("#FFFFFF"));
            scheduleHolder.dateTextView.setTextColor(Color.parseColor("#FFFFFF"));
            if(schedules.isEmpty() && exams.isEmpty()){
                scheduleHolder.dayTextView.setText("TODAY - HOLIDAY");
            }
        }  else {
            if(schedules.isEmpty() && exams.isEmpty()){
                scheduleHolder.dayTextView.setTextColor(Color.parseColor("#FFEBEE"));
                scheduleHolder.dateTextView.setTextColor(Color.parseColor("#FFEBEE"));
            }else{
                scheduleHolder.dayTextView.setTextColor(Color.parseColor("#FFFFFF"));
                scheduleHolder.dateTextView.setTextColor(Color.parseColor("#FFFFFF"));
            }
            scheduleHolder.topBarBg.setBackgroundColor(Color.parseColor("#424242"));
            scheduleHolder.dayTextView.setText(toDay.format(data.get(i)).toUpperCase());
        }

        ScheduleRecyclerAdapter scheduleAdapter = new ScheduleRecyclerAdapter(schedules);
        scheduleHolder.scheduleRecycler.setLayoutManager(new LinearLayoutManager(ctx));
        scheduleHolder.scheduleRecycler.setAdapter(scheduleAdapter);

        ExamRecyclerAdapter examRecyclerAdapter = new ExamRecyclerAdapter(exams);
        scheduleHolder.examRecycler.setLayoutManager(new LinearLayoutManager(ctx));
        scheduleHolder.examRecycler.setAdapter(examRecyclerAdapter);

        scheduleHolder.scheduleRecycler.setNestedScrollingEnabled(false);
        scheduleHolder.examRecycler.setNestedScrollingEnabled(false);
        realm.close();
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_journal, parent, false);
        return new ScheduleViewHolder(v);
    }

    public static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        TextView dayTextView;
        RelativeLayout topBarBg;
        LinearLayout journalCardview;
        RecyclerView scheduleRecycler;
        RecyclerView financeRecycler;
        RecyclerView examRecycler;
        ScheduleViewHolder(View itemView) {
            super(itemView);
            dateTextView = (TextView) itemView.findViewById(R.id.date);
            dayTextView = (TextView) itemView.findViewById(R.id.day);
            topBarBg = (RelativeLayout) itemView.findViewById(R.id.topbar);
            journalCardview = (LinearLayout) itemView.findViewById(R.id.journal_cardview);

            scheduleRecycler = (RecyclerView) itemView.findViewById(R.id.schedule_journal_recycler);
            financeRecycler = (RecyclerView) itemView.findViewById(R.id.finance_journal_recycler);
            examRecycler = (RecyclerView) itemView.findViewById(R.id.exam_journal_recycler);
        }
    }

}
