package com.directdev.portal.tools.helper;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.GsonBuilder;

import io.realm.RealmObject;

public class GsonHelper {
    public static com.google.gson.Gson create(){
        return new GsonBuilder().setDateFormat("yyyy-MM-dd")
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return f.getDeclaringClass().equals(RealmObject.class);
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                }).create();
    }
}
