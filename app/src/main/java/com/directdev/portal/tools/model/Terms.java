package com.directdev.portal.tools.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Terms extends RealmObject {
    @PrimaryKey
    private String value;
    private String field;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
