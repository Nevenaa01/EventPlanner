package com.example.eventplanner.utils;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DateRange implements Serializable {

    private String startDate;
    private String endDate;
    private transient SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public DateRange(String startDate, String endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public DateRange() {
    }

    public DateRange(Date startDate, Date endDate) {
        this.startDate = dateFormat.format(startDate);
        this.endDate = dateFormat.format(endDate);
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    @Override
    @NonNull
    public String toString() {
        return "DateRange{" +
                "startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        return map;
    }
}