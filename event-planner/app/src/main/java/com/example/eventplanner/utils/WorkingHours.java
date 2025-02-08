package com.example.eventplanner.utils;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class WorkingHours implements Serializable {

    private String startTime;
    private String endTime;

    public WorkingHours() {
    }

    public WorkingHours(String startTime, String endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    @Override
    @NonNull
    public String toString() {
        return "WorkingHours{" +
                "startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        return map;
    }

    public static WorkingHours fromMap(Map<String, Object> map) {
        WorkingHours workingHours = new WorkingHours();
        workingHours.setStartTime((String) map.get("startTime"));
        workingHours.setEndTime((String) map.get("endTime"));
        return workingHours;
    }
}