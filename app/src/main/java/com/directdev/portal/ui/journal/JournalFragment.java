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

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.directdev.portal.R;
import com.directdev.portal.tools.database.JournalDB;
import com.directdev.portal.tools.datatype.ScheduleData;
import com.directdev.portal.tools.event.DatabaseUpdateEvent;
import com.directdev.portal.tools.event.FetchResponseEvent;
import com.directdev.portal.tools.fetcher.FetchAccountData;
import com.directdev.portal.tools.fetcher.FetchSchedule;
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

public class JournalFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static List<ScheduleData> data = new LinkedList<>();
    protected SwipeRefreshLayout swipeLayout;
    protected JournalRecyclerAdapter adapter;
    protected RecyclerView recycler;
    protected boolean firstRequestSent = false;
    private List<List<ScheduleData>> nestedData = new LinkedList<>();
    private View view;
    private SharedPreferences sPref;
    private SharedPreferences.Editor edit;

    public JournalFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
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
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getBaseContext());
        recycler = (RecyclerView) view.findViewById(R.id.journal_recycler);
        recycler.setLayoutManager(layoutManager);

        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.journal_refresh);
        swipeLayout.setOnRefreshListener(this);

        recycler.setAdapter(adapter);

        if (sPref.getBoolean(getString(R.string.is_refreshing_journal_pref), false)) {
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
    public void onResume() {
        super.onResume();
        SharedPreferences settingsPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (!firstRequestSent) {
            if (sPref.getString(getString(R.string.resource_schedule_new_pref), null) != null) {
                onEventMainThread(new DatabaseUpdateEvent());
            }
            if (sPref.getInt(getString(R.string.login_data_given_pref), 0) != 0 && !settingsPref.getBoolean(getString(R.string.setting_auto_refresh), false)) {
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
    public void onRefresh() {
        swipeLayout.setEnabled(false);
        edit.putBoolean(getString(R.string.is_refreshing_journal_pref), true).apply();
        FetchSchedule getSchedule = new FetchSchedule(getActivity());
        getSchedule.requestAllData();

        FetchAccountData fetcher = new FetchAccountData(getActivity());
        fetcher.requestAllData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEventAsync(FetchResponseEvent event) {
        JournalDB db = new JournalDB(getActivity());
        try {
            JSONArray scheduleData = new JSONArray(sPref.getString(getString(R.string.resource_schedule_new_pref), ""));
            JSONObject financeData = new JSONObject(sPref.getString(getString(R.string.resource_finance_new_pref), ""));

            db.deleteData();
            db.addScheduleJson(scheduleData);
            db.addFinanceJson(financeData);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm", Locale.US);
            edit.putString(getString(R.string.last_update_pref), dateFormat.format(new Date()))
                    .apply();
        } catch (JSONException e) {
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
        EventBus.getDefault().post(new DatabaseUpdateEvent());
    }

    public void onEventMainThread(DatabaseUpdateEvent event) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = dateFormat.format(new Date());
        JournalDB db = new JournalDB(getActivity());
        data = db.queryJournal("date_data >= \"" + today + "\"");
        if (data != null) {
            nestedData = processData();
        }
        edit.putBoolean(getString(R.string.is_refreshing_journal_pref), false).apply();
        swipeLayout.setRefreshing(false);
        adapter = new JournalRecyclerAdapter(nestedData);
        recycler.swapAdapter(adapter, false);
        swipeLayout.setEnabled(true);
    }

    private List<List<ScheduleData>> processData() {
        List<List<ScheduleData>> nestedData = new LinkedList<>();
        List<ScheduleData> holder = new LinkedList<>();
        for (int i = 0; i < data.size(); i++) {
            if (i + 1 != data.size()) {
                if (data.get(i).date.equals(data.get(i + 1).date)) {
                    holder.add(data.get(i));
                } else {
                    holder.add(data.get(i));
                    nestedData.add(holder);
                    holder = new LinkedList<>();
                }
            } else {
                holder.add(data.get(i));
                nestedData.add(holder);
            }
        }
        return nestedData;
    }
}
