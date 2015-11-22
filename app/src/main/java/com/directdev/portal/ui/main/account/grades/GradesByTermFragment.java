package com.directdev.portal.ui.main.account.grades;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.directdev.portal.R;
import com.directdev.portal.tools.model.Course;
import com.directdev.portal.tools.model.Grades;
import com.directdev.portal.tools.model.GradesCourse;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class GradesByTermFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";

    private String mParam1;
    private Realm realm;
    private RealmResults<GradesCourse> courses;

    public GradesByTermFragment() {
    }

    public static GradesByTermFragment newInstance(String param1) {
        GradesByTermFragment fragment = new GradesByTermFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        realm = Realm.getDefaultInstance();
        courses = realm.where(GradesCourse.class).equalTo("STRM",mParam1).findAll();
        View view = inflater.inflate(R.layout.fragment_grades_by_term, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.grades_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        GradesRecyclerAdapter adapter = new GradesRecyclerAdapter(courses);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(adapter);
        try {
            RealmResults<Grades> grades = realm.where(Grades.class).equalTo("kodemtk", courses.get(0).getKodemtk()).findAll();
        }catch (ArrayIndexOutOfBoundsException e){
            CardView cardView = (CardView) view.findViewById(R.id.grades_no_data_cardview);
            cardView.setVisibility(View.VISIBLE);
        }
        return view;
    }

    @Override
    public void onStop() {
        realm.close();
        super.onStop();
    }
}
