package com.directdev.portal.tools.model;

import io.realm.RealmObject;

public class Outlines extends RealmObject{
    private String courseOutlineLearningOutcomeID;
    private String courseOutlineLearningOutcome;

    public String getCourseOutlineLearningOutcome() {
        return courseOutlineLearningOutcome;
    }

    public void setCourseOutlineLearningOutcome(String courseOutlineLearningOutcome) {
        this.courseOutlineLearningOutcome = courseOutlineLearningOutcome;
    }

    public String getCourseOutlineLearningOutcomeID() {

        return courseOutlineLearningOutcomeID;
    }

    public void setCourseOutlineLearningOutcomeID(String courseOutlineLearningOutcomeID) {
        this.courseOutlineLearningOutcomeID = courseOutlineLearningOutcomeID;
    }
}
