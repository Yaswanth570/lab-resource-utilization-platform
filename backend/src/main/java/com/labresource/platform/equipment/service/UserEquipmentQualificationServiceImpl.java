package com.labresource.platform.equipment.service;

import com.labresource.platform.common.exception.DuplicateResourceException;
import com.labresource.platform.common.exception.InvalidOperationException;
import com.labresource.platform.common.exception.ResourceNotFoundException;
import com.labresource.platform.equipment.Equipment;
import com.labresource.platform.equipment.QualificationStatus;
import com.labresource.platform.equipment.UserEquipmentQualification;
import com.labresource.platform.equipment.repository.EquipmentRepository;
import com.labresource.platform.equipment.repository.UserEquipmentQualificationRepository;
import com.labresource.platform.user.User;
import com.labresource.platform.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserEquipmentQualificationServiceImpl implements UserEquipmentQualificationService {

    private final UserEquipmentQualificationRepository qualificationRepository;
    private final UserRepository userRepository;
    private final EquipmentRepository equipmentRepository;

    public UserEquipmentQualificationServiceImpl(UserEquipmentQualificationRepository qualificationRepository,
                                                 UserRepository userRepository,
                                                 EquipmentRepository equipmentRepository) {
        this.qualificationRepository = qualificationRepository;
        this.userRepository = userRepository;
        this.equipmentRepository = equipmentRepository;
    }

    @Override
    @Transactional
    public UserEquipmentQualification createQualification(UserEquipmentQualification qualification,
                                                          Long userId,
                                                          Long equipmentId,
                                                          Long certifiedByUserId) {
        if (qualification == null) {
            throw new InvalidOperationException("Qualification payload cannot be null");
        }
        if (userId == null) {
            throw new InvalidOperationException("User ID is required");
        }
        if (equipmentId == null) {
            throw new InvalidOperationException("Equipment ID is required");
        }
        if (certifiedByUserId == null) {
            throw new InvalidOperationException("Certifying User ID is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", equipmentId));

        User certifiedBy = userRepository.findById(certifiedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", certifiedByUserId));

        if (qualificationRepository.findByUserIdAndEquipmentId(userId, equipmentId).isPresent()) {
            throw new DuplicateResourceException(String.format("User with id %d already has a qualification record for equipment %d",
                    userId, equipmentId));
        }

        qualification.setUser(user);
        qualification.setEquipment(equipment);
        qualification.setCertifiedBy(certifiedBy);

        if (qualification.getStatus() == null) {
            qualification.setStatus(QualificationStatus.ACTIVE);
        }
        if (qualification.getCertifiedAt() == null) {
            qualification.setCertifiedAt(Instant.now());
        }

        return qualificationRepository.save(qualification);
    }

    @Override
    public UserEquipmentQualification getQualificationById(Long id) {
        if (id == null) {
            throw new InvalidOperationException("Qualification ID cannot be null");
        }
        return qualificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserEquipmentQualification", "id", id));
    }

    @Override
    public List<UserEquipmentQualification> listQualificationsByUser(Long userId) {
        if (userId == null) {
            throw new InvalidOperationException("User ID cannot be null");
        }
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        return qualificationRepository.findByUserId(userId);
    }

    @Override
    public List<UserEquipmentQualification> listQualificationsByEquipment(Long equipmentId) {
        if (equipmentId == null) {
            throw new InvalidOperationException("Equipment ID cannot be null");
        }
        if (!equipmentRepository.existsById(equipmentId)) {
            throw new ResourceNotFoundException("Equipment", "id", equipmentId);
        }
        return qualificationRepository.findByEquipmentId(equipmentId);
    }

    @Override
    public UserEquipmentQualification getQualificationByUserAndEquipment(Long userId, Long equipmentId) {
        if (userId == null || equipmentId == null) {
            throw new InvalidOperationException("User ID and Equipment ID must not be null");
        }
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        if (!equipmentRepository.existsById(equipmentId)) {
            throw new ResourceNotFoundException("Equipment", "id", equipmentId);
        }
        return qualificationRepository.findByUserIdAndEquipmentId(userId, equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(
                        "Qualification not found for user %d and equipment %d", userId, equipmentId)));
    }

    @Override
    @Transactional
    public UserEquipmentQualification updateQualificationStatus(Long id, QualificationStatus newStatus, String notes) {
        if (newStatus == null) {
            throw new InvalidOperationException("New qualification status cannot be null");
        }
        UserEquipmentQualification existing = getQualificationById(id);
        existing.setStatus(newStatus);
        if (notes != null) {
            existing.setNotes(notes);
        }
        return qualificationRepository.save(existing);
    }

    @Override
    @Transactional
    public UserEquipmentQualification updateQualification(Long id, UserEquipmentQualification updatedData) {
        if (updatedData == null) {
            throw new InvalidOperationException("Updated qualification data cannot be null");
        }
        UserEquipmentQualification existing = getQualificationById(id);

        if (updatedData.getExpiresAt() != null) {
            existing.setExpiresAt(updatedData.getExpiresAt());
        }
        if (updatedData.getStatus() != null) {
            existing.setStatus(updatedData.getStatus());
        }
        if (updatedData.getNotes() != null) {
            existing.setNotes(updatedData.getNotes());
        }

        return qualificationRepository.save(existing);
    }

    @Override
    @Transactional
    public void revokeQualification(Long id, String reason) {
        UserEquipmentQualification existing = getQualificationById(id);
        existing.setStatus(QualificationStatus.REVOKED);
        if (reason != null && !reason.trim().isEmpty()) {
            String updatedNotes = existing.getNotes() != null
                    ? existing.getNotes() + " | Revocation reason: " + reason.trim()
                    : "Revocation reason: " + reason.trim();
            existing.setNotes(updatedNotes);
        }
        qualificationRepository.save(existing);
    }
}
