package com.directdev.portal.ui.main.journal;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
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
import com.directdev.portal.tools.event.NotSignedInEvent;
import com.directdev.portal.tools.event.UpdateFailedEvent;
import com.directdev.portal.tools.event.UpdateFinishEvent;
import com.directdev.portal.tools.helper.Pref;
import com.directdev.portal.tools.model.Dates;
import com.directdev.portal.tools.services.UpdateService;
import com.directdev.portal.ui.access.LogoutAuthorization;

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
    private Snackbar snackbar;
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
            snackbar = Snackbar.make(view, "Your data is outdated, last updated "+diffInHours+" hours ago", Snackbar.LENGTH_INDEFINITE);
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
        if (isNetworkAvailable()) {
            isWebLoading = true;
            webView.loadUrl("https://newbinusmaya.binus.ac.id/login.php");
        }else {
            swipeLayout.setRefreshing(false);
        }
    }

    //Method that is called when EventBus post the UpdateFinishEvent, see UpdateService.java
    public void onEventMainThread(UpdateFinishEvent event) {
        dateSetup();
        adapter = new JournalRecyclerAdapter(getActivity(),dates);
        recycler.swapAdapter(adapter, false);
        swipeLayout.setRefreshing(false);
        swipeLayout.setEnabled(true);
        snackbar.dismiss();
    }

    //Method that is called when EventBus post the UpdateFailedEvent, see tools.services.UpdateService.java to see
    //how the event get posted
    public void onEventMainThread(UpdateFailedEvent event) {
        Toast.makeText(getActivity(), event.name + " data not available to load, try to refresh again later", Toast.LENGTH_SHORT).show();
        snackbar.dismiss();
    }

    public void onEventMainThread(CantConnectEvent event) {
        Toast.makeText(getActivity(), "Failed to connect, try again later", Toast.LENGTH_LONG).show();
    }

    public void onEventMainThread(NotSignedInEvent event){
        Snackbar.make(view, "Your password changed", Snackbar.LENGTH_INDEFINITE)
                .setAction("Relogin", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        getActivity().stopService(new Intent(getActivity(), UpdateService.class));
                                        Intent intent = new Intent(getActivity(), LogoutAuthorization.class);
                                        startActivity(intent);
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        break;
                                }
                            }
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener)
                                .show();
                    }
                })
                .setActionTextColor(Color.YELLOW)
                .show();
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
        private int loginFailedCounter = 0;

        @Override
        public void onPageFinished(WebView webView, final String url) {
            super.onPageFinished(webView, url);
            webView.loadUrl("javascript:(function () {document.getElementById('ctl00_ContentPlaceHolder1_UsernameTextBoxBMNew').value='" + USERNAME + "'})()");
            webView.loadUrl("javascript:(function () {document.getElementById('ctl00_ContentPlaceHolder1_PasswordTextBoxBMNew').value='" + PASSWORD + "'})()");
            webView.loadUrl("javascript:(function () {document.getElementById('ctl00_ContentPlaceHolder1_SubmitButtonBMNew').click()})()");
            loginFailedCounter += 1;
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            try {
                if (url.equals("https://newbinusmaya.binus.ac.id/newStudent/")) {
                    loginFailedCounter = 0;
                    webView.loadUrl("http://example.com/");
                    view.stopLoading();
                    isWebLoading = false;
                    String cookie = android.webkit.CookieManager.getInstance().getCookie("https://newbinusmaya.binus.ac.id/student/#/index/dashboard");
                    Pref.save(ctx, getString(R.string.login_cookie_pref), cookie);
                    UpdateService.all(getActivity());
                }else if(loginFailedCounter > 6){
                    loginFailedCounter = 0;
                    webView.loadUrl("http://example.com/");
                    view.stopLoading();
                    isWebLoading = false;
                    onEventMainThread(new NotSignedInEvent());
                }
            }catch (IllegalStateException e){
                Crashlytics.log(e.getMessage());
            }
            return super.shouldOverrideUrlLoading(view, url);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
