package com.example.eventplanner.model;

public class Comment {
    private String grade;
    private String description;
    private String occurenceDate;
    private String status;
    private Report report;
    private String userODId;
    private String companyId;

    public Comment() {
    }

    public Comment(String grade, String description, String occurenceDate, String status, Report report, String userODId, String companyId) {
        this.grade = grade;
        this.description = description;
        this.occurenceDate = occurenceDate;
        this.status = status;
        this.report = report;
        this.userODId = userODId;
        this.companyId = companyId;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOccurenceDate() {
        return occurenceDate;
    }

    public void setOccurenceDate(String occurenceDate) {
        this.occurenceDate = occurenceDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public String getUserODId() {
        return userODId;
    }

    public void setUserODId(String userODId) {
        this.userODId = userODId;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }
}
