package com.directdev.portal.tools.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Course extends RealmObject{
    private String CRSE_ID;
    private String COURSENAME;
    private String COURSEID;
    private String STRM;
    private String SSR_COMPONENT;
    @PrimaryKey
    private String CLASS_NBR;

    public String getCRSE_ID() {
        return CRSE_ID;
    }

    public void setCRSE_ID(String CRSE_ID) {
        this.CRSE_ID = CRSE_ID;
    }

    public String getCOURSENAME() {
        return COURSENAME;
    }

    public void setCOURSENAME(String COURSENAME) {
        this.COURSENAME = COURSENAME;
    }

    public String getCOURSEID() {
        return COURSEID;
    }

    public void setCOURSEID(String COURSEID) {
        this.COURSEID = COURSEID;
    }

    public String getSTRM() {
        return STRM;
    }

    public void setSTRM(String STRM) {
        this.STRM = STRM;
    }

    public String getSSR_COMPONENT() {
        return SSR_COMPONENT;
    }

    public void setSSR_COMPONENT(String SSR_COMPONENT) {
        this.SSR_COMPONENT = SSR_COMPONENT;
    }

    public String getCLASS_NBR() {
        return CLASS_NBR;
    }

    public void setCLASS_NBR(String CLASS_NBR) {
        this.CLASS_NBR = CLASS_NBR;
    }
}
