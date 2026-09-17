package com.labresource.platform.equipment.repository;

import com.labresource.platform.equipment.Equipment;
import com.labresource.platform.equipment.EquipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    List<Equipment> findByInstitutionId(Long institutionId);

    List<Equipment> findByDepartmentId(Long departmentId);

    List<Equipment> findByCategoryId(Long categoryId);

    List<Equipment> findByInstitutionIdAndStatus(Long institutionId, EquipmentStatus status);

    List<Equipment> findByInstitutionIdAndIsShareableExternally(Long institutionId, boolean isShareableExternally);

    Optional<Equipment> findByAssetTag(String assetTag);

    Optional<Equipment> findBySerialNumber(String serialNumber);

    boolean existsByAssetTag(String assetTag);

    boolean existsBySerialNumber(String serialNumber);
}
