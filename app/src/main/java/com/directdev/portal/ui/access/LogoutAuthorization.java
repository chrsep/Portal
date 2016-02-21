package com.directdev.portal.ui.access;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.directdev.portal.R;
import com.directdev.portal.tools.helper.Portal;
import com.directdev.portal.ui.main.MainActivity;


public class LogoutAuthorization extends AppCompatActivity {
    protected SharedPreferences sharedPreferences;
    protected SharedPreferences.Editor edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if (!isConnected) {
            finish();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout_authorization);
        sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences), MODE_PRIVATE);
        edit = sharedPreferences.edit();
        edit.putInt("LoginAttempt", 0);
        edit.commit();
        final WebView webView = (WebView) findViewById(R.id.logoutWebView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        LoginWebView browser = new LoginWebView();
        webView.setWebViewClient(browser);
        webView.loadUrl("https://newbinusmaya.binus.ac.id/services/ci/index.php/login/logout");
    }

    private class LoginWebView extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (url.equals("https://newbinusmaya.binus.ac.id/newDefault/login.html")) {
                Portal.getInstance().clearApplicationData();
                edit.clear().commit();

                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return super.shouldOverrideUrlLoading(view, url);
        }
    }
}
