package com.directdev.portal.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.WebView;

import com.directdev.portal.R;


public class FinanceWebappActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finance_webapp);

        Toolbar toolbar = (Toolbar) findViewById(R.id.finance_webapp_toolbar);
        setSupportActionBar(toolbar);

        CookieManager cookie = CookieManager.getInstance();
        cookie.setCookie("https://newbinusmaya.binus.ac.id/student/#/financial/financialStatus", getString(R.string.login_cookie_pref));

        WebView webView = (WebView) findViewById(R.id.finance_webapp_webview);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.loadUrl("https://newbinusmaya.binus.ac.id/student/#/financial/financialStatus");

        if (getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Financial Status");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_finance_webapp, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
