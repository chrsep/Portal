package com.directdev.portal.tools.datatype;

public class FinanceData {
    public String description;
    public Integer amount;
    public String date;
    public String type;

    public FinanceData(String description, Integer amount, String date, String type) {
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.type = type;
    }
}
