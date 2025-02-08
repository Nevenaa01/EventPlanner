package com.example.eventplanner.model;

public class Notification {
    private Long id;
    private String title;
    private String body;
    private Boolean read;
    private String userId;

    public Notification() {
    }

    public Notification(Long id, String title, String body, Boolean read, String userId) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.read = read;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
