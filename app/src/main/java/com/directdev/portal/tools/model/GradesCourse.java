package com.directdev.portal.tools.model;


import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class GradesCourse extends RealmObject{
    private String STRM;
    @PrimaryKey
    private String kodemtk;

    public String getSTRM() {
        return STRM;
    }

    public void setSTRM(String STRM) {
        this.STRM = STRM;
    }

    public String getKodemtk() {
        return kodemtk;
    }

    public void setKodemtk(String kodemtk) {
        this.kodemtk = kodemtk;
    }
}
