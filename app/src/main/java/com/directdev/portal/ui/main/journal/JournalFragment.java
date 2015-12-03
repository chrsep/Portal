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

/**
 *  This is the part of the code that creates the journal tab,
 *
 *  Here we have a recyclerView of dates that display the black bar that displays the date, that recyclerView,
 *  also displays three other recyclerView, Finance, schedule & exam recyclerView that display the data of the
 *  events, for the layout see fragment_journal.xml
 */

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
        sPref = getActivity().getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE);
        edit = sPref.edit();

        //Analytics
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("View schedule")
                .putContentType("Activity")
                .putContentId("studentData"));
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_journal, container, false);

        //swipeLayout is the one that is responsible for the pull down to refresh action
        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.journal_refresh);
        swipeLayout.setOnRefreshListener(this);

        return view;
    }

    @Override
    public void onStart() {
        //Register the EventBus
        EventBus.getDefault().register(this);

        //Open database
        realm = Realm.getDefaultInstance();

        //We setup the date that are going to be displayed
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

        //This is to refresh every time Portal is opened
        if (sPref.getInt(getString(R.string.login_data_given_pref), 0) != 0 && !settingsPref.getBoolean(getString(R.string.setting_auto_refresh), false ) && !firstRequestSent) {
            swipeLayout.post(new Runnable() {
                @Override
                public void run() {swipeLayout.setRefreshing(true);}});
            swipeLayout.setEnabled(false);
            firstRequestSent = true;
            onRefresh();
        }

        //This is to show the circling refresh icon when refresh is happening (UpdateService is running)
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
        //Unregister EventBus and close the database
        EventBus.getDefault().unregister(this);
        realm.close();
        super.onStop();
    }

    //This is called when pull down to refresh is triggered by user
    @Override
    public void onRefresh() {
        swipeLayout.setEnabled(false);
        edit.putBoolean(getString(R.string.is_refreshing_journal_pref), true).apply();
        UpdateService.all(getActivity());
        FetchAccountData fetch = new FetchAccountData(getActivity());
        fetch.requestAllData();
    }

    //Method that is called when EventBus post the UpdateFinishEvent, see UpdateService.java
    public void onEventMainThread(UpdateFinishEvent event) {
        dateSetup();
        adapter = new JournalRecyclerAdapter(getActivity(),dates);
        recycler.swapAdapter(adapter, false);
        swipeLayout.setRefreshing(false);
        swipeLayout.setEnabled(true);
    }

    //Method that is called when EventBus post the UpdateFailedEvent, see tools.services.UpdateService.java to see
    //how the event get posted
    public void onEventAsync(UpdateFailedEvent event) {
        edit.putBoolean(getString(R.string.is_no_session), true).commit();
        Snackbar snackbar = Snackbar.make(view, "Refresh session to load new data", Snackbar.LENGTH_INDEFINITE)
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

    /**
     *  This method is used to generate the list of dates that have an event(Exam, finance, schedules),
     *  this date is used to display the Journal list, we are using a nested recyclerView to display
     *  to display that list.
     */
    private void dateSetup(){

        //Set the date collection to use
        dates = new LinkedList<>();

        //Get today's date
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
