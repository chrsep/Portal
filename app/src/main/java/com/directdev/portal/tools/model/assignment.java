package com.directdev.portal.tools.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

import io.realm.RealmObject;


public class assignment extends RealmObject {
    private String AssignmentFrom;
    private String Title;
    @SerializedName(value = "Date")
    private Date date;
    private Date deadlineDuration;
    private String assignmentPathLocation;
    private String deadlineTime;
    private int StudentAssignmentID;

    public String getAssignmentFrom() {
        return AssignmentFrom;
    }

    public void setAssignmentFrom(String assignmentFrom) {
        AssignmentFrom = assignmentFrom;
    }

    public String getDeadlineTime() {
        return deadlineTime;
    }

    public void setDeadlineTime(String deadlineTime) {
        this.deadlineTime = deadlineTime;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDeadlineDuration() {
        return deadlineDuration;
    }

    public void setDeadlineDuration(Date deadlineDuration) {
        this.deadlineDuration = deadlineDuration;
    }

    public String getAssignmentPathLocation() {
        return assignmentPathLocation;
    }

    public void setAssignmentPathLocation(String assignmentPathLocation) {
        this.assignmentPathLocation = assignmentPathLocation;
    }

    public int getStudentAssignmentID() {
        return StudentAssignmentID;
    }

    public void setStudentAssignmentID(int studentAssignmentID) {
        StudentAssignmentID = studentAssignmentID;
    }
}
