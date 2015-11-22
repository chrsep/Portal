package com.directdev.portal.ui.main.account;


import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.directdev.portal.R;
import com.directdev.portal.tools.helper.Portal;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;


public class SettingsActivity extends Activity {
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingFragment()).commit();

        Portal application = (Portal) getApplication();
        mTracker = application.getDefaultTracker();
    }

    public static class SettingFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.pref_data);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName("MainActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
}
