package com.directdev.portal.ui.main.journal;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.directdev.portal.R;
import com.directdev.portal.tools.event.CantConnectEvent;
import com.directdev.portal.tools.event.UpdateFailedEvent;
import com.directdev.portal.tools.event.UpdateFinishEvent;
import com.directdev.portal.tools.helper.Pref;
import com.directdev.portal.tools.model.Dates;
import com.directdev.portal.tools.services.UpdateService;

import org.solovyev.android.views.llm.LinearLayoutManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

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
    private static final String TAG = "JournalFragment";
    protected SwipeRefreshLayout swipeLayout;
    protected JournalRecyclerAdapter adapter;
    protected RecyclerView recycler;
    private View view;
    private List<Date> dates;
    private Realm realm;
    private WebView webView;
    protected boolean isWebLoading = false;

    public JournalFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_journal, container, false);
        //swipeLayout is the one that is responsible for the pull down to refresh action
        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.journal_refresh);
        swipeLayout.setOnRefreshListener(this);

        webView = (WebView) view.findViewById(R.id.refresh_webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        LoginWebView browser = new LoginWebView();
        webView.setWebViewClient(browser);

        if(savedInstanceState != null){
            isWebLoading = savedInstanceState.getBoolean("isWebLoading");
            webView.restoreState(savedInstanceState);
        }
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

        if(dates.isEmpty()){
            RelativeLayout background = (RelativeLayout) view.findViewById(R.id.parent_layout_journal);
            background.setBackgroundColor(ContextCompat.getColor(getActivity(),R.color.primary_light));

        }else {
            TextView backgroundTextOne = (TextView) view.findViewById(R.id.journal_background_one);
            TextView backgroundTextTwo = (TextView) view.findViewById(R.id.journal_background_two);
            backgroundTextOne.setVisibility(View.GONE);
            backgroundTextTwo.setVisibility(View.GONE);
        }
        recycler = (RecyclerView) view.findViewById(R.id.journal_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getBaseContext());
        adapter = new JournalRecyclerAdapter(getActivity(),dates);
        recycler.setLayoutManager(layoutManager);
        recycler.setAdapter(adapter);
        super.onStart();
    }

    @Override
    public void onResume() {
        //This is to show the circling refresh icon when refresh is happening (UpdateService is running)
        Log.d(TAG, "onResume: called" + isWebLoading);
        if (UpdateService.isActive || isWebLoading){
            Log.d(TAG, "onResume: if one called");
            swipeLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeLayout.setRefreshing(true);
                }
            });
            swipeLayout.setEnabled(false);
        }else {
            swipeLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeLayout.setRefreshing(false);
                }
            });
            swipeLayout.setEnabled(true);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.getDefault());
        Date startDate = new Date();
        try{
            startDate = sdf.parse(Pref.read(getActivity(), getActivity().getString(R.string.last_update_pref), " "));
        }catch (ParseException e){}

        Date endDate   = new Date();
        long duration  = endDate.getTime() - startDate.getTime();
        long diffInHours = TimeUnit.MILLISECONDS.toHours(duration);

        if(diffInHours > 24){
            Snackbar snackbar = Snackbar.make(view, "Your data is outdated, last updated "+diffInHours+" hours ago", Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("isWebLoading", isWebLoading);
        webView.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    //This is called when pull down to refresh is triggered by user
    @Override
    public void onRefresh() {
        isWebLoading = true;
        webView.loadUrl("https://newbinusmaya.binus.ac.id/login.php");
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
    public void onEventMainThread(UpdateFailedEvent event) {
        Toast.makeText(getActivity(), "Some data fails to load, try to refresh again later", Toast.LENGTH_LONG).show();
    }

    public void onEventMainThread(CantConnectEvent event) {
        Toast.makeText(getActivity(), "Failed to connect, try again later", Toast.LENGTH_LONG).show();
    }

    /**
     *  This method is used to generate the list of dates that have an event(Exam, finance, schedules),
     *  this date is used to display the Journal list, we are using a nested recyclerView to display
     *  to display that list.
     */
    private void dateSetup(){
        //Set the date collection to use
        dates = new LinkedList<>();
        String tempHolder;
        boolean noToday = true;
        //Get today's date
        Date today = new Date();
        Date tested;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());
        RealmResults<Dates> dateData = realm.where(Dates.class).findAll();

        tempHolder = sdf.format(today);
        try{
            today =sdf.parse(tempHolder);
            for (int i = 0 ; i < dateData.size() ; i++){
                tested = sdf.parse(dateData.get(i).getDatePK());
                if(tested.after(today)||tested.equals(today)) {
                    dates.add(tested);
                }
            }
            for (Date date:dates) {
                if(date.equals(today)){
                    noToday = false;
                }
            }
            if (noToday && !dates.isEmpty()){
                dates.add(sdf.parse(sdf.format(today)));
            }
        }catch (ParseException e){}
        Collections.sort(dates);
    }

    private class LoginWebView extends WebViewClient {
        private String USERNAME = Pref.read(getActivity(),getString(R.string.login_username_pref), "");
        private String PASSWORD = Pref.read(getActivity(),getString(R.string.login_password_pref), "");
        private Context ctx = getActivity();

        @Override
        public void onPageFinished(WebView webView, final String url) {
            try {
                if (Pref.read(getActivity(),"LoginAttempt", 0) < 4) {
                    int tries = Pref.read(ctx,"LoginAttempt", 0);
                    tries = tries + 1;
                    webView.loadUrl("javascript:(function () {document.getElementById('ctl00_ContentPlaceHolder1_UsernameTextBoxBMNew').value='" + USERNAME + "'})()");
                    webView.loadUrl("javascript:(function () {document.getElementById('ctl00_ContentPlaceHolder1_PasswordTextBoxBMNew').value='" + PASSWORD + "'})()");
                    webView.loadUrl("javascript:(function () {document.getElementById('ctl00_ContentPlaceHolder1_SubmitButtonBMNew').click()})()");
                    Pref.save(ctx, "LoginAttempt", tries);
                }
            } catch (NullPointerException e) {

            }
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d(TAG, "onPageStarted: "+ url);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            try {
                if (url.equals("https://newbinusmaya.binus.ac.id/newStudent/")) {
                    view.stopLoading();
                    isWebLoading = false;
                    String cookie = android.webkit.CookieManager.getInstance().getCookie("https://newbinusmaya.binus.ac.id/student/#/index/dashboard");
                    Pref.save(ctx, getString(R.string.login_cookie_pref), cookie);
                    UpdateService.all(getActivity());
                }
            }catch (IllegalStateException e){
                Crashlytics.log(e.getMessage());
            }
            return super.shouldOverrideUrlLoading(view, url);
        }
    }
}
