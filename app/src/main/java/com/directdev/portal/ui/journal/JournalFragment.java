package com.directdev.portal.ui.journal;


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

import com.directdev.portal.R;
import com.directdev.portal.tools.database.JournalDB;
import com.directdev.portal.tools.datatype.ScheduleData;
import com.directdev.portal.tools.event.DatabaseUpdateEvent;
import com.directdev.portal.tools.event.FetchResponseEvent;
import com.directdev.portal.tools.fetcher.FetchAccountData;
import com.directdev.portal.tools.fetcher.FetchSchedule;
import com.directdev.portal.tools.fetcher.FetchScore;
import com.directdev.portal.ui.access.LoginAuthorization;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.solovyev.android.views.llm.LinearLayoutManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import de.greenrobot.event.EventBus;

public class JournalFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static List<ScheduleData> data = new LinkedList<>();
    private List<List<ScheduleData>> nestedData = new LinkedList<>();
    private View view;
    protected SwipeRefreshLayout swipeLayout;
    protected JournalRecyclerAdapter adapter;
    protected RecyclerView recycler;
    protected boolean firstRequestSent = false;
    private SharedPreferences sPref;
    private SharedPreferences.Editor edit;
    private String mParam1;
    private String mParam2;


    public static JournalFragment newInstance(String param1, String param2) {
        JournalFragment fragment = new JournalFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public JournalFragment() {}


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        EventBus.getDefault().register(this);
        sPref = getActivity().getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE);
        edit = sPref.edit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.fragment_journal, container, false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getBaseContext());
        recycler = (RecyclerView) view.findViewById(R.id.journal_recycler);
        recycler.setLayoutManager(layoutManager);

        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.journal_refresh);
        swipeLayout.setOnRefreshListener(this);
        recycler.setAdapter(adapter);

        if (sPref.getBoolean(getString(R.string.is_refreshing_journal_pref),false)){
            swipeLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeLayout.setRefreshing(true);
                }
            });
            swipeLayout.setEnabled(false);
        }
        return view;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        SharedPreferences settingsPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if(!firstRequestSent)
        {
            if(sPref.getString(getString(R.string.resource_schedule_new_pref),null)!=null)
            {
                onEventMainThread(new DatabaseUpdateEvent());
            }
            if(sPref.getInt(getString(R.string.login_data_given_pref),0)!=0&&!settingsPref.getBoolean(getString(R.string.setting_auto_refresh),false))
            {
                swipeLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        swipeLayout.setRefreshing(true);
                    }
                });
                swipeLayout.setEnabled(false);
                onRefresh();
            }
            firstRequestSent = true;
        }
    }


    @Override
    public void onRefresh()
    {
        swipeLayout.setEnabled(false);
        edit.putBoolean(getString(R.string.is_refreshing_journal_pref),true).apply();
        FetchSchedule getSchedule = new FetchSchedule(getActivity());
        getSchedule.requestAllData();

        FetchAccountData fetcher = new FetchAccountData(getActivity());
        fetcher.requestAllData();

        FetchScore fetchScore = new FetchScore(getActivity());
        fetchScore.requestTerm();

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEventAsync(FetchResponseEvent event){
        JournalDB db = new JournalDB(getActivity());
        try
        {
            if(!sPref.getString(getString(R.string.resource_schedule_new_pref),"").equals(sPref.getString(getString(R.string.resource_schedule_old_pref),""))||
                    !sPref.getString(getString(R.string.resource_finance_new_pref),"").equals(sPref.getString(getString(R.string.resource_finance_old_pref),"")))
            {
                JSONArray journalData = new JSONArray(sPref.getString(getString(R.string.resource_schedule_new_pref), ""));
                db.deleteData();
                db.addScheduleJson(journalData);
                edit.putString(getString(R.string.resource_schedule_old_pref), sPref.getString(getString(R.string.resource_schedule_new_pref), ""));
                edit.commit();

                JSONObject data = new JSONObject(sPref.getString(getString(R.string.resource_finance_new_pref), ""));
                db.addFinanceJson(data);
                edit.putString(getString(R.string.resource_finance_old_pref), sPref.getString(getString(R.string.resource_finance_new_pref), ""));
                edit.commit();
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm",Locale.US);
            edit.putString(getString(R.string.last_update_pref),dateFormat.format(new Date()))
                    .apply();
            EventBus.getDefault().post(new DatabaseUpdateEvent());
        }
        catch (JSONException e)
        {
            Snackbar snackbar = Snackbar.make(view, "Update session to fetch new data", Snackbar.LENGTH_LONG)
                    .setAction("UPDATE", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            firstRequestSent = false;
                            Intent intent = new Intent(getActivity(), LoginAuthorization.class);
                            startActivity(intent);
                        }
                    })
                    .setActionTextColor(Color.YELLOW);
            snackbar.show();

        }
        EventBus.getDefault().post(new DatabaseUpdateEvent());
    }

    public void onEventMainThread(DatabaseUpdateEvent event)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = dateFormat.format(new Date());
        JournalDB db = new JournalDB(getActivity());
        data = db.queryJournal("date_data >= \"" + today + "\"");
        if(data!=null)
        {
            nestedData = processData();
        }
        edit.putBoolean(getString(R.string.is_refreshing_journal_pref),false).apply();
        swipeLayout.setRefreshing(false);
        adapter = new JournalRecyclerAdapter(nestedData);
        recycler.swapAdapter(adapter, false);
        swipeLayout.setEnabled(true);
    }

    private List<List<ScheduleData>> processData(){
        List<List<ScheduleData>> nestedData = new LinkedList<>();
        List<ScheduleData> holder = new LinkedList<>();
        for (int i = 0; i < data.size(); i++)
        {
            if(i+1!=data.size())
            {
                if (data.get(i).date.equals(data.get(i + 1).date))
                {
                    holder.add(data.get(i));
                }
                else
                {
                    holder.add(data.get(i));
                    nestedData.add(holder);
                    holder = new LinkedList<>();
                }
            }
            else
            {
                holder.add(data.get(i));
                nestedData.add(holder);
            }
        }
        return nestedData;
    }
}
