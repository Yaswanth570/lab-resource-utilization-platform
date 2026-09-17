package com.labresource.platform.equipment.service;

import com.labresource.platform.equipment.Equipment;
import com.labresource.platform.equipment.EquipmentStatus;

import java.util.List;

public interface EquipmentService {

    Equipment createEquipment(Equipment equipment, Long institutionId, Long departmentId, Long categoryId, Long primaryLabManagerId);

    Equipment getEquipmentById(Long id);

    Equipment getEquipmentByAssetTag(String assetTag);

    Equipment getEquipmentBySerialNumber(String serialNumber);

    List<Equipment> listEquipment();

    List<Equipment> listEquipmentByInstitution(Long institutionId);

    List<Equipment> listEquipmentByDepartment(Long departmentId);

    List<Equipment> listEquipmentByCategory(Long categoryId);

    List<Equipment> listEquipmentByInstitutionAndStatus(Long institutionId, EquipmentStatus status);

    Equipment updateEquipment(Long id, Equipment updatedData, Long departmentId, Long categoryId, Long primaryLabManagerId);

    Equipment updateOperationalStatus(Long id, EquipmentStatus newStatus, String reason);

    void activateEquipment(Long id);

    void deactivateEquipment(Long id);
}
