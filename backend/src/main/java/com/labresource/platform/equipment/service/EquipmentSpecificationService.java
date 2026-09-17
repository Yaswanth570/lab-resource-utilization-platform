package com.labresource.platform.equipment.service;

import com.labresource.platform.equipment.EquipmentSpecification;

import java.util.List;

public interface EquipmentSpecificationService {

    EquipmentSpecification addSpecification(Long equipmentId, EquipmentSpecification spec);

    EquipmentSpecification getSpecificationById(Long id);

    List<EquipmentSpecification> listSpecificationsByEquipment(Long equipmentId);

    EquipmentSpecification getSpecificationByEquipmentAndName(Long equipmentId, String specName);

    EquipmentSpecification updateSpecification(Long id, EquipmentSpecification updatedData);

    void deleteSpecification(Long id);
}
