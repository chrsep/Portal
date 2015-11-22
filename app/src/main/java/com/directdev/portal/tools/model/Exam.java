package com.directdev.portal.tools.model;

import java.util.Date;

import io.realm.RealmObject;

public class Exam extends RealmObject {
    private String DESCR;
    private String KDMTK;
    private Date ExamDate;
    private String ExamStartTime;
    private String Duration;
    private String ROOM;
    private String ChairNumber;
    private String COURSE_TITLE_LONG;

    public String getCOURSE_TITLE_LONG() {
        return COURSE_TITLE_LONG;
    }

    public void setCOURSE_TITLE_LONG(String COURSE_TITLE_LONG) {
        this.COURSE_TITLE_LONG = COURSE_TITLE_LONG;
    }

    public String getDESCR() {
        return DESCR;
    }

    public void setDESCR(String DESCR) {
        this.DESCR = DESCR;
    }

    public String getKDMTK() {
        return KDMTK;
    }

    public void setKDMTK(String KDMTK) {
        this.KDMTK = KDMTK;
    }

    public Date getExamDate() {
        return ExamDate;
    }

    public void setExamDate(Date examDate) {
        ExamDate = examDate;
    }

    public String getExamStartTime() {
        return ExamStartTime;
    }

    public void setExamStartTime(String examStartTime) {
        ExamStartTime = examStartTime;
    }

    public String getDuration() {
        return Duration;
    }

    public void setDuration(String duration) {
        Duration = duration;
    }

    public String getROOM() {
        return ROOM;
    }

    public void setROOM(String ROOM) {
        this.ROOM = ROOM;
    }

    public String getChairNumber() {
        return ChairNumber;
    }

    public void setChairNumber(String chairNumber) {
        ChairNumber = chairNumber;
    }
}
