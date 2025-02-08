package com.example.eventplanner.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EventPUPZ {

    private Long id;
    private String startHours;
    private String endHours;
    private String occurenceDate;
    private Long dateScheduleId;
    private Long workerId;
    private String day;
    private String type;

    public EventPUPZ() {
    }

    public EventPUPZ(Long id, String startHours, String endHours, String occurenceDate, Long dateScheduleId, String day, String type, Long workerId) {
        this.id = id;
        this.startHours = startHours;
        this.endHours = endHours;
        this.occurenceDate = occurenceDate;
        this.dateScheduleId = dateScheduleId;
        this.day = day;
        this.type = type;
        this.workerId = workerId;
    }

    public EventPUPZ(Long id, ServiceReservationRequest serviceReservationRequest){
        this.id = id;
        this.startHours = serviceReservationRequest.getStartHours();
        this.endHours = serviceReservationRequest.getEndHours();
        this.occurenceDate = serviceReservationRequest.getOccurenceDate();
        this.dateScheduleId = Long.parseLong(serviceReservationRequest.getDateScheduleId());
        this.day = getDayOfWeek(serviceReservationRequest.getOccurenceDate());
        this.type = "RESERVED";
        this.workerId = Long.parseLong(serviceReservationRequest.getWorkerId());
    }

    public Long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Long workerId) {
        this.workerId = workerId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public Long getDateScheduleId() {
        return dateScheduleId;
    }

    public void setDateScheduleId(Long dateScheduleId) {
        this.dateScheduleId = dateScheduleId;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public static String getDayOfWeek(String dateStr) {
        SimpleDateFormat originalFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);

        try {
            Date date = originalFormat.parse(dateStr);

            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.ENGLISH);
            String dayOfWeek = dayFormat.format(date).toUpperCase();

            return dayOfWeek;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }
}
