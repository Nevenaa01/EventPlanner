package com.example.eventplanner.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Product implements Parcelable {
    private Long id;
    private String pupvId;
    private Long categoryId;
    private Long subcategoryId;
    private String name;
    private String description;
    private Double price;
    private Double discount;
    private ArrayList<Uri> images;
    private ArrayList<Long> eventTypeIds;
    private Boolean available;
    private Boolean visible;
    private Boolean pending;
    private Boolean deleted;

    public Product() {
    }

    public Product(Long id, String pupvId, Long categoryId, Long subcategoryId, String name, String description, Double price, Double discount, ArrayList<Uri> images, ArrayList<Long> eventTypeIds, Boolean available, Boolean visible, Boolean pending, Boolean deleted) {
        this.id = id;
        this.pupvId = pupvId;
        this.categoryId = categoryId;
        this.subcategoryId = subcategoryId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.discount = discount;
        this.images = images;
        this.eventTypeIds = eventTypeIds;
        this.available = available;
        this.visible = visible;
        this.pending = pending;
        this.deleted = deleted;
    }

    protected Product(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        pupvId = in.readString();
        if (in.readByte() == 0) {
            categoryId = null;
        } else {
            categoryId = in.readLong();
        }
        if (in.readByte() == 0) {
            subcategoryId = null;
        } else {
            subcategoryId = in.readLong();
        }
        name = in.readString();
        description = in.readString();
        if (in.readByte() == 0) {
            price = null;
        } else {
            price = in.readDouble();
        }
        if (in.readByte() == 0) {
            discount = null;
        } else {
            discount = in.readDouble();
        }
        images = new ArrayList<>();
        in.readList(images, Uri.class.getClassLoader());
        eventTypeIds = new ArrayList<>();
        in.readList(eventTypeIds, Long.class.getClassLoader());
        byte tmpAvailable = in.readByte();
        available = tmpAvailable == 0 ? null : tmpAvailable == 1;
        byte tmpVisible = in.readByte();
        visible = tmpVisible == 0 ? null : tmpVisible == 1;
        byte tmpPending = in.readByte();
        pending = tmpPending == 0 ? null : tmpPending == 1;
        byte tmpDeleted = in.readByte();
        deleted = tmpDeleted == 0 ? null : tmpDeleted == 1;
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(id);
        }
        dest.writeString(pupvId);
        if (categoryId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(categoryId);
        }
        if (subcategoryId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(subcategoryId);
        }
        dest.writeString(name);
        dest.writeString(description);
        if (price == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(price);
        }
        if (discount == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(discount);
        }
        dest.writeList(images);
        dest.writeList(eventTypeIds);
        dest.writeByte((byte) (available == null ? 0 : available ? 1 : 2));
        dest.writeByte((byte) (visible == null ? 0 : visible ? 1 : 2));
        dest.writeByte((byte) (pending == null ? 0 : pending ? 1 : 2));
        dest.writeByte((byte) (deleted == null ? 0 : deleted ? 1 : 2));
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

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getSubcategoryId() {
        return subcategoryId;
    }

    public void setSubcategoryId(Long subcategoryId) {
        this.subcategoryId = subcategoryId;
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

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public ArrayList<Uri> getImages() {
        return images;
    }

    public void setImages(ArrayList<Uri> images) {
        this.images = images;
    }

    public ArrayList<Long> getEventTypeIds() {
        return eventTypeIds;
    }

    public void setEventTypeIds(ArrayList<Long> eventTypeIds) {
        this.eventTypeIds = eventTypeIds;
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

    public Boolean getPending() {
        return pending;
    }

    public void setPending(Boolean pending) {
        this.pending = pending;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}
