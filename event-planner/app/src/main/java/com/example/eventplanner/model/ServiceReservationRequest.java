package com.example.eventplanner.model;

public class ServiceReservationRequest {

    private String id;
    private String startHours;
    private String endHours;
    private String occurenceDate;
    private String dateScheduleId;
    private String workerId;
    private String day;
    private String type;
    private String status;
    private Service service;
    private String userId;
    public ServiceReservationRequest() {
    }

    public ServiceReservationRequest( String startHours, String endHours, String occurenceDate, String dateScheduleId, String workerId, String day, String type, String status, Service service, String userId) {

        this.startHours = startHours;
        this.endHours = endHours;
        this.occurenceDate = occurenceDate;
        this.dateScheduleId = dateScheduleId;
        this.workerId = workerId;
        this.day = day;
        this.type = type;
        this.status = status;
        this.service = service;
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStartHours() {
        return startHours;
    }

    public void setStartHours(String startHours) {
        this.startHours = startHours;
    }

    public String getEndHours() {
        return endHours;
    }

    public void setEndHours(String endHours) {
        this.endHours = endHours;
    }

    public String getOccurenceDate() {
        return occurenceDate;
    }

    public void setOccurenceDate(String occurenceDate) {
        this.occurenceDate = occurenceDate;
    }

    public String getDateScheduleId() {
        return dateScheduleId;
    }

    public void setDateScheduleId(String dateScheduleId) {
        this.dateScheduleId = dateScheduleId;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }
}
