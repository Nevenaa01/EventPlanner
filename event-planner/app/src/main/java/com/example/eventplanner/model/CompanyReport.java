package com.example.eventplanner.model;

public class CompanyReport {
    private Long id;
    private String pupvId;
    private String reasonOfReport;
    private String userId;

    public CompanyReport() {
    }

    public CompanyReport(Long id, String pupvId, String reasonOfReport, String userId) {
        this.id = id;
        this.pupvId = pupvId;
        this.reasonOfReport = reasonOfReport;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPupvId() {
        return pupvId;
    }

    public void setPupvId(String pupvId) {
        this.pupvId = pupvId;
    }

    public String getReasonOfReport() {
        return reasonOfReport;
    }

    public void setReasonOfReport(String reasonOfReport) {
        this.reasonOfReport = reasonOfReport;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
