package com.directdev.portal.ui.main.account.grades;

import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.directdev.portal.R;
import com.directdev.portal.tools.model.Grades;
import com.directdev.portal.tools.model.GradesCourse;

import io.realm.Realm;
import io.realm.RealmResults;

public class GradesRecyclerAdapter extends RecyclerView.Adapter {
    private RealmResults<GradesCourse> courses;
    private Realm realm;

    public GradesRecyclerAdapter(RealmResults<GradesCourse> courses) {
        this.courses = courses;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {

        realm = Realm.getDefaultInstance();
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int i) {
        GradesCourse course = courses.get(i);

        RealmResults<Grades> data = realm.where(Grades.class).equalTo("kodemtk", course.getKodemtk()).findAll();
        GradesViewHolder gradesViewHolder = (GradesViewHolder) holder;
        gradesViewHolder.courseName.setText(data.get(0).getCourse());
        try{
            gradesViewHolder.courseGrade.setText(data.get(0).getCourse_grade());
        }catch (ArrayIndexOutOfBoundsException e){
            gradesViewHolder.courseGrade.setText("-");
        }

        for (Grades grade: data) {
            switch (grade.getLam()) {
                case "MID EXAM":
                    gradesViewHolder.mid.setVisibility(View.VISIBLE);
                    if(grade.getScore().equals("N/A")){
                        gradesViewHolder.mid.setText("Mid Exam    \t: -");
                    }else{
                        gradesViewHolder.mid.setText("Mid Exam    \t: " + grade.getScore());
                    }
                    break;
                case "FINAL EXAM":
                    if(grade.getScore().equals("N/A")){
                        gradesViewHolder.fin.setVisibility(View.VISIBLE);
                        gradesViewHolder.fin.setText("Final Exam  \t: -");
                    }else{
                        gradesViewHolder.fin.setVisibility(View.VISIBLE);
                        gradesViewHolder.fin.setText("Final Exam  \t: " + grade.getScore());
                    }
                    break;
                case "LABORATORY":
                    if(grade.getScore().equals("N/A")){
                        gradesViewHolder.lab.setVisibility(View.VISIBLE);
                        gradesViewHolder.lab.setText("Laboratory  \t: -");
                    }else{
                        gradesViewHolder.lab.setVisibility(View.VISIBLE);
                        gradesViewHolder.lab.setText("Laboratory  \t: " + grade.getScore());
                    }
                    break;
                case "ASSIGNMENT":
                    if(grade.getScore().equals("N/A")){
                        gradesViewHolder.assignment.setVisibility(View.VISIBLE);
                        gradesViewHolder.assignment.setText("Assignment\t: -");
                    }else{
                        gradesViewHolder.assignment.setVisibility(View.VISIBLE);
                        gradesViewHolder.assignment.setText("Assignment\t: " + grade.getScore());
                    }
                    break;
            }
        }

        try {
            if (data.get(0).getCourse_grade().startsWith("A")) {
                gradesViewHolder.cardView.setCardBackgroundColor(Color.parseColor("#1565c0"));
            } else if (data.get(0).getCourse_grade().startsWith("B")) {
                gradesViewHolder.cardView.setCardBackgroundColor(Color.parseColor("#1b5e20"));
            } else if (data.get(0).getCourse_grade().startsWith("C")) {
                gradesViewHolder.cardView.setCardBackgroundColor(Color.parseColor("#bf360c"));
            } else if (data.get(0).getCourse_grade().startsWith("D")) {
                gradesViewHolder.cardView.setCardBackgroundColor(Color.parseColor("#b71c1c"));
            } else if (data.get(0).getCourse_grade().startsWith("E")) {
                gradesViewHolder.cardView.setCardBackgroundColor(Color.BLACK);
            } else {
                gradesViewHolder.cardView.setCardBackgroundColor(Color.parseColor("#795548"));
            }
        }catch (ArrayIndexOutOfBoundsException e){gradesViewHolder.cardView.setCardBackgroundColor(Color.parseColor("#795548"));}

    }

    @Override
    public int getItemCount() {
        try {
            return courses.size();
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

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        realm.close();
        super.onDetachedFromRecyclerView(recyclerView);
    }
}
