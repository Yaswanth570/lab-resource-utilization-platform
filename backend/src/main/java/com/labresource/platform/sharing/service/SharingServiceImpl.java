package com.labresource.platform.sharing.service;

import com.labresource.platform.common.exception.DuplicateResourceException;
import com.labresource.platform.common.exception.InvalidOperationException;
import com.labresource.platform.common.exception.ResourceNotFoundException;
import com.labresource.platform.equipment.Equipment;
import com.labresource.platform.equipment.repository.EquipmentRepository;
import com.labresource.platform.institution.Institution;
import com.labresource.platform.institution.repository.InstitutionRepository;
import com.labresource.platform.security.principal.SecurityUtils;
import com.labresource.platform.sharing.*;
import com.labresource.platform.sharing.repository.ResourceSharingAgreementRepository;
import com.labresource.platform.sharing.repository.ResourceSharingRequestRepository;
import com.labresource.platform.sharing.repository.SharedEquipmentAllocationRepository;
import com.labresource.platform.user.User;
import com.labresource.platform.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
public class SharingServiceImpl implements SharingService {

    private final ResourceSharingAgreementRepository agreementRepository;
    private final SharedEquipmentAllocationRepository allocationRepository;
    private final ResourceSharingRequestRepository requestRepository;
    private final InstitutionRepository institutionRepository;
    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;

    public SharingServiceImpl(ResourceSharingAgreementRepository agreementRepository,
                              SharedEquipmentAllocationRepository allocationRepository,
                              ResourceSharingRequestRepository requestRepository,
                              InstitutionRepository institutionRepository,
                              EquipmentRepository equipmentRepository,
                              UserRepository userRepository) {
        this.agreementRepository = agreementRepository;
        this.allocationRepository = allocationRepository;
        this.requestRepository = requestRepository;
        this.institutionRepository = institutionRepository;
        this.equipmentRepository = equipmentRepository;
        this.userRepository = userRepository;
    }

    // ==========================================
    // Agreement Operations
    // ==========================================

