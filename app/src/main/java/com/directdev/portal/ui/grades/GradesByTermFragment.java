package com.directdev.portal.ui.grades;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.directdev.portal.R;
import com.directdev.portal.tools.database.CourseDB;

import java.util.List;

public class GradesByTermFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";

    private List<String> term;
    private String mParam1;
    private CourseDB db;

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
        db = new CourseDB(getActivity());
        term = db.queryCourse(mParam1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grades_by_term, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.grades_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        GradesRecyclerAdapter adapter = new GradesRecyclerAdapter(term, getActivity());
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(adapter);

        if (term.size() == 0) {
            CardView cardView = (CardView) view.findViewById(R.id.grades_no_data_cardview);
            cardView.setVisibility(View.VISIBLE);
        }
        return view;
    }


}
