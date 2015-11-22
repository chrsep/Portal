package com.directdev.portal.tools.model;

import io.realm.RealmObject;

public class People extends RealmObject{
    private String BinusianID;
    private String Binusian;
    private String Photo;
    private String Role;

    public String getBinusianID() {
        return BinusianID;
    }

    public void setBinusianID(String binusianID) {
        BinusianID = binusianID;
    }

    public String getBinusian() {
        return Binusian;
    }

    public void setBinusian(String binusian) {
        Binusian = binusian;
    }

    public String getPhoto() {
        return Photo;
    }

    public void setPhoto(String photo) {
        Photo = photo;
    }

    public String getRole() {
        return Role;
    }

    public void setRole(String role) {
        Role = role;
    }
}
