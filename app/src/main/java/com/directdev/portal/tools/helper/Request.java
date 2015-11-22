package com.directdev.portal.tools.helper;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.directdev.portal.R;

import java.util.HashMap;
import java.util.Map;


public class Request extends StringRequest {
    private String cookie;

    private Request(Context ctx, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(Method.GET, url, listener, errorListener);
        this.cookie = Pref.read(ctx, R.string.login_cookie_pref, "");
    }

    public static Request create(Context ctx, String url, Response.Listener<String> listener, Response.ErrorListener error) {
        Request request =  new Request(ctx, url, listener,error);
        request.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        return request;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Cookie", cookie);
        return headers;

    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return super.getParams();
    }
}
