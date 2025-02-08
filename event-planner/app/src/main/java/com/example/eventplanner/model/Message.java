package com.example.eventplanner.model;

import java.util.Date;

public class Message {
    public Long Id;
    public String SenderId;
    public String SenderFullName;
    public String RecipientId;

    public String RecipientFullName;
    public Date DateOfSending;
    public String Content;
    public boolean status;

    public Message(Long id, String senderId, String senderFullName, String recipientId, String recipientFullName, Date dateOfSending, String content, boolean status) {
        Id = id;
        SenderId = senderId;
        SenderFullName = senderFullName;
        RecipientId = recipientId;
        RecipientFullName = recipientFullName;
        DateOfSending = dateOfSending;
        Content = content;
        this.status = status;
    }

    public Message(String senderId, String senderFullName, String recipientId, String recipientFullName, Date dateOfSending, String content, boolean status) {
        SenderId = senderId;
        SenderFullName = senderFullName;
        RecipientId = recipientId;
        RecipientFullName = recipientFullName;
        DateOfSending = dateOfSending;
        Content = content;
        this.status = status;
    }

    public String getSenderFullName() {
        return SenderFullName;
    }

    public void setSenderFullName(String senderFullName) {
        SenderFullName = senderFullName;
    }

    public String getRecipientFullName() {
        return RecipientFullName;
    }

    public void setRecipientFullName(String recipientFullName) {
        RecipientFullName = recipientFullName;
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getSenderId() {
        return SenderId;
    }

    public void setSenderId(String senderId) {
        SenderId = senderId;
    }

    public String getRecipientId() {
        return RecipientId;
    }

    public void setRecipientId(String recipientId) {
        RecipientId = recipientId;
    }

    public Date getDateOfSending() {
        return DateOfSending;
    }

    public void setDateOfSending(Date dateOfSending) {
        DateOfSending = dateOfSending;
    }

    public String getContent() {
        return Content;
    }

    public void setContent(String content) {
        Content = content;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
