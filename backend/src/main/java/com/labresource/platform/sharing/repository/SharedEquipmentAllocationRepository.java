package com.labresource.platform.sharing.repository;

import com.labresource.platform.sharing.SharedEquipmentAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SharedEquipmentAllocationRepository extends JpaRepository<SharedEquipmentAllocation, Long> {

    List<SharedEquipmentAllocation> findBySharingAgreementId(Long sharingAgreementId);

    List<SharedEquipmentAllocation> findByEquipmentId(Long equipmentId);

    Optional<SharedEquipmentAllocation> findBySharingAgreementIdAndEquipmentId(Long sharingAgreementId, Long equipmentId);

    List<SharedEquipmentAllocation> findBySharingAgreementIdAndIsActive(Long sharingAgreementId, boolean isActive);
}
