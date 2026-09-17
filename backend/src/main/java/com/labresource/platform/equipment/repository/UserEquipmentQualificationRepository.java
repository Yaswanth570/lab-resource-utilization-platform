package com.labresource.platform.equipment.repository;

import com.labresource.platform.equipment.QualificationStatus;
import com.labresource.platform.equipment.UserEquipmentQualification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserEquipmentQualificationRepository extends JpaRepository<UserEquipmentQualification, Long> {

    List<UserEquipmentQualification> findByUserId(Long userId);

    List<UserEquipmentQualification> findByEquipmentId(Long equipmentId);

    Optional<UserEquipmentQualification> findByUserIdAndEquipmentId(Long userId, Long equipmentId);

    List<UserEquipmentQualification> findByUserIdAndStatus(Long userId, QualificationStatus status);
}
