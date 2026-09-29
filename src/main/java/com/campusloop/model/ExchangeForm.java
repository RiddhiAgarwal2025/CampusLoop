package com.campusloop.model;

/** Raw values of the exchange-details form, kept as text for redisplay after an error. */
public class ExchangeForm {

    private String location = "";
    private String date = "";
    private String time = "";
    private String note = "";

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location == null ? "" : location;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date == null ? "" : date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time == null ? "" : time;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note == null ? "" : note;
    }
}