    @Override
    public ResourceSharingAgreement createAgreement(ResourceSharingAgreement agreement,
                                                    Long requestingInstitutionId,
                                                    Long ownerInstitutionId,
                                                    Long sharingRequestId) {
        if (requestingInstitutionId == null || ownerInstitutionId == null) {
            throw new InvalidOperationException("Both requesting and owner institution IDs are required");
        }
        if (requestingInstitutionId.equals(ownerInstitutionId)) {
            throw new InvalidOperationException("Requesting institution and owner institution must be different");
        }

        Institution requestingInstitution = institutionRepository.findById(requestingInstitutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Institution", "id", requestingInstitutionId));

        Institution ownerInstitution = institutionRepository.findById(ownerInstitutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Institution", "id", ownerInstitutionId));

        if (agreement.getStartDate() == null || agreement.getEndDate() == null) {
            throw new InvalidOperationException("Start date and end date are required");
        }
        if (agreement.getEndDate().isBefore(agreement.getStartDate())) {
            throw new InvalidOperationException("End date cannot be before start date");
        }

        if (agreement.getBillingRateMultiplier() == null || agreement.getBillingRateMultiplier().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Billing rate multiplier must be strictly positive");
        }

        if (agreement.getMaxMonthlyHours() != null && agreement.getMaxMonthlyHours() <= 0) {
            throw new InvalidOperationException("Max monthly hours quota must be positive");
        }

        // Handle Agreement Code
        if (agreement.getAgreementCode() == null || agreement.getAgreementCode().isBlank()) {
            agreement.setAgreementCode("AGR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        } else if (agreementRepository.existsByAgreementCode(agreement.getAgreementCode())) {
            throw new DuplicateResourceException("Sharing Agreement with code '" + agreement.getAgreementCode() + "' already exists");
        }

        // Link or auto-create ResourceSharingRequest
        ResourceSharingRequest request;
        if (sharingRequestId != null) {
            request = requestRepository.findById(sharingRequestId)
                    .orElseThrow(() -> new ResourceNotFoundException("ResourceSharingRequest", "id", sharingRequestId));
            if (!request.getRequestingInstitution().getId().equals(requestingInstitutionId) ||
                !request.getOwnerInstitution().getId().equals(ownerInstitutionId)) {
                throw new InvalidOperationException("Specified sharing request does not match the agreement institutions");
            }
            if (agreementRepository.findBySharingRequestId(sharingRequestId).isPresent()) {
                throw new DuplicateResourceException("An agreement already exists for sharing request #" + sharingRequestId);
            }
        } else {
            // Auto-create backing approved request to fulfill the non-null foreign key constraint
            request = new ResourceSharingRequest();
            request.setRequestingInstitution(requestingInstitution);
            request.setOwnerInstitution(ownerInstitution);
            request.setStatus(SharingRequestStatus.APPROVED);
            request.setRequestTitle("Administrative Sharing Agreement " + agreement.getAgreementCode());
            request.setJustification("Auto-created for administrative cross-institutional resource sharing agreement");
            request.setRequestedStartDate(agreement.getStartDate());
            request.setRequestedEndDate(agreement.getEndDate());

            // Resolve an attributing user
            Long currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
            User requester = null;
            if (currentUserId != null) {
                requester = userRepository.findById(currentUserId).orElse(null);
            }
            if (requester == null) {
                List<User> reqUsers = userRepository.findByInstitutionId(requestingInstitutionId);
                if (!reqUsers.isEmpty()) {
                    requester = reqUsers.get(0);
                } else {
                    List<User> allUsers = userRepository.findAll();
                    if (!allUsers.isEmpty()) {
                        requester = allUsers.get(0);
                    }
                }
            }
            if (requester == null) {
                throw new InvalidOperationException("Cannot establish agreement: no user found to attribute sharing request");
            }

            request.setRequestedByUser(requester);
            request.setReviewedByUser(requester);
            request.setReviewedAt(Instant.now());
            request = requestRepository.save(request);
        }

        agreement.setRequestingInstitution(requestingInstitution);
        agreement.setOwnerInstitution(ownerInstitution);
        agreement.setSharingRequest(request);
        if (agreement.getStatus() == null) {
            agreement.setStatus(SharingAgreementStatus.ACTIVE);
        }

        ResourceSharingAgreement saved = agreementRepository.save(agreement);
        return initializeAgreement(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ResourceSharingAgreement getAgreementById(Long id) {
        ResourceSharingAgreement agreement = agreementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ResourceSharingAgreement", "id", id));
        return initializeAgreement(agreement);
    }

    @Override
    @Transactional(readOnly = true)
    public ResourceSharingAgreement getAgreementByCode(String code) {
        ResourceSharingAgreement agreement = agreementRepository.findByAgreementCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("ResourceSharingAgreement", "code", code));
        return initializeAgreement(agreement);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceSharingAgreement> listAgreements(Long institutionId,
                                                         Long requestingInstitutionId,
                                                         Long ownerInstitutionId,
                                                         SharingAgreementStatus status) {
        List<ResourceSharingAgreement> list = agreementRepository.findAll();
        return list.stream()
                .filter(ag -> {
                    if (institutionId != null &&
                        !Objects.equals(ag.getRequestingInstitution().getId(), institutionId) &&
                        !Objects.equals(ag.getOwnerInstitution().getId(), institutionId)) {
                        return false;
                    }
                    if (requestingInstitutionId != null &&
                        !Objects.equals(ag.getRequestingInstitution().getId(), requestingInstitutionId)) {
                        return false;
                    }
                    if (ownerInstitutionId != null &&
                        !Objects.equals(ag.getOwnerInstitution().getId(), ownerInstitutionId)) {
                        return false;
                    }
                    if (status != null && ag.getStatus() != status) {
                        return false;
                    }
                    return true;
                })
                .map(this::initializeAgreement)
                .toList();
    }

    @Override
    public ResourceSharingAgreement updateAgreementStatus(Long id, SharingAgreementStatus newStatus, Long operatorInstitutionId) {
        ResourceSharingAgreement agreement = getAgreementById(id);

        if (operatorInstitutionId != null &&
            !Objects.equals(agreement.getOwnerInstitution().getId(), operatorInstitutionId) &&
            !Objects.equals(agreement.getRequestingInstitution().getId(), operatorInstitutionId)) {
            throw new InvalidOperationException("Institution is not a party to this agreement");
        }

        if (agreement.getStatus() == SharingAgreementStatus.TERMINATED) {
            throw new InvalidOperationException("Terminated agreements cannot change status");
        }
        if (agreement.getStatus() == SharingAgreementStatus.EXPIRED) {
            throw new InvalidOperationException("Expired agreements cannot change status");
        }

        if (agreement.getStatus() == newStatus) {
            return initializeAgreement(agreement);
        }

        // Validate lifecycle transitions
        if (agreement.getStatus() == SharingAgreementStatus.ACTIVE) {
            if (newStatus != SharingAgreementStatus.SUSPENDED &&
                newStatus != SharingAgreementStatus.TERMINATED &&
                newStatus != SharingAgreementStatus.EXPIRED) {
                throw new InvalidOperationException("Invalid transition from ACTIVE to " + newStatus);
            }
        } else if (agreement.getStatus() == SharingAgreementStatus.SUSPENDED) {
            if (newStatus != SharingAgreementStatus.ACTIVE &&
                newStatus != SharingAgreementStatus.TERMINATED &&
                newStatus != SharingAgreementStatus.EXPIRED) {
                throw new InvalidOperationException("Invalid transition from SUSPENDED to " + newStatus);
            }
        }

        agreement.setStatus(newStatus);
        ResourceSharingAgreement updated = agreementRepository.save(agreement);
        return initializeAgreement(updated);
    }

    // ==========================================
    // Allocation Operations
    // ==========================================

    @Override
    public SharedEquipmentAllocation createAllocation(Long agreementId,
                                                      Long equipmentId,
                                                      BigDecimal customHourlyRate,
                                                      Boolean isActive,
                                                      Long operatorInstitutionId) {
        if (agreementId == null || equipmentId == null) {
            throw new InvalidOperationException("Both agreementId and equipmentId are required");
        }

        ResourceSharingAgreement agreement = getAgreementById(agreementId);

        if (agreement.getStatus() != SharingAgreementStatus.ACTIVE) {
            throw new InvalidOperationException("Cannot allocate equipment under non-ACTIVE agreement (status: " + agreement.getStatus() + ")");
        }

        if (agreement.getEndDate().isBefore(LocalDate.now())) {
            throw new InvalidOperationException("Cannot allocate equipment to an expired sharing agreement");
        }

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", equipmentId));

        if (!equipment.isShareableExternally()) {
            throw new InvalidOperationException("Equipment '" + equipment.getName() + "' is not marked as shareable externally");
        }

        if (!Objects.equals(equipment.getInstitution().getId(), agreement.getOwnerInstitution().getId())) {
            throw new InvalidOperationException(String.format(
                    "Equipment institution #%d does not match agreement owner institution #%d",
                    equipment.getInstitution().getId(), agreement.getOwnerInstitution().getId()));
        }

        if (operatorInstitutionId != null && !Objects.equals(operatorInstitutionId, agreement.getOwnerInstitution().getId())) {
            throw new InvalidOperationException("Only the equipment owner institution can allocate equipment under this agreement");
        }

        if (allocationRepository.findBySharingAgreementIdAndEquipmentId(agreementId, equipmentId).isPresent()) {
            throw new DuplicateResourceException(String.format(
                    "Equipment '%s' is already allocated under agreement '%s'",
                    equipment.getName(), agreement.getAgreementCode()));
        }

        if (customHourlyRate != null && customHourlyRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidOperationException("Custom hourly rate cannot be negative");
        }

        SharedEquipmentAllocation allocation = new SharedEquipmentAllocation();
        allocation.setSharingAgreement(agreement);
        allocation.setEquipment(equipment);
        allocation.setCustomHourlyRate(customHourlyRate);
        allocation.setActive(isActive != null ? isActive : true);

        SharedEquipmentAllocation saved = allocationRepository.save(allocation);
        return initializeAllocation(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SharedEquipmentAllocation getAllocationById(Long id) {
        SharedEquipmentAllocation allocation = allocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SharedEquipmentAllocation", "id", id));
        return initializeAllocation(allocation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SharedEquipmentAllocation> listAllocations(Long agreementId,
                                                           Long equipmentId,
                                                           Boolean isActive,
                                                           Long institutionId) {
        List<SharedEquipmentAllocation> list = allocationRepository.findAll();
        return list.stream()
                .filter(alloc -> {
                    if (agreementId != null && !Objects.equals(alloc.getSharingAgreement().getId(), agreementId)) {
                        return false;
                    }
                    if (equipmentId != null && !Objects.equals(alloc.getEquipment().getId(), equipmentId)) {
                        return false;
                    }
                    if (isActive != null && alloc.isActive() != isActive) {
                        return false;
                    }
                    if (institutionId != null) {
                        Long ownerId = alloc.getSharingAgreement().getOwnerInstitution().getId();
                        Long reqId = alloc.getSharingAgreement().getRequestingInstitution().getId();
                        if (!Objects.equals(ownerId, institutionId) && !Objects.equals(reqId, institutionId)) {
                            return false;
                        }
                    }
                    return true;
                })
                .map(this::initializeAllocation)
                .toList();
    }

    @Override
    public SharedEquipmentAllocation updateAllocationActive(Long id, boolean isActive, Long operatorInstitutionId) {
        SharedEquipmentAllocation allocation = getAllocationById(id);

        if (operatorInstitutionId != null &&
            !Objects.equals(allocation.getSharingAgreement().getOwnerInstitution().getId(), operatorInstitutionId)) {
            throw new InvalidOperationException("Only the equipment owner institution can modify allocation active status");
        }

        if (isActive) {
            if (allocation.getSharingAgreement().getStatus() != SharingAgreementStatus.ACTIVE) {
                throw new InvalidOperationException("Cannot activate allocation under non-ACTIVE agreement (status: " +
                        allocation.getSharingAgreement().getStatus() + ")");
            }
            if (!allocation.getEquipment().isShareableExternally()) {
                throw new InvalidOperationException("Cannot activate allocation for equipment that is not externally shareable");
            }
        }

        allocation.setActive(isActive);
        SharedEquipmentAllocation updated = allocationRepository.save(allocation);
        return initializeAllocation(updated);
    }

    // ==========================================
    // Lazy Proxy Helpers
    // ==========================================

    private ResourceSharingAgreement initializeAgreement(ResourceSharingAgreement ag) {
        if (ag != null) {
            if (ag.getRequestingInstitution() != null) {
                try {
                    ag.getRequestingInstitution().getName();
                } catch (Exception ignored) {
                }
            }
            if (ag.getOwnerInstitution() != null) {
                try {
                    ag.getOwnerInstitution().getName();
                } catch (Exception ignored) {
                }
            }
        }
        return ag;
    }

    private SharedEquipmentAllocation initializeAllocation(SharedEquipmentAllocation alloc) {
        if (alloc != null) {
            if (alloc.getSharingAgreement() != null) {
                initializeAgreement(alloc.getSharingAgreement());
            }
            if (alloc.getEquipment() != null) {
                try {
                    alloc.getEquipment().getName();
                    if (alloc.getEquipment().getCategory() != null) {
                        alloc.getEquipment().getCategory().getName();
                    }
                    if (alloc.getEquipment().getInstitution() != null) {
                        alloc.getEquipment().getInstitution().getName();
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return alloc;
    }
}
