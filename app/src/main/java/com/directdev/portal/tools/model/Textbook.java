package com.directdev.portal.tools.model;


import io.realm.RealmObject;

public class Textbook extends RealmObject{
    private String Type;
    private String Title;
    private String ISBN;
    private String year;
    private String Edition;
    private String Author;
    private String City;
    private String Publisher;
    private String Bibli;
    private String Price;
    private String hyperlink;
    private String AdditionalInfo;

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getBibli() {
        return Bibli;
    }

    public void setBibli(String bibli) {
        Bibli = bibli;
    }

    public String getPrice() {
        return Price;
    }

    public void setPrice(String price) {
        Price = price;
    }

    public String getAdditionalInfo() {
        return AdditionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        AdditionalInfo = additionalInfo;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getISBN() {
        return ISBN;
    }

    public void setISBN(String ISBN) {
        this.ISBN = ISBN;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getEdition() {
        return Edition;
    }

    public void setEdition(String edition) {
        Edition = edition;
    }

    public String getAuthor() {
        return Author;
    }

    public void setAuthor(String author) {
        Author = author;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        City = city;
    }

    public String getPublisher() {
        return Publisher;
    }

    public void setPublisher(String publisher) {
        Publisher = publisher;
    }

    public String getHyperlink() {
        return hyperlink;
    }

    public void setHyperlink(String hyperlink) {
        this.hyperlink = hyperlink;
    }
}
