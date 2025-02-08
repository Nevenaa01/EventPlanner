package com.example.eventplanner.model;

import java.io.Serializable;

public class UserPUPZ implements Serializable {

    private Long id;
    private String ownerId;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phone;
    private String address;
    private boolean isValid;
    private String UserType;

    public UserPUPZ() {
    }

    public UserPUPZ(Long id, String ownerId, String firstName, String lastName, String email, String password, String phone, String address, boolean isValid, String userType) {
        this.id = id;
        this.ownerId = ownerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.address = address;
        this.isValid = isValid;
        UserType = userType;
    }

    public UserPUPZ(Long id, String ownerId, String firstName, String lastName, String email, String password, String phone, String address, boolean isValid) {
        this.id = id;
        this.ownerId = ownerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.address = address;
        this.isValid = isValid;
    }

    public Long getId() {
        return id;
    }

    public String getUserType() {
        return UserType;
    }

    public void setUserType(String userType) {
        UserType = userType;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }
}
