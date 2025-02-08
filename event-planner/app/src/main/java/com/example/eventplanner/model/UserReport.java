package com.example.eventplanner.model;

public class UserReport {
    private Long id;
    private String reportedId;
    private String reporterId;
    private String reason;
    private Long dateOfReport;
    private Status status;

    public enum Status {
        REPORTED,
        APPROVED,
        DENIED
    }

    public UserReport() {
    }

    public UserReport(Long id, String reportedId, String reporterId, String reason, Long dateOfReport, Status status) {
        this.id = id;
        this.reportedId = reportedId;
        this.reporterId = reporterId;
        this.reason = reason;
        this.dateOfReport = dateOfReport;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReportedId() {
        return reportedId;
    }

    public void setReportedId(String reportedId) {
        this.reportedId = reportedId;
    }

    public String getReporterId() {
        return reporterId;
    }

    public void setReporterId(String reporterId) {
        this.reporterId = reporterId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Long getDateOfReport() {
        return dateOfReport;
    }

    public void setDateOfReport(Long dateOfReport) {
        this.dateOfReport = dateOfReport;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
