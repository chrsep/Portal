package com.directdev.portal.ui.main.journal;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.directdev.portal.R;
import com.directdev.portal.tools.model.Exam;

import io.realm.RealmResults;

/**
 *  This is a recyclerView Adapter, it's job is to bind data to the views in the recyclerView
 *  this specific adapter is used to display the exam data inside Journal tab
 */

public class ExamRecyclerAdapter extends RecyclerView.Adapter {
    private RealmResults<Exam> data;

    public ExamRecyclerAdapter(RealmResults<Exam> data) {
        this.data = data;
    }

    //This binds data to the view
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int i) {
        ScheduleViewHolder viewHolder = (ScheduleViewHolder) holder;
        final Exam exam = data.get(i);
        Log.d("onBindViewHolder", "onBindViewHolder: " + exam.getCOURSE_TITLE_LONG());
        viewHolder.name.setText(exam.getCOURSE_TITLE_LONG());
        viewHolder.chair.setText("Seat: "+exam.getChairNumber());
        viewHolder.room.setText(exam.getROOM());
        viewHolder.time.setText(exam.getExamStartTime().substring(0,5)+"  ||  "+exam.getDuration()+" mins");
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_journal_exam, parent, false);
        return new ScheduleViewHolder(v);
    }

    //This to holds all the view
    public static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView chair;
        TextView room;
        TextView time;

        ScheduleViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.journal_exam_name);
            chair = (TextView) itemView.findViewById(R.id.journal_exam_chair);
            time = (TextView) itemView.findViewById(R.id.journal_exam_shift);
            room = (TextView) itemView.findViewById(R.id.journal_exam_room);
        }
    }
}
