package com.directdev.portal.tools.model;


import io.realm.RealmObject;

public class Grades extends RealmObject{
    private String kodemtk;
    private String course;
    private String lam;
    private String scu;
    private String weight;
    private String score;
    private String grade;
    private String course_grade;
    private String strm;

    public String getStrm() {
        return strm;
    }

    public void setStrm(String strm) {
        this.strm = strm;
    }

    public String getKodemtk() {
        return kodemtk;
    }

    public void setKodemtk(String kodemtk) {
        this.kodemtk = kodemtk;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getLam() {
        return lam;
    }

    public void setLam(String lam) {
        this.lam = lam;
    }

    public String getScu() {
        return scu;
    }

    public void setScu(String scu) {
        this.scu = scu;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getCourse_grade() {
        return course_grade;
    }

    public void setCourse_grade(String course_grade) {
        this.course_grade = course_grade;
    }
}
