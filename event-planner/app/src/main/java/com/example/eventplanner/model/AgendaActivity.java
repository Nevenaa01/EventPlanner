package com.example.eventplanner.model;

public class AgendaActivity {
    public Long Id;
    public Long EventId;
    public String Name;
    public String Description;
    public String DurationFrom;
    public String DurationTo;
    public String Address;

    public AgendaActivity(Long id, Long eventId, String name, String description, String durationFrom, String durationTo, String address) {
        Id = id;
        EventId = eventId;
        Name = name;
        Description = description;
        DurationFrom = durationFrom;
        DurationTo = durationTo;
        Address = address;
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public Long getEventId() {
        return EventId;
    }

    public void setEventId(Long eventId) {
        EventId = eventId;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getDurationFrom() {
        return DurationFrom;
    }

    public void setDurationFrom(String durationFrom) {
        DurationFrom = durationFrom;
    }

    public String getDurationTo() {
        return DurationTo;
    }

    public void setDurationTo(String durationTo) {
        DurationTo = durationTo;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }
}
