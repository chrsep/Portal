package com.directdev.portal.ui.access;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.directdev.portal.R;


public class LoginAuthorization extends AppCompatActivity {
    private static final String TAG = "LoginAuthorization";
    private static String USERNAME = "false";
    private static String PASSWORD = "b!false";
    protected SharedPreferences sharedPreferences;
    protected SharedPreferences.Editor edit;
    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_authorization);

        bundle = getIntent().getExtras();
        TextView textView = (TextView) findViewById(R.id.login_authenticator_text);
        textView.setText(bundle.getString("text"));

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if (!isConnected) {
            finish();
        }
        sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences), MODE_PRIVATE);
        edit = sharedPreferences.edit();
        edit.putInt("LoginAttempt", 0);
        edit.commit();
        USERNAME = sharedPreferences.getString(getString(R.string.login_username_pref), "");
        PASSWORD = sharedPreferences.getString(getString(R.string.login_password_pref), "");
        final WebView webView = (WebView) findViewById(R.id.loginWebView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        LoginWebView browser = new LoginWebView();
        webView.setWebViewClient(browser);
        webView.loadUrl("https://newbinusmaya.binus.ac.id/login.php");
    }

    private class LoginWebView extends WebViewClient {
        @Override
        public void onPageFinished(WebView webView, String url) {
            try {
                if (sharedPreferences.getInt("LoginAttempt", 0) == 4 && sharedPreferences.getInt(getString(R.string.login_condition_pref), 0) == 0) {
                    edit.putInt(getString(R.string.login_condition_pref), 0).commit();
                    finish();
                }
                if (sharedPreferences.getInt("LoginAttempt", 0) < 4) {
                    int tries = sharedPreferences.getInt("LoginAttempt", 0);
                    tries = tries + 1;
                    webView.loadUrl("javascript:(function () {document.getElementById('ctl00_ContentPlaceHolder1_UsernameTextBoxBMNew').value='" + USERNAME + "'})()");
                    webView.loadUrl("javascript:(function () {document.getElementById('ctl00_ContentPlaceHolder1_PasswordTextBoxBMNew').value='" + PASSWORD + "'})()");
                    webView.loadUrl("javascript:(function () {document.getElementById('ctl00_ContentPlaceHolder1_SubmitButtonBMNew').click()})()");
                    edit.putInt("LoginAttempt", tries).commit();
                }
            } catch (NullPointerException e) {
                //We detected crashes involving NullPointerException coming from this method
                //This will catch the crash and send back crash data to us.
                Crashlytics.logException(e);
                finish();
            }
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d(TAG, "onPageStarted: "+url);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.equals("https://newbinusmaya.binus.ac.id/newStudent/")) {
                view.stopLoading();
                String cookie = android.webkit.CookieManager.getInstance().getCookie("https://newbinusmaya.binus.ac.id/student/#/index/dashboard");
                edit.putString(getString(R.string.login_cookie_pref), cookie)
                        .putInt(getString(R.string.login_condition_pref), 1)
                        .putBoolean(getString(R.string.is_no_session), false)
                        .commit();
                finish();
            }
            return super.shouldOverrideUrlLoading(view, url);
        }
    }
}
