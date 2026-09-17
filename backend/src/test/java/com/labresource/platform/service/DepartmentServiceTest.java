package com.labresource.platform.service;

import com.labresource.platform.common.exception.DuplicateResourceException;
import com.labresource.platform.common.exception.InvalidOperationException;
import com.labresource.platform.common.exception.ResourceNotFoundException;
import com.labresource.platform.department.Department;
import com.labresource.platform.department.repository.DepartmentRepository;
import com.labresource.platform.department.service.DepartmentServiceImpl;
import com.labresource.platform.institution.Institution;
import com.labresource.platform.institution.repository.InstitutionRepository;
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
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private InstitutionRepository institutionRepository;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    private Institution institution;
    private Department department;

    @BeforeEach
    void setUp() {
        institution = new Institution();
        institution.setId(1L);
        institution.setName("MIT");
        institution.setCode("MIT");

        department = new Department();
        department.setId(10L);
        department.setCode("PHYS");
        department.setName("Physics Department");
        department.setInstitution(institution);
    }

    @Test
    void createDepartment_success() {
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(institution));
        when(departmentRepository.existsByInstitutionIdAndCode(1L, "PHYS")).thenReturn(false);
        when(departmentRepository.findByInstitutionIdAndName(1L, "Physics Department")).thenReturn(Optional.empty());
        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Department created = departmentService.createDepartment(1L, department);

        assertNotNull(created);
        assertEquals("PHYS", created.getCode());
        assertEquals(institution, created.getInstitution());
        verify(departmentRepository).save(department);
    }

    @Test
    void createDepartment_nonexistentInstitution_throwsException() {
        when(institutionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> departmentService.createDepartment(99L, department));
        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    void createDepartment_duplicateCodeInSameInstitution_throwsException() {
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(institution));
        when(departmentRepository.existsByInstitutionIdAndCode(1L, "PHYS")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> departmentService.createDepartment(1L, department));
        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    void updateDepartment_crossInstitutionMove_throwsException() {
        Institution otherInstitution = new Institution();
        otherInstitution.setId(2L);
        otherInstitution.setName("Stanford");

        Department updatedData = new Department();
        updatedData.setInstitution(otherInstitution);

        when(departmentRepository.findById(10L)).thenReturn(Optional.of(department));

        assertThrows(InvalidOperationException.class, () -> departmentService.updateDepartment(10L, updatedData));
        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    void getDepartmentById_notFound_throwsException() {
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> departmentService.getDepartmentById(999L));
    }
}
