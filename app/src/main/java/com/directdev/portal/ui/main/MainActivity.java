package com.directdev.portal.ui.main;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.directdev.portal.R;
import com.directdev.portal.tools.helper.MainViewPagerAdapter;
import com.directdev.portal.tools.helper.Portal;
import com.directdev.portal.tools.helper.Pref;
import com.directdev.portal.ui.access.LoginActivity;
import com.directdev.portal.ui.access.WebappActivity;
import com.directdev.portal.ui.main.account.AccountFragment;
import com.directdev.portal.ui.main.journal.JournalFragment;
import com.directdev.portal.ui.main.resource.ResourceFragment;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import de.greenrobot.event.EventBus;

/*
 * This is where the portal starts
 *
 * When the application is opened, MainActivity will be launched
 */

public class MainActivity extends AppCompatActivity {
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Sets up the toolbar (The one on top with buttons and "Portal" text)
        setTitle("Portal");
        Toolbar toolbar = (Toolbar) findViewById(R.id.tabanim_toolbar);
        setSupportActionBar(toolbar);

        //Sets up the tabs
        ViewPager viewPager = (ViewPager) findViewById(R.id.tabanim_viewpager);
        setupViewPager(viewPager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabanim_tabs);
        tabLayout.setupWithViewPager(viewPager);

        // Analytics to track crashes, user growth and so on
        Portal application = (Portal) getApplication();
        mTracker = application.getDefaultTracker();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkLogin();
        mTracker.setScreenName("MainActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    //This creates the menu (Notification news, and open in web_app) using the menu_main layout
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // This is triggered when a button is pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toolbar_notification:
                Toast notif = Toast.makeText(this, "Notification is still being built", Toast.LENGTH_SHORT);
                notif.show();
                return true;
            case R.id.toolbar_news:
                Toast news = Toast.makeText(this, "News is still being built", Toast.LENGTH_SHORT);
                news.show();
                return true;
            case R.id.action_schedule_webapp:
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
                if (isConnected) {
                    Intent intent = new Intent(this, WebappActivity.class);
                    intent.putExtra("url", "https://newbinusmaya.binus.ac.id/student/index.html#/learning/lecturing");
                    intent.putExtra("title", "Schedules");
                    startActivity(intent);
                } else {
                    Toast connection = Toast.makeText(this, "You are offline, please find a connection", Toast.LENGTH_SHORT);
                    connection.show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // This is used to setup the tabs
    private void setupViewPager(ViewPager viewPager) {
        MainViewPagerAdapter adapter = new MainViewPagerAdapter(getSupportFragmentManager());
        new JournalFragment();
        new ResourceFragment();
        new AccountFragment();
        adapter.addFrag(new JournalFragment(), "JOURNAL");
        adapter.addFrag(new ResourceFragment(), "INFO");
        adapter.addFrag(new AccountFragment(), "ACCOUNT");
        viewPager.setAdapter(adapter);
    }

    // This is used to check if the user has logged in or not, if not than the login page will be shown
    public void checkLogin() {
        if (Pref.read(this, R.string.login_data_given_pref, 0) != 1 || Pref.read(this, R.string.login_condition_pref, 0) != 1) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }
}

