package com.labresource.platform.equipment.service;

import com.labresource.platform.equipment.QualificationStatus;
import com.labresource.platform.equipment.UserEquipmentQualification;

import java.util.List;

public interface UserEquipmentQualificationService {

    UserEquipmentQualification createQualification(UserEquipmentQualification qualification, Long userId, Long equipmentId, Long certifiedByUserId);

    UserEquipmentQualification getQualificationById(Long id);

    List<UserEquipmentQualification> listQualificationsByUser(Long userId);

    List<UserEquipmentQualification> listQualificationsByEquipment(Long equipmentId);

    UserEquipmentQualification getQualificationByUserAndEquipment(Long userId, Long equipmentId);

    UserEquipmentQualification updateQualificationStatus(Long id, QualificationStatus newStatus, String notes);

    UserEquipmentQualification updateQualification(Long id, UserEquipmentQualification updatedData);

    void revokeQualification(Long id, String reason);
}
