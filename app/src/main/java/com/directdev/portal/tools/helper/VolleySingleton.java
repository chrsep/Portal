package com.directdev.portal.tools.helper;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;


public class VolleySingleton {
    private static VolleySingleton instance;
    private RequestQueue queue;

    private VolleySingleton(Context ctx){queue = Volley.newRequestQueue(ctx);}

    public static VolleySingleton getInstance(Context ctx){
        if (instance == null){instance = new VolleySingleton(ctx);}
        return instance;
    }

    public RequestQueue getQueue(){return queue;}
}
