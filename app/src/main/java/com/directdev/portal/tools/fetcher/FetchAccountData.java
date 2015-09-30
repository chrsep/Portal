package com.directdev.portal.tools.fetcher;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.directdev.portal.R;
import com.directdev.portal.tools.event.AccountResponseEvent;
import com.directdev.portal.tools.event.GpaResponseEvent;
import com.directdev.portal.tools.event.PhotoResponseEvent;
import com.directdev.portal.tools.event.ThereIsNewPhotoEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;


public class FetchAccountData {
    private Context context;
    private SharedPreferences sPref;
    private SharedPreferences.Editor edit;
    private String FILENAME;

    public FetchAccountData(Context context) {
        this.context = context;
        sPref = context.getSharedPreferences(
                context.getString(R.string.shared_preferences), Context.MODE_PRIVATE
        );
        edit = sPref.edit();
        FILENAME = context.getString(R.string.resource_photo);
    }

    public void requestAllData() {
        if (sPref.getInt(context.getString(R.string.photo_downloaded), 0) == 0) {
            requestData(context.getString(R.string.request_photo), "getPhoto");
        }
        requestData(context.getString(R.string.request_student_info), "geAccountData");
        requestData(context.getString(R.string.request_dashboard), "getGpa");
    }

    public void requestPhoto() {
        requestData(context.getString(R.string.request_photo), "getPhoto");
    }

    private void requestData(String url, final String dataType) {
        final SharedPreferences.Editor editor = sPref.edit();
        RequestQueue queue = Volley.newRequestQueue(context);
        CustomStringRequest request = new CustomStringRequest(url, sPref.getString(context.getString(R.string.login_cookie_pref), ""),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            switch (dataType) {
                                case "getPhoto":
                                    try {
                                        JSONObject data = new JSONObject(response);
                                        String toDecode = data.getString("photo");
                                        byte[] photo = Base64.decode(toDecode, Base64.DEFAULT);
                                        FileOutputStream fileOutputStream = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
                                        fileOutputStream.write(photo);
                                        fileOutputStream.close();
                                        edit.putInt(context.getString(R.string.photo_downloaded), 1).commit();
                                        EventBus.getDefault().post(new PhotoResponseEvent());
                                    } catch (IOException e) {
                                        Log.d("photo", "failed");
                                    }
                                    break;
                                case "geAccountData": {
                                    JSONObject data1 = new JSONObject(response);
                                    JSONObject data2 = data1.getJSONObject("Student");
                                    String name = data2.getString("Name");
                                    String major = data2.getString("Major");
                                    editor.putString(context.getString(R.string.resource_account_name), name)
                                            .putString(context.getString(R.string.resource_major), major)
                                            .commit();
                                    JSONObject photo = data2.getJSONObject("Photo");
                                    String photodata = photo.getString("photo");
                                    if (sPref.getString(context.getString(R.string.resource_small_photo), "").equals(photodata)) {
                                        edit.putString(context.getString(R.string.resource_small_photo), photodata)
                                                .commit();
                                        EventBus.getDefault().post(new ThereIsNewPhotoEvent());
                                    }
                                    EventBus.getDefault().post(new AccountResponseEvent());
                                    break;
                                }
                                case "getGpa": {
                                    JSONObject data = new JSONObject(response);
                                    JSONObject data5 = data.getJSONObject("WidgetData");
                                    JSONArray data2 = data5.getJSONArray("GPA");
                                    JSONObject data3 = data2.getJSONObject(0);
                                    String data4 = "0";
                                    try {
                                        data4 = data3.getString("GPA").substring(0, 3);
                                    } catch (StringIndexOutOfBoundsException e) {
                                        data4 = "N/A";
                                    } finally {
                                        editor.putString(context.getString(R.string.resource_gpa), data4)
                                                .commit();
                                        EventBus.getDefault().post(new GpaResponseEvent());
                                        break;
                                    }
                                }
                            }
                        } catch (JSONException e) {
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Data recieve error", error.toString());
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
            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put("Cookie", cookie);
            return headers;

        }
    }
}
