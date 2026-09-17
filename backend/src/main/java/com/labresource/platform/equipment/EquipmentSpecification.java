package com.labresource.platform.equipment;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "equipment_specifications", uniqueConstraints = {
    @UniqueConstraint(name = "uq_eq_spec", columnNames = {"equipment_id", "spec_name"})
}, indexes = {
    @Index(name = "idx_eq_specs_equip", columnList = "equipment_id")
})
public class EquipmentSpecification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Column(name = "spec_name", nullable = false, length = 100)
    private String specName;

    @Column(name = "spec_value", nullable = false, length = 255)
    private String specValue;

    @Column(name = "unit", length = 50)
    private String unit;

    public EquipmentSpecification() {
    }

    public EquipmentSpecification(Equipment equipment, String specName, String specValue, String unit) {
        this.equipment = equipment;
        this.specName = specName;
        this.specValue = specValue;
        this.unit = unit;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EquipmentSpecification that = (EquipmentSpecification) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
