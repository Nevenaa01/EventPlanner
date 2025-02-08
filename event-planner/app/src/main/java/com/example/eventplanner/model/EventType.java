package com.example.eventplanner.model;

import java.io.Serializable;
import java.util.List;

public class EventType implements Serializable {
    private Long id;
    private boolean inUse;
    private String typeName;
    private String typeDescription;
    private List<Subcategory> recomendedSubcategories;
    public EventType() {
    }

    public EventType(Long id, boolean inUse, String typeName, String typeDescription, List<Subcategory> recomendedSubcategories) {
        this.id = id;
        this.inUse = inUse;
        this.typeName = typeName;
        this.typeDescription = typeDescription;
        this.recomendedSubcategories = recomendedSubcategories;
    }

    public EventType(boolean inUse, String typeName, String typeDescription, List<Subcategory> recomendedSubcategories) {
        this.inUse = inUse;
        this.typeName = typeName;
        this.typeDescription = typeDescription;
        this.recomendedSubcategories = recomendedSubcategories;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isInUse() {
        return inUse;
    }

    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    public List<Subcategory> getRecomendedSubcategories() {
        return recomendedSubcategories;
    }

    public void setRecomendedSubcategories(List<Subcategory> recomendedSubcategories) {
        this.recomendedSubcategories = recomendedSubcategories;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeDescription() {
        return typeDescription;
    }

    public void setTypeDescription(String typeDescription) {
        this.typeDescription = typeDescription;
    }
}
