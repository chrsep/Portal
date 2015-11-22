package com.directdev.portal.tools.model;

import io.realm.RealmObject;

public class Resource extends RealmObject {
    private String courseOutlineTopicID;
    private int mediaTypeId;
    private String mediaType;
    private String Title;
    private String description;
    private String pathid;
    private String path;
    private String location;
    private String filename;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCourseOutlineTopicID() {
        return courseOutlineTopicID;
    }

    public void setCourseOutlineTopicID(String courseOutlineTopicID) {
        this.courseOutlineTopicID = courseOutlineTopicID;
    }

    public int getMediaTypeId() {
        return mediaTypeId;
    }

    public void setMediaTypeId(int mediaTypeId) {
        this.mediaTypeId = mediaTypeId;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getPathid() {
        return pathid;
    }

    public void setPathid(String pathid) {
        this.pathid = pathid;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
