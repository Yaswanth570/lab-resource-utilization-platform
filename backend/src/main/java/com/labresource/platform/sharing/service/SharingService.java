package com.labresource.platform.sharing.service;

import com.labresource.platform.sharing.ResourceSharingAgreement;
import com.labresource.platform.sharing.SharedEquipmentAllocation;
import com.labresource.platform.sharing.SharingAgreementStatus;

import java.math.BigDecimal;
import java.util.List;

public interface SharingService {

    // ==========================================
    // Agreement Operations
    // ==========================================

    ResourceSharingAgreement createAgreement(ResourceSharingAgreement agreement, Long requestingInstitutionId, Long ownerInstitutionId, Long sharingRequestId);

    ResourceSharingAgreement getAgreementById(Long id);

    ResourceSharingAgreement getAgreementByCode(String code);

    List<ResourceSharingAgreement> listAgreements(Long institutionId, Long requestingInstitutionId, Long ownerInstitutionId, SharingAgreementStatus status);

    ResourceSharingAgreement updateAgreementStatus(Long id, SharingAgreementStatus newStatus, Long operatorInstitutionId);

    // ==========================================
    // Allocation Operations
    // ==========================================

    SharedEquipmentAllocation createAllocation(Long agreementId, Long equipmentId, BigDecimal customHourlyRate, Boolean isActive, Long operatorInstitutionId);

    SharedEquipmentAllocation getAllocationById(Long id);

    List<SharedEquipmentAllocation> listAllocations(Long agreementId, Long equipmentId, Boolean isActive, Long institutionId);

    SharedEquipmentAllocation updateAllocationActive(Long id, boolean isActive, Long operatorInstitutionId);
}
