package com.labresource.platform.equipment.service;

import com.labresource.platform.common.exception.DuplicateResourceException;
import com.labresource.platform.common.exception.InvalidOperationException;
import com.labresource.platform.common.exception.ResourceNotFoundException;
import com.labresource.platform.equipment.Equipment;
import com.labresource.platform.equipment.EquipmentSpecification;
import com.labresource.platform.equipment.repository.EquipmentRepository;
import com.labresource.platform.equipment.repository.EquipmentSpecificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class EquipmentSpecificationServiceImpl implements EquipmentSpecificationService {

    private final EquipmentSpecificationRepository equipmentSpecificationRepository;
    private final EquipmentRepository equipmentRepository;

    public EquipmentSpecificationServiceImpl(EquipmentSpecificationRepository equipmentSpecificationRepository,
                                             EquipmentRepository equipmentRepository) {
        this.equipmentSpecificationRepository = equipmentSpecificationRepository;
        this.equipmentRepository = equipmentRepository;
    }

    @Override
    @Transactional
    public EquipmentSpecification addSpecification(Long equipmentId, EquipmentSpecification spec) {
        if (equipmentId == null) {
            throw new InvalidOperationException("Equipment ID is required");
        }
        if (spec == null) {
            throw new InvalidOperationException("Equipment specification payload cannot be null");
        }
        if (spec.getSpecName() == null || spec.getSpecName().trim().isEmpty()) {
            throw new InvalidOperationException("Specification name is required");
        }
        if (spec.getSpecValue() == null || spec.getSpecValue().trim().isEmpty()) {
            throw new InvalidOperationException("Specification value is required");
        }

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", equipmentId));

        String normalizedName = spec.getSpecName().trim();
        if (equipmentSpecificationRepository.findByEquipmentIdAndSpecName(equipmentId, normalizedName).isPresent()) {
            throw new DuplicateResourceException(String.format("Specification with name '%s' already exists for equipment %d",
                    normalizedName, equipmentId));
        }

        spec.setEquipment(equipment);
        spec.setSpecName(normalizedName);
        spec.setSpecValue(spec.getSpecValue().trim());

        return equipmentSpecificationRepository.save(spec);
    }

    @Override
    public EquipmentSpecification getSpecificationById(Long id) {
        if (id == null) {
            throw new InvalidOperationException("Specification ID cannot be null");
        }
        return equipmentSpecificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EquipmentSpecification", "id", id));
    }

    @Override
    public List<EquipmentSpecification> listSpecificationsByEquipment(Long equipmentId) {
        if (equipmentId == null) {
            throw new InvalidOperationException("Equipment ID cannot be null");
        }
        if (!equipmentRepository.existsById(equipmentId)) {
            throw new ResourceNotFoundException("Equipment", "id", equipmentId);
        }
        return equipmentSpecificationRepository.findByEquipmentId(equipmentId);
    }

    @Override
    public EquipmentSpecification getSpecificationByEquipmentAndName(Long equipmentId, String specName) {
        if (equipmentId == null) {
            throw new InvalidOperationException("Equipment ID cannot be null");
        }
        if (specName == null || specName.trim().isEmpty()) {
            throw new InvalidOperationException("Specification name cannot be null or blank");
        }
        if (!equipmentRepository.existsById(equipmentId)) {
            throw new ResourceNotFoundException("Equipment", "id", equipmentId);
        }
        return equipmentSpecificationRepository.findByEquipmentIdAndSpecName(equipmentId, specName.trim())
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Specification '%s' not found for equipment %d",
                        specName.trim(), equipmentId)));
    }

    @Override
    @Transactional
    public EquipmentSpecification updateSpecification(Long id, EquipmentSpecification updatedData) {
        if (updatedData == null) {
            throw new InvalidOperationException("Updated specification data cannot be null");
        }
        EquipmentSpecification existing = getSpecificationById(id);
        Long equipmentId = existing.getEquipment().getId();

        if (updatedData.getSpecName() != null && !updatedData.getSpecName().trim().isEmpty()) {
            String newName = updatedData.getSpecName().trim();
            if (!newName.equalsIgnoreCase(existing.getSpecName())) {
                equipmentSpecificationRepository.findByEquipmentIdAndSpecName(equipmentId, newName).ifPresent(s -> {
                    throw new DuplicateResourceException(String.format("Specification with name '%s' already exists for equipment %d",
                            newName, equipmentId));
                });
                existing.setSpecName(newName);
            }
        }

        if (updatedData.getSpecValue() != null && !updatedData.getSpecValue().trim().isEmpty()) {
            existing.setSpecValue(updatedData.getSpecValue().trim());
        }

        if (updatedData.getUnit() != null) {
            existing.setUnit(updatedData.getUnit().trim());
        }

        return equipmentSpecificationRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteSpecification(Long id) {
        EquipmentSpecification existing = getSpecificationById(id);
        equipmentSpecificationRepository.delete(existing);
    }
}
