package com.directdev.portal.tools.fetcher;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.directdev.portal.R;
import com.directdev.portal.tools.event.DatabaseUpdateEvent;
import com.directdev.portal.tools.event.FetchResponseEvent;

import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;

public class FetchSchedule {
    protected int order;
    SharedPreferences sPref;
    private Context context;

    public FetchSchedule(Context context) {
        this.context = context;
        sPref = context.getSharedPreferences(
                context.getString(R.string.shared_preferences), Context.MODE_PRIVATE
        );
    }

    public void requestAllData() {
        order = 0;
        requestData(context.getString(R.string.request_schedule), "schedule");
        requestData(context.getString(R.string.request_finance), "finance");
    }

    private void requestData(String url, final String dataType) {
        final SharedPreferences.Editor editor = sPref.edit();
        RequestQueue queue = Volley.newRequestQueue(context);
        CustomStringRequest request = new CustomStringRequest(url, sPref.getString(context.getString(R.string.login_cookie_pref), ""),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        switch (dataType) {
                            case "finance":
                                editor.putString(context.getString(R.string.resource_finance_new_pref), response);
                                editor.apply();
                                order++;
                                break;
                            case "schedule":
                                editor.putString(context.getString(R.string.resource_schedule_new_pref), response);
                                editor.apply();
                                order++;
                                break;
                        }
                        if (order == 2) {
                            EventBus.getDefault().post(new FetchResponseEvent());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast connectionFailed = Toast.makeText(context, "Cannot connect to server", Toast.LENGTH_SHORT);
                        connectionFailed.show();
                        EventBus.getDefault().post(new DatabaseUpdateEvent());
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

