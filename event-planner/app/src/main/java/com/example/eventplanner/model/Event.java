package com.example.eventplanner.model;

import java.util.Date;
import java.util.Objects;

public class Event {
    public Long Id;

    public String UserODId;
    public String TypeEvent;
    public String Name;
    public String Description;

    public int MaxPeople;
    public String LocationPlace;
    public int MaxDistance;
    public Date DateEvent;

    public boolean Availble; //dostupno true, ne false
    public Event() {}
    public Event(Long id, String userODId, String typeEvent, String name, String description, int maxPeople, String locationPlace, int maxDistance, Date dateEvent, boolean availble) {
        Id = id;
        TypeEvent = typeEvent;
        Name = name;
        Description = description;
        MaxPeople = maxPeople;
        LocationPlace = locationPlace;
        MaxDistance = maxDistance;
        DateEvent = dateEvent;
        Availble = availble;
        UserODId = userODId;
    }

    public Event( String typeEvent, String name, String description, int maxPeople, String locationPlace, int maxDistance, Date dateEvent, boolean availble) {
        TypeEvent = typeEvent;
        Name = name;
        Description = description;
        MaxPeople = maxPeople;
        LocationPlace = locationPlace;
        MaxDistance = maxDistance;
        DateEvent = dateEvent;
        Availble = availble;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Event otherEvent = (Event) obj;
        return Objects.equals(Id, otherEvent.Id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Id);
    }


    public String getUserODId() {
        return UserODId;
    }

    public void setUserODId(String userODId) {
        UserODId = userODId;
    }

    @Override
    public String toString() {
        return Name;
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getTypeEvent() {
        return TypeEvent;
    }

    public void setTypeEvent(String typeEvent) {
        TypeEvent = typeEvent;
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

    public int getMaxPeople() {
        return MaxPeople;
    }

    public void setMaxPeople(int maxPeople) {
        MaxPeople = maxPeople;
    }

    public String getLocationPlace() {
        return LocationPlace;
    }

    public void setLocationPlace(String locationPlace) {
        LocationPlace = locationPlace;
    }

    public int getMaxDistance() {
        return MaxDistance;
    }

    public void setMaxDistance(int maxDistance) {
        MaxDistance = maxDistance;
    }

    public Date getDateEvent() {
        return DateEvent;
    }

    public void setDateEvent(Date dateEvent) {
        DateEvent = dateEvent;
    }

    public boolean isAvailble() {
        return Availble;
    }

    public void setAvailble(boolean availble) {
        Availble = availble;
    }
}
