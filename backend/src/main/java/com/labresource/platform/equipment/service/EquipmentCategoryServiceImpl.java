package com.labresource.platform.equipment.service;

import com.labresource.platform.common.exception.DuplicateResourceException;
import com.labresource.platform.common.exception.InvalidOperationException;
import com.labresource.platform.common.exception.ResourceNotFoundException;
import com.labresource.platform.equipment.EquipmentCategory;
import com.labresource.platform.equipment.repository.EquipmentCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class EquipmentCategoryServiceImpl implements EquipmentCategoryService {

    private final EquipmentCategoryRepository equipmentCategoryRepository;

    public EquipmentCategoryServiceImpl(EquipmentCategoryRepository equipmentCategoryRepository) {
        this.equipmentCategoryRepository = equipmentCategoryRepository;
    }

    @Override
    @Transactional
    public EquipmentCategory createCategory(EquipmentCategory category) {
        if (category == null) {
            throw new InvalidOperationException("Equipment category payload cannot be null");
        }
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new InvalidOperationException("Equipment category name is required");
        }

        String normalizedName = category.getName().trim();
        if (equipmentCategoryRepository.existsByName(normalizedName)) {
            throw new DuplicateResourceException("EquipmentCategory", "name", normalizedName);
        }

        category.setName(normalizedName);
        return equipmentCategoryRepository.save(category);
    }

    @Override
    public EquipmentCategory getCategoryById(Long id) {
        if (id == null) {
            throw new InvalidOperationException("Category ID cannot be null");
        }
        return equipmentCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EquipmentCategory", "id", id));
    }

    @Override
    public EquipmentCategory getCategoryByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidOperationException("Category name cannot be null or blank");
        }
        return equipmentCategoryRepository.findByName(name.trim())
                .orElseThrow(() -> new ResourceNotFoundException("EquipmentCategory", "name", name.trim()));
    }

    @Override
    public List<EquipmentCategory> listCategories() {
        return equipmentCategoryRepository.findAll();
    }

    @Override
    @Transactional
    public EquipmentCategory updateCategory(Long id, EquipmentCategory updatedData) {
        if (updatedData == null) {
            throw new InvalidOperationException("Updated category data cannot be null");
        }
        EquipmentCategory existing = getCategoryById(id);

        if (updatedData.getName() != null && !updatedData.getName().trim().isEmpty()) {
            String newName = updatedData.getName().trim();
            if (!newName.equalsIgnoreCase(existing.getName()) && equipmentCategoryRepository.existsByName(newName)) {
                throw new DuplicateResourceException("EquipmentCategory", "name", newName);
            }
            existing.setName(newName);
        }

        if (updatedData.getDescription() != null) {
            existing.setDescription(updatedData.getDescription());
        }

        return equipmentCategoryRepository.save(existing);
    }
}
