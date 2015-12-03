package com.directdev.portal.ui.main.account.grades;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.directdev.portal.R;
import com.directdev.portal.tools.model.Terms;
import com.directdev.portal.tools.helper.MainViewPagerAdapter;
import com.directdev.portal.tools.helper.Portal;
import com.directdev.portal.ui.access.WebappActivity;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import io.realm.Realm;
import io.realm.RealmResults;

public class GradesActivity extends AppCompatActivity {
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grades);
        realm = Realm.getDefaultInstance();
        TabLayout tabLayout = (TabLayout) findViewById(R.id.grades_tab);
        ViewPager viewPager = (ViewPager) findViewById(R.id.grades_viewpager);
        RealmResults<Terms> terms = realm.where(Terms.class).findAll();
        setupViewPager(viewPager, terms);

        tabLayout.setupWithViewPager(viewPager);

        CollapsingToolbarLayout colToolbar = (CollapsingToolbarLayout) findViewById(R.id.grades_collapsing_toolbar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.grades_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        colToolbar.setTitle("Grades");

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("View grades")
                .putContentType("Activity")
                .putContentId("studentData"));

        Portal application = (Portal) getApplication();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_grades, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                Toast toast = Toast.makeText(GradesActivity.this, "Grades data will be updated together with schedules", Toast.LENGTH_LONG);
                toast.show();
                return true;
            case R.id.action_grades_webapp:
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null &&
                        activeNetwork.isConnectedOrConnecting();

                if (isConnected) {
                    Intent intent = new Intent(this, WebappActivity.class);
                    intent.putExtra("url", "https://newbinusmaya.binus.ac.id/student/#/score/viewscore");
                    intent.putExtra("title", "View Grades");
                    startActivity(intent);
                } else {
                    Toast connection = Toast.makeText(this, "You are offline, please find a connection", Toast.LENGTH_SHORT);
                    connection.show();
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        realm.close();
        super.onDestroy();
    }

    private void setupViewPager(ViewPager viewPager, RealmResults<Terms> terms) {
        MainViewPagerAdapter adapter = new MainViewPagerAdapter(getSupportFragmentManager());
        for (int i = 0; i < terms.size(); i++) {
            adapter.addFrag(GradesByTermFragment.newInstance(terms.get(i).getValue()), terms.get(i).getValue());
        }
        viewPager.setAdapter(adapter);
    }
}
