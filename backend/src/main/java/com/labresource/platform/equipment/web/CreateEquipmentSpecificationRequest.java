package com.labresource.platform.equipment.web;

import jakarta.validation.constraints.NotBlank;

public class CreateEquipmentSpecificationRequest {

    @NotBlank(message = "Specification name is required")
    private String specName;

    @NotBlank(message = "Specification value is required")
    private String specValue;

    private String unit;

    public CreateEquipmentSpecificationRequest() {
    }

    public String getSpecName() {
        return specName;
    }

    public void setSpecName(String specName) {
        this.specName = specName;
    }

    public String getSpecValue() {
        return specValue;
    }

    public void setSpecValue(String specValue) {
        this.specValue = specValue;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
