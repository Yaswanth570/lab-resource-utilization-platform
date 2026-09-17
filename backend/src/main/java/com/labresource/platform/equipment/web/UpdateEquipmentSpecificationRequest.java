package com.labresource.platform.equipment.web;

public class UpdateEquipmentSpecificationRequest {

    private String specName;
    private String specValue;
    private String unit;

    public UpdateEquipmentSpecificationRequest() {
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
