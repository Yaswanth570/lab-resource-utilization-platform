package com.labresource.platform.service;

import com.labresource.platform.common.exception.DuplicateResourceException;
import com.labresource.platform.common.exception.ResourceNotFoundException;
import com.labresource.platform.equipment.Equipment;
import com.labresource.platform.equipment.EquipmentSpecification;
import com.labresource.platform.equipment.repository.EquipmentRepository;
import com.labresource.platform.equipment.repository.EquipmentSpecificationRepository;
import com.labresource.platform.equipment.service.EquipmentSpecificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipmentSpecificationServiceTest {

    @Mock
    private EquipmentSpecificationRepository specificationRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

    @InjectMocks
    private EquipmentSpecificationServiceImpl specificationService;

    private Equipment equipment;
    private EquipmentSpecification specification;

    @BeforeEach
    void setUp() {
        equipment = new Equipment();
        equipment.setId(10L);
        equipment.setName("Spectrometer");

        specification = new EquipmentSpecification(equipment, "Wavelength Range", "200-900", "nm");
        specification.setId(100L);
    }

    @Test
    void addSpecification_success() {
        when(equipmentRepository.findById(10L)).thenReturn(Optional.of(equipment));
        when(specificationRepository.findByEquipmentIdAndSpecName(10L, "Wavelength Range")).thenReturn(Optional.empty());
        when(specificationRepository.save(any(EquipmentSpecification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EquipmentSpecification created = specificationService.addSpecification(10L, specification);

        assertNotNull(created);
        assertEquals("Wavelength Range", created.getSpecName());
        assertEquals("200-900", created.getSpecValue());
        assertEquals(equipment, created.getEquipment());
        verify(specificationRepository).save(specification);
    }

    @Test
    void addSpecification_nonexistentEquipment_throwsException() {
        when(equipmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> specificationService.addSpecification(99L, specification));
        verify(specificationRepository, never()).save(any(EquipmentSpecification.class));
    }

    @Test
    void addSpecification_duplicateSpecName_throwsException() {
        when(equipmentRepository.findById(10L)).thenReturn(Optional.of(equipment));
        when(specificationRepository.findByEquipmentIdAndSpecName(10L, "Wavelength Range")).thenReturn(Optional.of(specification));

        assertThrows(DuplicateResourceException.class,
                () -> specificationService.addSpecification(10L, specification));
        verify(specificationRepository, never()).save(any(EquipmentSpecification.class));
    }

    @Test
    void listSpecificationsByEquipment_success() {
        when(equipmentRepository.existsById(10L)).thenReturn(true);
        when(specificationRepository.findByEquipmentId(10L)).thenReturn(List.of(specification));

        List<EquipmentSpecification> specs = specificationService.listSpecificationsByEquipment(10L);

        assertEquals(1, specs.size());
        assertEquals("Wavelength Range", specs.get(0).getSpecName());
    }

    @Test
    void deleteSpecification_success() {
        when(specificationRepository.findById(100L)).thenReturn(Optional.of(specification));

        specificationService.deleteSpecification(100L);

        verify(specificationRepository).delete(specification);
    }
}
