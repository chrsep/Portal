package com.directdev.portal.ui.access;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.directdev.portal.R;
import com.directdev.portal.tools.helper.Portal;
import com.directdev.portal.ui.access.LoginAuthorization;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;


public class WebappActivity extends AppCompatActivity {
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webapp);
        Bundle bundle = getIntent().getExtras();

        Intent intent = new Intent(this, LoginAuthorization.class);
        intent.putExtra("text", "Accessing web-app");
        startActivity(intent);

        Toolbar toolbar = (Toolbar) findViewById(R.id.finance_webapp_toolbar);
        setSupportActionBar(toolbar);

        final WebView webView = (WebView) findViewById(R.id.finance_webapp_webview);

        webView.setWebViewClient(new WebViewClient() {
            Boolean alreadyReload = false;

            @Override
            public void onPageFinished(WebView view, String url) {
                webView.setVisibility(View.VISIBLE);
                if (!alreadyReload) {
                    webView.reload();
                    alreadyReload = true;
                }
                super.onPageFinished(view, url);
            }
        });

        Portal application = (Portal) getApplication();
        mTracker = application.getDefaultTracker();

        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(bundle.getString("url"));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(bundle.getString("title"));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName("MainActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
}
