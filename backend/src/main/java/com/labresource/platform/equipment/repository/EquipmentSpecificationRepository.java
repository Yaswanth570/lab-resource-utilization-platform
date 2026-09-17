package com.labresource.platform.equipment.repository;

import com.labresource.platform.equipment.EquipmentSpecification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentSpecificationRepository extends JpaRepository<EquipmentSpecification, Long> {

    List<EquipmentSpecification> findByEquipmentId(Long equipmentId);

    Optional<EquipmentSpecification> findByEquipmentIdAndSpecName(Long equipmentId, String specName);
}
