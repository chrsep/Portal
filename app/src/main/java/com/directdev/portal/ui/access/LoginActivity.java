package com.directdev.portal.ui.access;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.directdev.portal.R;
import com.directdev.portal.tools.fetcher.FetchScore;
import com.directdev.portal.tools.uihelper.MyApplication;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private SharedPreferences sharedPref;
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button button = (Button) findViewById(R.id.loginbutton);
        button.setOnClickListener(this);

        EditText password = (EditText) findViewById(R.id.passwordLogin);
        password.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    onClick(v);
                }
                return false;
            }
        });
        sharedPref = getSharedPreferences(getString(R.string.shared_preferences), MODE_PRIVATE);
        MyApplication application = (MyApplication) getApplication();
        mTracker = application.getDefaultTracker();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sharedPref.getInt(getResources().getString(R.string.login_condition_pref), 0) == 1) {
            FetchScore fetchScore = new FetchScore(this);
            fetchScore.requestTerm();
            finish();
        } else if (sharedPref.getInt(getResources().getString(R.string.login_data_given_pref), 0) == 1) {
            Toast toast = Toast.makeText(LoginActivity.this, "Wrong username or password", Toast.LENGTH_SHORT);
            toast.show();
        }
        mTracker.setScreenName("FinanceActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onClick(View v) {
        EditText email = (EditText) findViewById(R.id.emailLogin);
        EditText password = (EditText) findViewById(R.id.passwordLogin);
        String emailExtra = email.getText().toString();
        emailExtra += "@binus.ac.id";
        String passwordExtra = password.getText().toString();

        if (emailExtra.equals("@binus.ac.id")) {
            Toast toast = Toast.makeText(LoginActivity.this, "Your username is important", Toast.LENGTH_SHORT);
            toast.show();
            return;
        } else if (passwordExtra.equals("")) {
            Toast toast = Toast.makeText(LoginActivity.this, "You password is needed for login", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        SharedPreferences.Editor dtEdit = sharedPref.edit();

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            dtEdit.putString(getResources().getString(R.string.login_username_pref), emailExtra)
                    .putString(getResources().getString(R.string.login_password_pref), passwordExtra)
                    .putInt(getResources().getString(R.string.login_data_given_pref), 1)
                    .commit();

            Intent intent = new Intent(this, LoginAuthorization.class);
            intent.putExtra("text", "Signing you in");
            startActivity(intent);
            if (sharedPref.getInt(getString(R.string.login_condition_pref), 0) != 0) {
                finish();
            }
        } else {
            Toast toast = Toast.makeText(LoginActivity.this, "You are currently offline, please find a connection", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
