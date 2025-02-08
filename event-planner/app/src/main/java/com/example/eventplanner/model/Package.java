package com.example.eventplanner.model;

import android.net.Uri;

import java.util.ArrayList;

public class Package {
    private Long id;
    private String pupvId;
    private String name;
    private String description;
    private Double discount;
    private Boolean available;
    private Boolean visible;
    private Long categoryId;
    private ArrayList<Long> subCategoryId;
    private ArrayList<Long> productIds;
    private ArrayList<Long> serviceIds;
    private ArrayList<Long> eventTypeIds;
    private Double price;
    private ArrayList<Uri> images;
    private String reservationDue;
    private String cancelationDue;
    private Boolean automaticAffirmation;
    private Boolean deleted;

    public Package() {
    }

    public Package(Long id, String pupvId, String name, String description, Double discount, Boolean available, Boolean visible, Long categoryId, ArrayList<Long> subCategoryId, ArrayList<Long> productIds, ArrayList<Long> serviceIds, ArrayList<Long> eventTypeIds, Double price, ArrayList<Uri> images, String reservationDue, String cancelationDue, Boolean automaticAffirmation, Boolean deleted) {
        this.id = id;
        this.pupvId = pupvId;
        this.name = name;
        this.description = description;
        this.discount = discount;
        this.available = available;
        this.visible = visible;
        this.categoryId = categoryId;
        this.subCategoryId = subCategoryId;
        this.productIds = productIds;
        this.serviceIds = serviceIds;
        this.eventTypeIds = eventTypeIds;
        this.price = price;
        this.images = images;
        this.reservationDue = reservationDue;
        this.cancelationDue = cancelationDue;
        this.automaticAffirmation = automaticAffirmation;
        this.deleted = deleted;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPupvId() {
        return pupvId;
    }

    public void setPupvId(String pupvId) {
        this.pupvId = pupvId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public ArrayList<Long> getSubCategoryId() {
        return subCategoryId;
    }

    public void setSubCategoryId(ArrayList<Long> subCategoryId) {
        this.subCategoryId = subCategoryId;
    }

    public ArrayList<Long> getProductIds() {
        return productIds;
    }

    public void setProductIds(ArrayList<Long> productIds) {
        this.productIds = productIds;
    }

    public ArrayList<Long> getServiceIds() {
        return serviceIds;
    }

    public void setServiceIds(ArrayList<Long> serviceIds) {
        this.serviceIds = serviceIds;
    }

    public ArrayList<Long> getEventTypeIds() {
        return eventTypeIds;
    }

    public void setEventTypeIds(ArrayList<Long> eventTypeIds) {
        this.eventTypeIds = eventTypeIds;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public ArrayList<Uri> getImages() {
        return images;
    }

    public void setImages(ArrayList<Uri> images) {
        this.images = images;
    }

    public String getReservationDue() {
        return reservationDue;
    }

    public void setReservationDue(String reservationDue) {
        this.reservationDue = reservationDue;
    }

    public String getCancelationDue() {
        return cancelationDue;
    }

    public void setCancelationDue(String cancelationDue) {
        this.cancelationDue = cancelationDue;
    }

    public Boolean getAutomaticAffirmation() {
        return automaticAffirmation;
    }

    public void setAutomaticAffirmation(Boolean automaticAffirmation) {
        this.automaticAffirmation = automaticAffirmation;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}
