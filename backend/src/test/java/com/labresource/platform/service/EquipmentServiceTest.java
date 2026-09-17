package com.labresource.platform.service;

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
import com.labresource.platform.equipment.service.EquipmentServiceImpl;
import com.labresource.platform.institution.Institution;
import com.labresource.platform.institution.repository.InstitutionRepository;
import com.labresource.platform.user.repository.UserRepository;
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
class EquipmentServiceTest {

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private InstitutionRepository institutionRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private EquipmentCategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EquipmentServiceImpl equipmentService;

    private Institution institutionA;
    private Institution institutionB;
    private Department departmentA;
    private Department departmentB;
    private EquipmentCategory category;
    private Equipment equipment;

    @BeforeEach
    void setUp() {
        institutionA = new Institution();
        institutionA.setId(1L);
        institutionA.setName("MIT");

        institutionB = new Institution();
        institutionB.setId(2L);
        institutionB.setName("Harvard");

        departmentA = new Department();
        departmentA.setId(10L);
        departmentA.setName("Physics Dept");
        departmentA.setInstitution(institutionA);

        departmentB = new Department();
        departmentB.setId(20L);
        departmentB.setName("Biology Dept");
        departmentB.setInstitution(institutionB);

        category = new EquipmentCategory("Microscopy", "Electron Microscopes");
        category.setId(5L);

        equipment = new Equipment();
        equipment.setId(100L);
        equipment.setName("Transmission Electron Microscope");
        equipment.setAssetTag("EQ-MIT-001");
        equipment.setSerialNumber("SN-TEM-9988");
        equipment.setLocationBuilding("Building 13");
        equipment.setLocationRoom("Room 204");
    }

    @Test
    void createEquipment_success() {
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(institutionA));
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(departmentA));
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category));
        when(equipmentRepository.existsByAssetTag("EQ-MIT-001")).thenReturn(false);
        when(equipmentRepository.existsBySerialNumber("SN-TEM-9988")).thenReturn(false);
        when(equipmentRepository.save(any(Equipment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Equipment created = equipmentService.createEquipment(equipment, 1L, 10L, 5L, null);

        assertNotNull(created);
        assertEquals(institutionA, created.getInstitution());
        assertEquals(departmentA, created.getDepartment());
        assertEquals(category, created.getCategory());
        assertEquals(EquipmentStatus.AVAILABLE, created.getStatus());
        verify(equipmentRepository).save(equipment);
    }

    @Test
    void createEquipment_nonexistentInstitution_throwsException() {
        when(institutionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> equipmentService.createEquipment(equipment, 99L, 10L, 5L, null));
        verify(equipmentRepository, never()).save(any(Equipment.class));
    }

    @Test
    void createEquipment_nonexistentDepartment_throwsException() {
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(institutionA));
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> equipmentService.createEquipment(equipment, 1L, 99L, 5L, null));
        verify(equipmentRepository, never()).save(any(Equipment.class));
    }

    @Test
    void createEquipment_crossInstitutionDepartment_throwsException() {
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(institutionA));
        // departmentB belongs to institutionB (id 2)
        when(departmentRepository.findById(20L)).thenReturn(Optional.of(departmentB));

        InvalidOperationException ex = assertThrows(InvalidOperationException.class,
                () -> equipmentService.createEquipment(equipment, 1L, 20L, 5L, null));
        assertTrue(ex.getMessage().contains("does not belong to institution"));
        verify(equipmentRepository, never()).save(any(Equipment.class));
    }

    @Test
    void createEquipment_duplicateAssetTag_throwsException() {
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(institutionA));
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(departmentA));
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category));
        when(equipmentRepository.existsByAssetTag("EQ-MIT-001")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> equipmentService.createEquipment(equipment, 1L, 10L, 5L, null));
        verify(equipmentRepository, never()).save(any(Equipment.class));
    }

    @Test
    void createEquipment_duplicateSerialNumber_throwsException() {
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(institutionA));
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(departmentA));
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category));
        when(equipmentRepository.existsByAssetTag("EQ-MIT-001")).thenReturn(false);
        when(equipmentRepository.existsBySerialNumber("SN-TEM-9988")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> equipmentService.createEquipment(equipment, 1L, 10L, 5L, null));
        verify(equipmentRepository, never()).save(any(Equipment.class));
    }

    @Test
    void getEquipmentById_notFound_throwsException() {
        when(equipmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> equipmentService.getEquipmentById(999L));
    }

    @Test
    void updateOperationalStatus_validTransition_success() {
        equipment.setStatus(EquipmentStatus.AVAILABLE);
        when(equipmentRepository.findById(100L)).thenReturn(Optional.of(equipment));
        when(equipmentRepository.save(any(Equipment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Equipment updated = equipmentService.updateOperationalStatus(100L, EquipmentStatus.UNDER_MAINTENANCE, "Routine check");

        assertEquals(EquipmentStatus.UNDER_MAINTENANCE, updated.getStatus());
        assertEquals("Routine check", updated.getOperationalStatusReason());
        verify(equipmentRepository).save(equipment);
    }

    @Test
    void updateOperationalStatus_retiredReactivation_throwsException() {
        equipment.setStatus(EquipmentStatus.RETIRED);
        when(equipmentRepository.findById(100L)).thenReturn(Optional.of(equipment));

        assertThrows(InvalidOperationException.class,
                () -> equipmentService.updateOperationalStatus(100L, EquipmentStatus.AVAILABLE, "Reactivating"));
        verify(equipmentRepository, never()).save(any(Equipment.class));
    }

    @Test
    void deactivateEquipment_success() {
        equipment.setStatus(EquipmentStatus.AVAILABLE);
        when(equipmentRepository.findById(100L)).thenReturn(Optional.of(equipment));
        when(equipmentRepository.save(any(Equipment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        equipmentService.deactivateEquipment(100L);

        assertEquals(EquipmentStatus.OUT_OF_SERVICE, equipment.getStatus());
        assertNotNull(equipment.getDeletedAt());
        verify(equipmentRepository).save(equipment);
    }
}
