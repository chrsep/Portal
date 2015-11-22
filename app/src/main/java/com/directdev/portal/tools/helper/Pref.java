package com.directdev.portal.tools.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.StringRes;

import com.directdev.portal.R;

public class Pref {
    public static void save(Context ctx, String[] key, String[] value){
        SharedPreferences sp = ctx.getSharedPreferences(ctx.getString(R.string.shared_preferences), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        for(int i = 0; i < key.length ; i++){
            editor.putString(key[i], value[i]);
        }
        editor.commit();
    }

    public static void save(Context ctx, String key, int value){
        SharedPreferences sp = ctx.getSharedPreferences(ctx.getString(R.string.shared_preferences), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static void save(Context ctx, String key, String value){
        SharedPreferences sp = ctx.getSharedPreferences(ctx.getString(R.string.shared_preferences), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String read(Context ctx, @StringRes int id, String defaultValue){
        SharedPreferences sp = ctx.getSharedPreferences(ctx.getString(R.string.shared_preferences), Context.MODE_PRIVATE);
        return sp.getString(ctx.getString(id), defaultValue);
    }

    public static int read(Context ctx, @StringRes int id, int defaultValue){
        SharedPreferences sp = ctx.getSharedPreferences(ctx.getString(R.string.shared_preferences), Context.MODE_PRIVATE);
        return sp.getInt(ctx.getString(id), defaultValue);
    }

    public static boolean read(Context ctx, @StringRes int id, boolean defaultValue){
        SharedPreferences sp = ctx.getSharedPreferences(ctx.getString(R.string.shared_preferences), Context.MODE_PRIVATE);
        return sp.getBoolean(ctx.getString(id), defaultValue);
    }

    public static String read(Context ctx, String key, String defaultValue){
        SharedPreferences sp = ctx.getSharedPreferences(ctx.getString(R.string.shared_preferences), Context.MODE_PRIVATE);
        return sp.getString(key, defaultValue);
    }
}
