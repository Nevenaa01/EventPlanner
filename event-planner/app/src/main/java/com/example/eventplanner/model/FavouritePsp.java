package com.example.eventplanner.model;

import java.util.ArrayList;

public class FavouritePsp {
    public ArrayList<Long> productIds;
    public ArrayList<Long> serviceIds;
    public ArrayList<Long> packageIds;

    public FavouritePsp(ArrayList<Long> productIds, ArrayList<Long> serviceIds, ArrayList<Long> packageIds) {
        this.productIds = productIds;
        this.serviceIds = serviceIds;
        this.packageIds = packageIds;
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

    public ArrayList<Long> getPackageIds() {
        return packageIds;
    }

    public void setPackageIds(ArrayList<Long> packageIds) {
        this.packageIds = packageIds;
    }
}
