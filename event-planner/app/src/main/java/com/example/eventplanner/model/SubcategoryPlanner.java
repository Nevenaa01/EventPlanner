package com.example.eventplanner.model;

public class SubcategoryPlanner {
    private Long serialNum;
    private String NameCategory;
    private String NameSubcategory;
    private Float Price;

    private Long EventId;

    public SubcategoryPlanner(){

    }

    public SubcategoryPlanner(Long serialNum, String nameCategory, String nameSubcategory, Float price, Long eventId) {
        this.serialNum = serialNum;
        NameCategory = nameCategory;
        NameSubcategory = nameSubcategory;
        Price = price;
        EventId = eventId;
    }

    public SubcategoryPlanner(Long serialNum, String nameCategory, String nameSubcategory, Float price) {
        this.serialNum = serialNum;
        NameCategory = nameCategory;
        NameSubcategory = nameSubcategory;
        Price = price;
    }

    public Long getEventId() {
        return EventId;
    }

    public void setEventId(Long eventId) {
        EventId = eventId;
    }

    public Long getSerialNum() {
        return serialNum;
    }

    public void setSerialNum(Long serialNum) {
        this.serialNum = serialNum;
    }

    public String getNameCategory() {
        return NameCategory;
    }

    public void setNameCategory(String nameCategory) {
        NameCategory = nameCategory;
    }

    public String getNameSubcategory() {
        return NameSubcategory;
    }

    public void setNameSubcategory(String nameSubcategory) {
        NameSubcategory = nameSubcategory;
    }

    public Float getPrice() {
        return Price;
    }

    public void setPrice(Float price) {
        Price = price;
    }
}
