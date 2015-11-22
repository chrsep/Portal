package com.directdev.portal.tools.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

import io.realm.RealmObject;


public class Schedule extends RealmObject {

    private String CourseID;
    private String CourseName;
    private Date Date;
    private int Session;
    private String Room;
    private String Mode;
    @SerializedName(value = "Class")
    private String Classcode;
    private String Type;
    private String Week;

    public String getShift() {
        return Shift;
    }

    public void setShift(String shift) {
        Shift = shift;
    }

    private String Shift;

    public String getRoom() {
        return Room;
    }

    public void setRoom(String room) {
        Room = room;
    }

    public String getMode() {
        return Mode;
    }

    public void setMode(String mode) {
        Mode = mode;
    }

    public String getClasscode() {
        return Classcode;
    }

    public void setClasscode(String classcode) {
        Classcode = classcode;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getWeek() {
        return Week;
    }

    public void setWeek(String week) {
        Week = week;
    }

    public int getSession() {
        return Session;
    }

    public void setSession(int session) {
        Session = session;
    }

    public String getCourseID() {
        return CourseID;
    }

    public void setCourseID(String courseID) {
        CourseID = courseID;
    }

    public String getCourseName() {
        return CourseName;
    }

    public void setCourseName(String courseName) {
        CourseName = courseName;
    }

    public Date getDate() {
        return Date;
    }

    public void setDate(Date date) {
        Date = date;
    }
}
