package com.labresource.platform.service;

import com.labresource.platform.common.exception.DuplicateResourceException;
import com.labresource.platform.common.exception.InvalidOperationException;
import com.labresource.platform.common.exception.ResourceNotFoundException;
import com.labresource.platform.equipment.EquipmentCategory;
import com.labresource.platform.equipment.repository.EquipmentCategoryRepository;
import com.labresource.platform.equipment.service.EquipmentCategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipmentCategoryServiceTest {

    @Mock
    private EquipmentCategoryRepository categoryRepository;

    @InjectMocks
    private EquipmentCategoryServiceImpl categoryService;

    private EquipmentCategory category;

    @BeforeEach
    void setUp() {
        category = new EquipmentCategory("Spectroscopy", "Optical and mass spectrometers");
        category.setId(1L);
    }

    @Test
    void createCategory_success() {
        when(categoryRepository.existsByName("Spectroscopy")).thenReturn(false);
        when(categoryRepository.save(any(EquipmentCategory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EquipmentCategory created = categoryService.createCategory(category);

        assertNotNull(created);
        assertEquals("Spectroscopy", created.getName());
        verify(categoryRepository).save(category);
    }

    @Test
    void createCategory_duplicateName_throwsException() {
        when(categoryRepository.existsByName("Spectroscopy")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> categoryService.createCategory(category));
        verify(categoryRepository, never()).save(any(EquipmentCategory.class));
    }

    @Test
    void getCategoryById_notFound_throwsException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.getCategoryById(99L));
    }

    @Test
    void updateCategory_success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(EquipmentCategory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EquipmentCategory updatedData = new EquipmentCategory();
        updatedData.setDescription("Updated description");

        EquipmentCategory updated = categoryService.updateCategory(1L, updatedData);

        assertEquals("Updated description", updated.getDescription());
        verify(categoryRepository).save(category);
    }
}
