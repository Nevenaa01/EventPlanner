package com.example.eventplanner.model;

public class GuestEvent {
    public Long Id;
    public Long eventId;
    public String Fullname;
    public String Age;

    public String Invite;

    public String AcceptInvite;

    public String specialRequests;

    public GuestEvent(Long id, Long eventId, String fullname, String age, String invite, String acceptInvite, String specialRequests) {
        this.Id = id;
        this.eventId = eventId;
        Fullname = fullname;
        Age = age;
        Invite = invite;
        AcceptInvite = acceptInvite;
        this.specialRequests = specialRequests;
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getFullname() {
        return Fullname;
    }

    public void setFullname(String fullname) {
        Fullname = fullname;
    }

    public String getAge() {
        return Age;
    }

    public void setAge(String age) {
        Age = age;
    }

    public String getInvite() {
        return Invite;
    }

    public void setInvite(String invite) {
        Invite = invite;
    }

    public String getAcceptInvite() {
        return AcceptInvite;
    }

    public void setAcceptInvite(String  acceptInvite) {
        AcceptInvite = acceptInvite;
    }

    public String getSpecialRequests() {
        return specialRequests;
    }

    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests;
    }
}
