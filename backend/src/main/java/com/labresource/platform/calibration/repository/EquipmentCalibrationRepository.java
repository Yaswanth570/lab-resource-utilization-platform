package com.labresource.platform.calibration.repository;

import com.labresource.platform.calibration.CalibrationStatus;
import com.labresource.platform.calibration.EquipmentCalibration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentCalibrationRepository extends JpaRepository<EquipmentCalibration, Long> {

    Optional<EquipmentCalibration> findByCalibrationReference(String calibrationReference);

    boolean existsByCalibrationReference(String calibrationReference);

    List<EquipmentCalibration> findByEquipmentId(Long equipmentId);

    List<EquipmentCalibration> findByEquipmentIdAndStatus(Long equipmentId, CalibrationStatus status);
}
