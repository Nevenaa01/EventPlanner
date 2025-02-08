package com.example.eventplanner.model;

public class Subcategory {
    private Long id;
    private String categoryName;
    private String name;
    private String description;
    private int type;//0 -service; 1 - product

    public Subcategory() {
    }

    @Override
    public String toString() {
        return name + " - " + description;
    }

    public Subcategory(String categoryName, String name, String description, int type) {
        this.categoryName = categoryName;
        this.name = name;
        this.description = description;
        this.type = type;
    }

    public Subcategory(Long id, String categoryName, String name, String description, int type) {
        this.id = id;
        this.categoryName = categoryName;
        this.name = name;
        this.description = description;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
