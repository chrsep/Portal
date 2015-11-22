package com.directdev.portal.tools.model;


import io.realm.RealmObject;

public class Forum extends RealmObject{
    private String ID;
    private String ForumThreadTitle;
    private String replies;
    private String lastreply;
    private String CreatorID;
    private String creator;
    private String CrseID;
    private String ClassID;
    private String TopicID;
    private String TeamID;
    private String Year;
    private String ExternalID;

    public String getCreatorID() {
        return CreatorID;
    }

    public void setCreatorID(String creatorID) {
        CreatorID = creatorID;
    }

    public String getCrseID() {
        return CrseID;
    }

    public void setCrseID(String crseID) {
        CrseID = crseID;
    }

    public String getClassID() {
        return ClassID;
    }

    public void setClassID(String classID) {
        ClassID = classID;
    }

    public String getTopicID() {
        return TopicID;
    }

    public void setTopicID(String topicID) {
        TopicID = topicID;
    }

    public String getTeamID() {
        return TeamID;
    }

    public void setTeamID(String teamID) {
        TeamID = teamID;
    }

    public String getYear() {
        return Year;
    }

    public void setYear(String year) {
        Year = year;
    }

    public String getExternalID() {
        return ExternalID;
    }

    public void setExternalID(String externalID) {
        ExternalID = externalID;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getForumThreadTitle() {
        return ForumThreadTitle;
    }

    public void setForumThreadTitle(String forumThreadTitle) {
        ForumThreadTitle = forumThreadTitle;
    }

    public String getReplies() {
        return replies;
    }

    public void setReplies(String replies) {
        this.replies = replies;
    }

    public String getLastreply() {
        return lastreply;
    }

    public void setLastreply(String lastreply) {
        this.lastreply = lastreply;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }
}
