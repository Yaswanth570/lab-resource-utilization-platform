package com.labresource.platform.calibration.repository;

import com.labresource.platform.calibration.CertificationStatus;
import com.labresource.platform.calibration.EquipmentCertification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentCertificationRepository extends JpaRepository<EquipmentCertification, Long> {

    List<EquipmentCertification> findByEquipmentId(Long equipmentId);

    List<EquipmentCertification> findByEquipmentIdAndStatus(Long equipmentId, CertificationStatus status);

    List<EquipmentCertification> findByEquipmentIdAndIsMandatory(Long equipmentId, boolean isMandatory);

    List<EquipmentCertification> findByEquipmentIdAndIsMandatoryAndStatus(Long equipmentId, boolean isMandatory, CertificationStatus status);
}
