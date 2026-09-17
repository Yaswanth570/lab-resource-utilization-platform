package com.labresource.platform.equipment.service;

import com.labresource.platform.common.exception.DuplicateResourceException;
import com.labresource.platform.common.exception.InvalidOperationException;
import com.labresource.platform.common.exception.ResourceNotFoundException;
import com.labresource.platform.department.Department;
import com.labresource.platform.department.repository.DepartmentRepository;
import com.labresource.platform.equipment.Equipment;
import com.labresource.platform.equipment.EquipmentCategory;
import com.labresource.platform.equipment.EquipmentStatus;
import com.labresource.platform.equipment.repository.EquipmentCategoryRepository;
import com.labresource.platform.equipment.repository.EquipmentRepository;
import com.labresource.platform.institution.Institution;
import com.labresource.platform.institution.repository.InstitutionRepository;
import com.labresource.platform.user.User;
import com.labresource.platform.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final InstitutionRepository institutionRepository;
    private final DepartmentRepository departmentRepository;
    private final EquipmentCategoryRepository equipmentCategoryRepository;
    private final UserRepository userRepository;

    public EquipmentServiceImpl(EquipmentRepository equipmentRepository,
                                InstitutionRepository institutionRepository,
                                DepartmentRepository departmentRepository,
                                EquipmentCategoryRepository equipmentCategoryRepository,
                                UserRepository userRepository) {
        this.equipmentRepository = equipmentRepository;
        this.institutionRepository = institutionRepository;
        this.departmentRepository = departmentRepository;
        this.equipmentCategoryRepository = equipmentCategoryRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Equipment createEquipment(Equipment equipment, Long institutionId, Long departmentId, Long categoryId, Long primaryLabManagerId) {
        if (equipment == null) {
            throw new InvalidOperationException("Equipment payload cannot be null");
        }
        if (institutionId == null) {
            throw new InvalidOperationException("Institution ID is required to create equipment");
        }
        if (departmentId == null) {
            throw new InvalidOperationException("Department ID is required to create equipment");
        }
        if (categoryId == null) {
            throw new InvalidOperationException("Equipment category ID is required");
        }
        if (equipment.getName() == null || equipment.getName().trim().isEmpty()) {
            throw new InvalidOperationException("Equipment name is required");
        }
        if (equipment.getAssetTag() == null || equipment.getAssetTag().trim().isEmpty()) {
            throw new InvalidOperationException("Asset tag is required");
        }
        if (equipment.getSerialNumber() == null || equipment.getSerialNumber().trim().isEmpty()) {
            throw new InvalidOperationException("Serial number is required");
        }

        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Institution", "id", institutionId));

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", departmentId));

        // Tenant Rule: Equipment.department.institution == Equipment.institution
        if (!department.getInstitution().getId().equals(institutionId)) {
            throw new InvalidOperationException(String.format(
                    "Department with id %d does not belong to institution with id %d", departmentId, institutionId));
        }

        EquipmentCategory category = equipmentCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("EquipmentCategory", "id", categoryId));

        if (primaryLabManagerId != null) {
            User manager = userRepository.findById(primaryLabManagerId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", primaryLabManagerId));
            if (!manager.getInstitution().getId().equals(institutionId)) {
                throw new InvalidOperationException(String.format(
                        "Primary lab manager with id %d does not belong to institution with id %d", primaryLabManagerId, institutionId));
            }
            equipment.setPrimaryLabManager(manager);
        }

        String normalizedAssetTag = equipment.getAssetTag().trim().toUpperCase();
        String normalizedSerialNumber = equipment.getSerialNumber().trim().toUpperCase();

        if (equipmentRepository.existsByAssetTag(normalizedAssetTag)) {
            throw new DuplicateResourceException("Equipment", "assetTag", normalizedAssetTag);
        }
        if (equipmentRepository.existsBySerialNumber(normalizedSerialNumber)) {
            throw new DuplicateResourceException("Equipment", "serialNumber", normalizedSerialNumber);
        }

        equipment.setInstitution(institution);
        equipment.setDepartment(department);
        equipment.setCategory(category);
        equipment.setName(equipment.getName().trim());
        equipment.setAssetTag(normalizedAssetTag);
        equipment.setSerialNumber(normalizedSerialNumber);

        if (equipment.getStatus() == null) {
            equipment.setStatus(EquipmentStatus.AVAILABLE);
        }

        return initializeEquipment(equipmentRepository.save(equipment));
    }

    private Equipment initializeEquipment(Equipment eq) {
        if (eq == null) {
            return null;
        }
        if (eq.getInstitution() != null) {
            try { eq.getInstitution().getName(); } catch (Exception ignored) {}
        }
        if (eq.getDepartment() != null) {
            try { eq.getDepartment().getName(); } catch (Exception ignored) {}
        }
        if (eq.getCategory() != null) {
            try { eq.getCategory().getName(); } catch (Exception ignored) {}
        }
        if (eq.getPrimaryLabManager() != null) {
            try {
                eq.getPrimaryLabManager().getFirstName();
                eq.getPrimaryLabManager().getLastName();
            } catch (Exception ignored) {}
        }
        return eq;
    }

    @Override
    public Equipment getEquipmentById(Long id) {
        if (id == null) {
            throw new InvalidOperationException("Equipment ID cannot be null");
        }
        return initializeEquipment(equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", id)));
    }

    @Override
    public Equipment getEquipmentByAssetTag(String assetTag) {
        if (assetTag == null || assetTag.trim().isEmpty()) {
            throw new InvalidOperationException("Asset tag cannot be null or blank");
        }
        return initializeEquipment(equipmentRepository.findByAssetTag(assetTag.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", "assetTag", assetTag.trim().toUpperCase())));
    }

    @Override
    public Equipment getEquipmentBySerialNumber(String serialNumber) {
        if (serialNumber == null || serialNumber.trim().isEmpty()) {
            throw new InvalidOperationException("Serial number cannot be null or blank");
        }
        return initializeEquipment(equipmentRepository.findBySerialNumber(serialNumber.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", "serialNumber", serialNumber.trim().toUpperCase())));
    }

    @Override
    public List<Equipment> listEquipment() {
        return equipmentRepository.findAll().stream().map(this::initializeEquipment).toList();
    }

    @Override
    public List<Equipment> listEquipmentByInstitution(Long institutionId) {
        if (institutionId == null) {
            throw new InvalidOperationException("Institution ID cannot be null");
        }
        if (!institutionRepository.existsById(institutionId)) {
            throw new ResourceNotFoundException("Institution", "id", institutionId);
        }
        return equipmentRepository.findByInstitutionId(institutionId).stream().map(this::initializeEquipment).toList();
    }

    @Override
    public List<Equipment> listEquipmentByDepartment(Long departmentId) {
        if (departmentId == null) {
            throw new InvalidOperationException("Department ID cannot be null");
        }
        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department", "id", departmentId);
        }
        return equipmentRepository.findByDepartmentId(departmentId).stream().map(this::initializeEquipment).toList();
    }

    @Override
    public List<Equipment> listEquipmentByCategory(Long categoryId) {
        if (categoryId == null) {
            throw new InvalidOperationException("Category ID cannot be null");
        }
        if (!equipmentCategoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("EquipmentCategory", "id", categoryId);
        }
        return equipmentRepository.findByCategoryId(categoryId).stream().map(this::initializeEquipment).toList();
    }

    @Override
    public List<Equipment> listEquipmentByInstitutionAndStatus(Long institutionId, EquipmentStatus status) {
        if (institutionId == null) {
            throw new InvalidOperationException("Institution ID cannot be null");
        }
        if (!institutionRepository.existsById(institutionId)) {
            throw new ResourceNotFoundException("Institution", "id", institutionId);
        }
        return equipmentRepository.findByInstitutionIdAndStatus(institutionId, status).stream().map(this::initializeEquipment).toList();
    }

    @Override
    @Transactional
    public Equipment updateEquipment(Long id, Equipment updatedData, Long departmentId, Long categoryId, Long primaryLabManagerId) {
        if (updatedData == null) {
            throw new InvalidOperationException("Updated equipment data cannot be null");
        }
        Equipment existing = getEquipmentById(id);
        Long institutionId = existing.getInstitution().getId();

        // Reject cross-institution transfer
        if (updatedData.getInstitution() != null && updatedData.getInstitution().getId() != null
                && !updatedData.getInstitution().getId().equals(institutionId)) {
            throw new InvalidOperationException("Moving equipment to a different institution is not permitted");
        }

        if (departmentId != null && !departmentId.equals(existing.getDepartment().getId())) {
            Department newDepartment = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", departmentId));
            if (!newDepartment.getInstitution().getId().equals(institutionId)) {
                throw new InvalidOperationException(String.format(
                        "Department with id %d does not belong to the equipment's institution (id %d)", departmentId, institutionId));
            }
            existing.setDepartment(newDepartment);
        }

        if (categoryId != null && !categoryId.equals(existing.getCategory().getId())) {
            EquipmentCategory newCategory = equipmentCategoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("EquipmentCategory", "id", categoryId));
            existing.setCategory(newCategory);
        }

        if (primaryLabManagerId != null) {
            User manager = userRepository.findById(primaryLabManagerId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", primaryLabManagerId));
            if (!manager.getInstitution().getId().equals(institutionId)) {
                throw new InvalidOperationException(String.format(
                        "Primary lab manager with id %d does not belong to institution with id %d", primaryLabManagerId, institutionId));
            }
            existing.setPrimaryLabManager(manager);
        }

        if (updatedData.getAssetTag() != null && !updatedData.getAssetTag().trim().isEmpty()) {
            String newAssetTag = updatedData.getAssetTag().trim().toUpperCase();
            if (!newAssetTag.equalsIgnoreCase(existing.getAssetTag()) && equipmentRepository.existsByAssetTag(newAssetTag)) {
                throw new DuplicateResourceException("Equipment", "assetTag", newAssetTag);
            }
            existing.setAssetTag(newAssetTag);
        }

        if (updatedData.getSerialNumber() != null && !updatedData.getSerialNumber().trim().isEmpty()) {
            String newSerialNumber = updatedData.getSerialNumber().trim().toUpperCase();
            if (!newSerialNumber.equalsIgnoreCase(existing.getSerialNumber()) && equipmentRepository.existsBySerialNumber(newSerialNumber)) {
                throw new DuplicateResourceException("Equipment", "serialNumber", newSerialNumber);
            }
            existing.setSerialNumber(newSerialNumber);
        }

        if (updatedData.getName() != null && !updatedData.getName().trim().isEmpty()) {
            existing.setName(updatedData.getName().trim());
        }
        if (updatedData.getModelNumber() != null) {
            existing.setModelNumber(updatedData.getModelNumber());
        }
        if (updatedData.getManufacturer() != null) {
            existing.setManufacturer(updatedData.getManufacturer());
        }
        if (updatedData.getLocationBuilding() != null) {
            existing.setLocationBuilding(updatedData.getLocationBuilding());
        }
        if (updatedData.getLocationRoom() != null) {
            existing.setLocationRoom(updatedData.getLocationRoom());
        }
        if (updatedData.getHourlyRateInternal() != null) {
            existing.setHourlyRateInternal(updatedData.getHourlyRateInternal());
        }
        if (updatedData.getHourlyRateExternal() != null) {
            existing.setHourlyRateExternal(updatedData.getHourlyRateExternal());
        }
        if (updatedData.getMinBookingDurationMins() != null) {
            existing.setMinBookingDurationMins(updatedData.getMinBookingDurationMins());
        }
        if (updatedData.getMaxBookingDurationMins() != null) {
            existing.setMaxBookingDurationMins(updatedData.getMaxBookingDurationMins());
        }
        if (updatedData.getBufferTimeMins() != null) {
            existing.setBufferTimeMins(updatedData.getBufferTimeMins());
        }
        existing.setRequiresTrainingCertification(updatedData.isRequiresTrainingCertification());
        existing.setRequiresApproval(updatedData.isRequiresApproval());
        existing.setShareableExternally(updatedData.isShareableExternally());

        if (updatedData.getPurchaseDate() != null) {
            existing.setPurchaseDate(updatedData.getPurchaseDate());
        }
        if (updatedData.getPurchaseCost() != null) {
            existing.setPurchaseCost(updatedData.getPurchaseCost());
        }
        if (updatedData.getWarrantyExpiryDate() != null) {
            existing.setWarrantyExpiryDate(updatedData.getWarrantyExpiryDate());
        }

        return initializeEquipment(equipmentRepository.save(existing));
    }

    @Override
    @Transactional
    public Equipment updateOperationalStatus(Long id, EquipmentStatus newStatus, String reason) {
        if (newStatus == null) {
            throw new InvalidOperationException("New equipment status cannot be null");
        }
        Equipment existing = getEquipmentById(id);

        if (existing.getStatus() == EquipmentStatus.RETIRED && newStatus != EquipmentStatus.RETIRED) {
            throw new InvalidOperationException("Cannot reactivate or change status of RETIRED equipment");
        }

        existing.setStatus(newStatus);
        if (reason != null) {
            existing.setOperationalStatusReason(reason);
        }

        return initializeEquipment(equipmentRepository.save(existing));
    }

    @Override
    @Transactional
    public void activateEquipment(Long id) {
        Equipment existing = getEquipmentById(id);
        if (existing.getStatus() == EquipmentStatus.RETIRED) {
            throw new InvalidOperationException("Cannot activate RETIRED equipment");
        }
        existing.setDeletedAt(null);
        if (existing.getStatus() == EquipmentStatus.OUT_OF_SERVICE) {
            existing.setStatus(EquipmentStatus.AVAILABLE);
        }
        equipmentRepository.save(existing);
    }

    @Override
    @Transactional
    public void deactivateEquipment(Long id) {
        Equipment existing = getEquipmentById(id);
        existing.setDeletedAt(Instant.now());
        if (existing.getStatus() != EquipmentStatus.RETIRED) {
            existing.setStatus(EquipmentStatus.OUT_OF_SERVICE);
        }
        equipmentRepository.save(existing);
    }
}
