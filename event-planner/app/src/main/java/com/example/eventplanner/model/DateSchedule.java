package com.example.eventplanner.model;

import com.example.eventplanner.utils.DateRange;
import com.example.eventplanner.utils.Days;
import com.example.eventplanner.utils.WorkingHours;
import com.google.gson.Gson;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DateSchedule implements Serializable {

    private Long id;
    private Long workerId;
    private DateRange dateRange;
    private HashMap<String, WorkingHours> schedule;

    public DateSchedule() {
        this.dateRange = null;

        this.schedule = new HashMap<>();
        for (Days day : Days.values()) {
            this.schedule.put(day.toString(), null);
        }
    }

    public DateSchedule(Long workerId, DateRange dateRange, HashMap<String, WorkingHours> schedule) {
        this.workerId = workerId;
        this.schedule = schedule;
        this.dateRange = dateRange;
    }

    public void setItem(String day, WorkingHours hours){
        schedule.put(day, hours);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Long workerId) {
        this.workerId = workerId;
    }

    public DateRange getDateRange() {
        return dateRange;
    }

    public void setDateRange(DateRange dateRange) {
        this.dateRange = dateRange;
    }

    public HashMap<String, WorkingHours> getSchedule() {
        return schedule;
    }

    public void setSchedule(HashMap<String, WorkingHours> schedule) {
        this.schedule = schedule;
    }

    public static DateSchedule fromFirestoreData(Map<String, Object> data) {
        Gson gson = new Gson();
        return gson.fromJson(gson.toJson(data), DateSchedule.class);
    }
}
