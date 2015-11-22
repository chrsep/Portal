package com.directdev.portal.tools.model;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


public class Dates extends RealmObject {
    @PrimaryKey
    @SerializedName(value = "Date", alternate = {"ExamDate", "ITEM_EFFECTIVE_DT"})
    private String DatePK;

    public String getDatePK() {
        return DatePK;
    }

    public void setDatePK(String datePK) {
        DatePK = datePK;
    }

}
