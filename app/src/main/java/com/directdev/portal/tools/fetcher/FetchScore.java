package com.directdev.portal.tools.fetcher;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.directdev.portal.R;
import com.directdev.portal.tools.event.GradesResponseEvent;
import com.directdev.portal.tools.event.TermResponseEvent;

import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;


public class FetchScore {
    private Context context;
    private SharedPreferences sPref;

    public FetchScore(Context context) {
        this.context = context;
        sPref = context.getSharedPreferences(
                context.getString(R.string.shared_preferences), Context.MODE_PRIVATE
        );
    }

    public void requestTerm() {
        final SharedPreferences.Editor editor = sPref.edit();
        RequestQueue queue = Volley.newRequestQueue(context);
        CustomStringRequest request = new CustomStringRequest(context.getString(R.string.request_terms), sPref.getString(context.getString(R.string.login_cookie_pref), ""),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        editor.putString(context.getString(R.string.resource_terms), response).commit();
                        EventBus.getDefault().post(new TermResponseEvent());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //EventBus.getDefault().post(new FetchResponseEvent());
                    }
                });
        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(
                socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        );
        request.setRetryPolicy(policy);
        queue.add(request);
    }

    public void requestScores(final String term) {
        final SharedPreferences.Editor editor = sPref.edit();
        RequestQueue queue = Volley.newRequestQueue(context);
        CustomStringRequest request = new CustomStringRequest(context.getString(R.string.request_scores) + term, sPref.getString(context.getString(R.string.login_cookie_pref), ""),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        editor.putString(context.getString(R.string.resource_scores) + "_" + term, response).commit();
                        EventBus.getDefault().post(new GradesResponseEvent(term));
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //EventBus.getDefault().post(new FetchResponseEvent());
                    }
                });
        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(
                socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        );
        request.setRetryPolicy(policy);
        queue.add(request);
    }

    private class CustomStringRequest extends StringRequest {
        private String cookie;

        public CustomStringRequest(String url, String cookies, Response.Listener<String> listener, Response.ErrorListener errorListener) {
            super(Method.GET, url, listener, errorListener);
            this.cookie = cookies;
        }

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            HashMap<String, String> headers = new HashMap<>();
            headers.put("Cookie", cookie);
            return headers;

        }
    }
}
