package com.labresource.platform.equipment.repository;

import com.labresource.platform.equipment.EquipmentCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EquipmentCategoryRepository extends JpaRepository<EquipmentCategory, Long> {

    Optional<EquipmentCategory> findByName(String name);

    boolean existsByName(String name);
}
