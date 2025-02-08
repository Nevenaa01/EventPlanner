package com.example.eventplanner.model;

import java.util.List;

public class PackageReservationRequest {
    private List<ServiceReservationRequest> services;
    private String userId;
    private String pupvId;
    private List<Product> products;
    private String status;

    public PackageReservationRequest() {
    }

    public PackageReservationRequest(List<ServiceReservationRequest> services, String userId, String pupvId, List<Product> products, String status) {
        this.services = services;
        this.userId = userId;
        this.pupvId = pupvId;
        this.products = products;
        this.status = status;
    }

    public List<ServiceReservationRequest> getServices() {
        return services;
    }

    public void setServices(List<ServiceReservationRequest> services) {
        this.services = services;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPupvId() {
        return pupvId;
    }

    public void setPupvId(String pupvId) {
        this.pupvId = pupvId;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
