package com.labresource.platform.equipment.web;

import com.labresource.platform.equipment.EquipmentSpecification;

public class EquipmentSpecificationResponse {

    private Long id;
    private Long equipmentId;
    private String specName;
    private String specValue;
    private String unit;

    public EquipmentSpecificationResponse() {
    }

    public static EquipmentSpecificationResponse fromEntity(EquipmentSpecification spec) {
        if (spec == null) {
            return null;
        }
        EquipmentSpecificationResponse res = new EquipmentSpecificationResponse();
        res.setId(spec.getId());
        res.setEquipmentId(spec.getEquipment() != null ? spec.getEquipment().getId() : null);
        res.setSpecName(spec.getSpecName());
        res.setSpecValue(spec.getSpecValue());
        res.setUnit(spec.getUnit());
        return res;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Long equipmentId) {
        this.equipmentId = equipmentId;
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
