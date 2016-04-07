package com.directdev.portal.tools.model;

import java.util.Date;

import io.realm.RealmObject;

public class Finance extends RealmObject {
    private Date DUE_DT;
    private Date ITEM_EFFECTIVE_DT;
    private String ITEM_TERM;
    private String DESCR;
    private String ITEM_TYPE_CD;
    private int ITEM_AMT;
    private String ITEM_NBR;

    public String getITEM_NBR() {
        return ITEM_NBR;
    }

    public void setITEM_NBR(String ITEM_NBR) {
        this.ITEM_NBR = ITEM_NBR;
    }

    public Date getDUE_DT() {
        return DUE_DT;
    }

    public void setDUE_DT(Date DUE_DT) {
        this.DUE_DT = DUE_DT;
    }

    public Date getITEM_EFFECTIVE_DT() {
        return ITEM_EFFECTIVE_DT;
    }

    public void setITEM_EFFECTIVE_DT(Date ITEM_EFFECTIVE_DT) {
        this.ITEM_EFFECTIVE_DT = ITEM_EFFECTIVE_DT;
    }

    public String getITEM_TERM() {
        return ITEM_TERM;
    }

    public void setITEM_TERM(String ITEM_TERM) {
        this.ITEM_TERM = ITEM_TERM;
    }

    public String getDESCR() {
        return DESCR;
    }

    public void setDESCR(String DESCR) {
        this.DESCR = DESCR;
    }

    public String getITEM_TYPE_CD() {
        return ITEM_TYPE_CD;
    }

    public void setITEM_TYPE_CD(String ITEM_TYPE_CD) {
        this.ITEM_TYPE_CD = ITEM_TYPE_CD;
    }

    public int getITEM_AMT() {
        return ITEM_AMT;
    }

    public void setITEM_AMT(int ITEM_AMT) {
        this.ITEM_AMT = ITEM_AMT;
    }
}
