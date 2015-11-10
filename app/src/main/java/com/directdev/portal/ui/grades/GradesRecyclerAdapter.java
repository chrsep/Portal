package com.directdev.portal.ui.grades;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.directdev.portal.R;
import com.directdev.portal.tools.database.CourseDB;
import com.directdev.portal.tools.datatype.ScoreData;

import java.util.List;

public class GradesRecyclerAdapter extends RecyclerView.Adapter {
    private List<String> course;
    private CourseDB db;
    private ScoreData data;

    public GradesRecyclerAdapter(List<String> data, Context ctx) {
        this.course = data;
        db = new CourseDB(ctx);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int i) {
        data = db.queryGrades(course.get(i));
        GradesViewHolder gradesViewHolder = (GradesViewHolder) holder;
        gradesViewHolder.courseName.setText(course.get(i));
        gradesViewHolder.courseGrade.setText(data.grade);
        if (!data.mid.equals("0")) {
            gradesViewHolder.mid.setVisibility(View.VISIBLE);
            gradesViewHolder.mid.setText("Mid Exam    \t: " + data.mid);
        }
        if (!data.fin.equals("0")) {
            gradesViewHolder.fin.setVisibility(View.VISIBLE);
            gradesViewHolder.fin.setText("Final Exam  \t: " + data.fin);
        }
        if (!data.lab.equals("0")) {
            gradesViewHolder.lab.setVisibility(View.VISIBLE);
            gradesViewHolder.lab.setText("Laboratory  \t: " + data.lab);
        }
        if (!data.assignment.equals("0")) {
            gradesViewHolder.assignment.setVisibility(View.VISIBLE);
            gradesViewHolder.assignment.setText("Assignment\t: " + data.assignment);
        }

        if (data.grade.substring(0, 1).equals("A")) {
            gradesViewHolder.cardView.setCardBackgroundColor(Color.parseColor("#1565c0"));
        } else if (data.grade.substring(0, 1).equals("B")) {
            gradesViewHolder.cardView.setCardBackgroundColor(Color.parseColor("#1b5e20"));
        } else if (data.grade.substring(0, 1).equals("C")) {
            gradesViewHolder.cardView.setCardBackgroundColor(Color.parseColor("#bf360c"));
        } else if (data.grade.substring(0, 1).equals("D")) {
            gradesViewHolder.cardView.setCardBackgroundColor(Color.parseColor("#b71c1c"));
        } else if (data.grade.substring(0, 1).equals("E")) {
            gradesViewHolder.cardView.setCardBackgroundColor(Color.BLACK);
        }else{
            gradesViewHolder.cardView.setCardBackgroundColor(Color.parseColor("#795548"));
        }
    }

    @Override
    public int getItemCount() {
        try {
            return course.size();
        } catch (NullPointerException e) {
            return 0;
        }
    }

    @Override
    public GradesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grades, parent, false);
        return new GradesViewHolder(v);
    }

    public static class GradesViewHolder extends RecyclerView.ViewHolder {
        TextView courseName;
        TextView courseGrade;
        TextView assignment;
        TextView lab;
        TextView mid;
        TextView fin;

        CardView cardView;

        GradesViewHolder(View itemView) {
            super(itemView);
            courseName = (TextView) itemView.findViewById(R.id.course_name);
            courseGrade = (TextView) itemView.findViewById(R.id.course_grades);
            assignment = (TextView) itemView.findViewById(R.id.assignment);
            lab = (TextView) itemView.findViewById(R.id.laboratory);
            mid = (TextView) itemView.findViewById(R.id.mid);
            fin = (TextView) itemView.findViewById(R.id.fin);

            cardView = (CardView) itemView.findViewById(R.id.item_grades_cardview);
        }
    }
}
