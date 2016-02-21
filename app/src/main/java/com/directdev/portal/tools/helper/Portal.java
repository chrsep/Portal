package com.directdev.portal.tools.helper;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.directdev.portal.R;
import com.directdev.portal.tools.model.Absence;
import com.directdev.portal.tools.model.Course;
import com.directdev.portal.tools.model.Dates;
import com.directdev.portal.tools.model.Exam;
import com.directdev.portal.tools.model.Finance;
import com.directdev.portal.tools.model.Forum;
import com.directdev.portal.tools.model.Grades;
import com.directdev.portal.tools.model.GradesCourse;
import com.directdev.portal.tools.model.Outlines;
import com.directdev.portal.tools.model.People;
import com.directdev.portal.tools.model.Resource;
import com.directdev.portal.tools.model.Schedule;
import com.directdev.portal.tools.model.Terms;
import com.directdev.portal.tools.model.Textbook;
import com.directdev.portal.tools.model.assignment;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.io.File;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class Portal extends Application {
    private static final String TAG = "Portal";
    private static Portal instance;
    private Tracker mTracker;

    public static Portal getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        instance = this;

        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this).build();
        Realm.setDefaultConfiguration(realmConfiguration);
        //TODO: Delete below when going live

    }

    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
            mTracker.enableAdvertisingIdCollection(true);
            mTracker.enableAutoActivityTracking(true);
            mTracker.enableExceptionReporting(true);
        }
        return mTracker;
    }

    public void clearApplicationData() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.clear(Absence.class);
        realm.clear(Course.class);
        realm.clear(Dates.class);
        realm.clear(Exam.class);
        realm.clear(Finance.class);
        realm.clear(Forum.class);
        realm.clear(Grades.class);
        realm.clear(GradesCourse.class);
        realm.clear(Outlines.class);
        realm.clear(People.class);
        realm.clear(Resource.class);
        realm.clear(Schedule.class);
        realm.clear(Terms.class);
        realm.clear(Textbook.class);
        realm.clear(assignment.class);
        realm.commitTransaction();

        File dir = getFilesDir();
        File file = new File(dir, getString(R.string.resource_photo));
        boolean deleted = file.delete();
    }
}