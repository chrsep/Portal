package com.directdev.portal.ui.main.journal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.directdev.portal.R;
import com.directdev.portal.tools.fetcher.FetchAccountData;
import com.directdev.portal.tools.model.Dates;
import com.directdev.portal.tools.event.UpdateFailedEvent;
import com.directdev.portal.tools.event.UpdateFinishEvent;
import com.directdev.portal.tools.services.UpdateService;
import com.directdev.portal.ui.access.LoginAuthorization;

import org.solovyev.android.views.llm.LinearLayoutManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmResults;

public class JournalFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    protected SwipeRefreshLayout swipeLayout;
    protected JournalRecyclerAdapter adapter;
    protected RecyclerView recycler;
    protected boolean firstRequestSent = false;
    private View view;
    private SharedPreferences sPref;
    private SharedPreferences.Editor edit;
    private List<Date> dates;
    private Realm realm;

    public JournalFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sPref = getActivity().getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE);
        edit = sPref.edit();

        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("View schedule")
                .putContentType("Activity")
                .putContentId("studentData"));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_journal, container, false);

        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.journal_refresh);
        swipeLayout.setOnRefreshListener(this);

        return view;
    }

    @Override
    public void onStart() {
        EventBus.getDefault().register(this);
        realm = Realm.getDefaultInstance();
        dateSetup();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getBaseContext());
        adapter = new JournalRecyclerAdapter(getActivity(),dates);
        recycler = (RecyclerView) view.findViewById(R.id.journal_recycler);
        recycler.setLayoutManager(layoutManager);
        recycler.setAdapter(adapter);
        super.onStart();
    }

    @Override
    public void onResume() {
        SharedPreferences settingsPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (sPref.getInt(getString(R.string.login_data_given_pref), 0) != 0 && !settingsPref.getBoolean(getString(R.string.setting_auto_refresh), false ) && !firstRequestSent) {
            swipeLayout.post(new Runnable() {
                @Override
                public void run() {swipeLayout.setRefreshing(true);}});
            swipeLayout.setEnabled(false);
            firstRequestSent = true;
            onRefresh();
        }
        if (UpdateService.isActive){
            swipeLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeLayout.setRefreshing(true);
                }
            });
            swipeLayout.setEnabled(false);
        }
        super.onResume();
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        realm.close();
        super.onStop();
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    @Override
    public void onRefresh() {
        swipeLayout.setEnabled(false);
        edit.putBoolean(getString(R.string.is_refreshing_journal_pref), true).apply();
        UpdateService.all(getActivity());
        FetchAccountData fetch = new FetchAccountData(getActivity());
        fetch.requestAllData();
    }

    public void onEventMainThread(UpdateFinishEvent event) {
        dateSetup();
        adapter = new JournalRecyclerAdapter(getActivity(),dates);
        recycler.swapAdapter(adapter, false);
        swipeLayout.setRefreshing(false);
        swipeLayout.setEnabled(true);
    }

    public void onEventAsync(UpdateFailedEvent event) {
        edit.putBoolean(getString(R.string.is_no_session), true).commit();
        Snackbar snackbar = Snackbar.make(view, "Refresh session to load new data", Snackbar.LENGTH_LONG)
                .setAction("REFRESH", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        firstRequestSent = false;
                        Intent intent = new Intent(getActivity(), LoginAuthorization.class);
                        intent.putExtra("text", "Refreshing session");
                        startActivity(intent);
                    }
                })
                .setActionTextColor(Color.YELLOW);
        snackbar.show();
    }

    private void dateSetup(){
        dates = new LinkedList<>();
        Date today = new Date();
        Date tested = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());
        RealmResults<Dates> dateData = realm.where(Dates.class).findAll();
        try {
            if (!dateData.isEmpty()) {
                dates.add(sdf.parse(sdf.format(tested)));
            }
        }catch (ParseException e){}
        for (int i = 0 ; i < dateData.size() ; i++){
            try{tested = sdf.parse(dateData.get(i).getDatePK());}catch (ParseException e){}
            if(tested.after(today)) dates.add(tested);
        }
        Collections.sort(dates);
    }
}
