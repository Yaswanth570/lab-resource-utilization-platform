package com.labresource.platform.equipment.web;

public class UpdateEquipmentCategoryRequest {

    private String name;
    private String description;

    public UpdateEquipmentCategoryRequest() {
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
}
