package com.example.eventplanner.model;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class UserPUPV implements Serializable {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phone;
    private String address;
    private boolean isValid;
    private String companyName;
    private String companyDescription;
    private String companyAddress;
    private String companyemail;
    private String companyPhone;
    private String workTime;

    private List<String> eventTypesIds;
    private List<EventType> eventTypes;
    private List<String> categoriesIds;

    private List<Category> categories;

    private String dateTimePosted;

    public UserPUPV(String id, String firstName, String lastName, String email, String password, String phone, String address, boolean isValid, String companyName, String companyDescription, String companyAddress, String companyemail, String companyPhone, String workTime,List<String> eventTypesLong,List<String> categoriesLong,String dateTimePosted) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.address = address;
        this.isValid = isValid;
        this.companyName = companyName;
        this.companyDescription = companyDescription;
        this.companyAddress = companyAddress;
        this.companyemail = companyemail;
        this.companyPhone = companyPhone;
        this.workTime = workTime;
        this.eventTypesIds=eventTypesLong;
        this.categoriesIds=categoriesLong;
        this.dateTimePosted=dateTimePosted;

    }

    public String getDateTimePosted() {
        return dateTimePosted;
    }

    public void setDateTimePosted(String dateTimePosted) {
        this.dateTimePosted = dateTimePosted;
    }

    public List<String> getEventTypesIds() {
        return eventTypesIds;
    }

    public void setEventTypesIds(List<String> eventTypesIds) {
        this.eventTypesIds = eventTypesIds;
    }

    public List<String> getCategoriesIds() {
        return categoriesIds;
    }

    public void setCategoriesIds(List<String> categoriesIds) {
        this.categoriesIds = categoriesIds;
    }

    public List<EventType> getEventTypes() {
        return eventTypes;
    }

    public void setEventTypes(List<EventType> eventTypes) {
        this.eventTypes = eventTypes;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public UserPUPV(String firstName, String lastName, String email, String password, String phone, String address, boolean isValid, String companyName, String companyDescription, String companyAddress, String companyemail, String companyPhone, String workTime) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.address = address;
        this.isValid = isValid;
        this.companyName = companyName;
        this.companyDescription = companyDescription;
        this.companyAddress = companyAddress;
        this.companyemail = companyemail;
        this.companyPhone = companyPhone;
        this.workTime = workTime;
    }

    public UserPUPV() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyDescription() {
        return companyDescription;
    }

    public void setCompanyDescription(String companyDescription) {
        this.companyDescription = companyDescription;
    }

    public String getCompanyAddress() {
        return companyAddress;
    }

    public void setCompanyAddress(String companyAddress) {
        this.companyAddress = companyAddress;
    }

    public String getCompanyemail() {
        return companyemail;
    }

    public void setCompanyemail(String companyemail) {
        this.companyemail = companyemail;
    }

    public String getCompanyPhone() {
        return companyPhone;
    }

    public void setCompanyPhone(String companyPhone) {
        this.companyPhone = companyPhone;
    }

    public String getWorkTime() {
        return workTime;
    }

    public void setWorkTime(String workTime) {
        this.workTime = workTime;
    }
}
