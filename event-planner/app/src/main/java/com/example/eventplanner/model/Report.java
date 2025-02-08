package com.example.eventplanner.model;

public class Report {
    private String occurenceDate;
    private String description;

    public Report() {
    }

    public Report(String occurenceDate, String description) {
        this.occurenceDate = occurenceDate;
        this.description = description;
    }

    public String getoOcurenceDate() {
        return occurenceDate;
    }

    public void setOccurenceDate(String occurenceDate) {
        this.occurenceDate = occurenceDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
