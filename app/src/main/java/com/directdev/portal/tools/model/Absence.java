package com.directdev.portal.tools.model;

import io.realm.RealmObject;

public class Absence extends RealmObject {
    private String Degree;
    private String Period;
    private String CourseID;
    private String Course;
    private String CourseName;
    private String Classes;
    private String Com;
    private String TotalSession;
    private String MaxAbsence;
    private String SessionDone;
    private String TotalAbsence;

    public String getDegree() {
        return Degree;
    }

    public void setDegree(String degree) {
        Degree = degree;
    }

    public String getPeriod() {
        return Period;
    }

    public void setPeriod(String period) {
        Period = period;
    }

    public String getCourseID() {
        return CourseID;
    }

    public void setCourseID(String courseID) {
        CourseID = courseID;
    }

    public String getCourse() {
        return Course;
    }

    public void setCourse(String course) {
        Course = course;
    }

    public String getCourseName() {
        return CourseName;
    }

    public void setCourseName(String courseName) {
        CourseName = courseName;
    }

    public String getClasses() {
        return Classes;
    }

    public void setClasses(String classes) {
        Classes = classes;
    }

    public String getCom() {
        return Com;
    }

    public void setCom(String com) {
        Com = com;
    }

    public String getTotalSession() {
        return TotalSession;
    }

    public void setTotalSession(String totalSession) {
        TotalSession = totalSession;
    }

    public String getMaxAbsence() {
        return MaxAbsence;
    }

    public void setMaxAbsence(String maxAbsence) {
        MaxAbsence = maxAbsence;
    }

    public String getSessionDone() {
        return SessionDone;
    }

    public void setSessionDone(String sessionDone) {
        SessionDone = sessionDone;
    }

    public String getTotalAbsence() {
        return TotalAbsence;
    }

    public void setTotalAbsence(String totalAbsence) {
        TotalAbsence = totalAbsence;
    }
}
