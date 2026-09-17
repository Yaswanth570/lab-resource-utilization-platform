package com.labresource.platform.equipment.service;

import com.labresource.platform.equipment.EquipmentCategory;

import java.util.List;

public interface EquipmentCategoryService {

    EquipmentCategory createCategory(EquipmentCategory category);

    EquipmentCategory getCategoryById(Long id);

    EquipmentCategory getCategoryByName(String name);

    List<EquipmentCategory> listCategories();

    EquipmentCategory updateCategory(Long id, EquipmentCategory updatedData);
}
