package com.directdev.portal.ui.grades;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.directdev.portal.R;
import com.directdev.portal.tools.database.CourseDB;
import com.directdev.portal.tools.event.GradesResponseEvent;
import com.directdev.portal.tools.event.TermResponseEvent;
import com.directdev.portal.tools.fetcher.FetchScore;
import com.directdev.portal.tools.uihelper.MainViewPagerAdapter;
import com.directdev.portal.ui.WebappActivity;
import com.directdev.portal.ui.access.LoginAuthorization;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import de.greenrobot.event.EventBus;

public class GradesActivity extends AppCompatActivity {
    private FetchScore fetch;
    private CourseDB db;
    private SharedPreferences sPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grades);
        List<String> terms;
        db = new CourseDB(this);
        fetch = new FetchScore(this);
        sPref = getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE);
        EventBus.getDefault().register(this);
        terms = db.queryTerm();

        TabLayout tabLayout = (TabLayout) findViewById(R.id.grades_tab);
        ViewPager viewPager = (ViewPager) findViewById(R.id.grades_viewpager);
        setupViewPager(viewPager, terms);

        tabLayout.setupWithViewPager(viewPager);

        CollapsingToolbarLayout colToolbar = (CollapsingToolbarLayout) findViewById(R.id.grades_collapsing_toolbar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.grades_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        colToolbar.setTitle("Grades");

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("View grades")
                .putContentType("Activity")
                .putContentId("studentData"));
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
                fetch.requestTerm();
                return true;
            case R.id.action_grades_webapp:
                Intent intent = new Intent(this, WebappActivity.class);
                intent.putExtra("url","https://newbinusmaya.binus.ac.id/student/#/score/viewscore");
                intent.putExtra("title","View Grades");
                startActivity(intent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(TermResponseEvent event){
        List<String> terms;
        try{
            JSONArray jArray = new JSONArray(sPref.getString(getString(R.string.resource_terms),""));
            db.deleteData();
            db.addTerms(jArray);

            terms = db.queryTerm();
            for (int i = 0 ; i < terms.size(); i++){
                fetch.requestScores(terms.get(i));
            }
        }catch (JSONException e){
            Crashlytics.logException(e);
        }

    }
    public void onEvent(GradesResponseEvent event){
        try {
            JSONObject data= new JSONObject(sPref.getString(getString(R.string.resource_scores) + "_" + event.term, ""));
            db.addGrades(data,event.term);
        }catch (JSONException e){
            Crashlytics.logException(e);
        }
    }

    private void setupViewPager(ViewPager viewPager, List<String> terms) {
        MainViewPagerAdapter adapter = new MainViewPagerAdapter(getSupportFragmentManager());
        for(int i = 0; i<terms.size(); i++){
            adapter.addFrag(GradesByTermFragment.newInstance(terms.get(i)), terms.get(i));
        }
        viewPager.setAdapter(adapter);
    }
}
