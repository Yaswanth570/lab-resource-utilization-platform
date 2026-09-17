package com.labresource.platform.service;

import com.labresource.platform.common.exception.DuplicateResourceException;
import com.labresource.platform.common.exception.InvalidOperationException;
import com.labresource.platform.common.exception.ResourceNotFoundException;
import com.labresource.platform.institution.Institution;
import com.labresource.platform.institution.repository.InstitutionRepository;
import com.labresource.platform.institution.service.InstitutionServiceImpl;
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
class InstitutionServiceTest {

    @Mock
    private InstitutionRepository institutionRepository;

    @InjectMocks
    private InstitutionServiceImpl institutionService;

    private Institution sampleInstitution;

    @BeforeEach
    void setUp() {
        sampleInstitution = new Institution();
        sampleInstitution.setId(1L);
        sampleInstitution.setCode("MIT");
        sampleInstitution.setName("Massachusetts Institute of Technology");
        sampleInstitution.setCountry("USA");
        sampleInstitution.setContactEmail("admin@mit.edu");
    }

    @Test
    void createInstitution_success() {
        when(institutionRepository.existsByCode("MIT")).thenReturn(false);
        when(institutionRepository.existsByName("Massachusetts Institute of Technology")).thenReturn(false);
        when(institutionRepository.save(any(Institution.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Institution created = institutionService.createInstitution(sampleInstitution);

        assertNotNull(created);
        assertEquals("MIT", created.getCode());
        verify(institutionRepository).save(any(Institution.class));
    }

    @Test
    void createInstitution_duplicateCode_throwsException() {
        when(institutionRepository.existsByCode("MIT")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> institutionService.createInstitution(sampleInstitution));
        verify(institutionRepository, never()).save(any(Institution.class));
    }

    @Test
    void createInstitution_duplicateName_throwsException() {
        when(institutionRepository.existsByCode("MIT")).thenReturn(false);
        when(institutionRepository.existsByName("Massachusetts Institute of Technology")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> institutionService.createInstitution(sampleInstitution));
        verify(institutionRepository, never()).save(any(Institution.class));
    }

    @Test
    void createInstitution_missingCode_throwsException() {
        sampleInstitution.setCode(null);

        assertThrows(InvalidOperationException.class, () -> institutionService.createInstitution(sampleInstitution));
    }

    @Test
    void getInstitutionById_notFound_throwsException() {
        when(institutionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> institutionService.getInstitutionById(99L));
    }

    @Test
    void getInstitutionById_success() {
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(sampleInstitution));

        Institution found = institutionService.getInstitutionById(1L);

        assertNotNull(found);
        assertEquals(1L, found.getId());
    }

    @Test
    void deactivateInstitution_success() {
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(sampleInstitution));
        when(institutionRepository.save(any(Institution.class))).thenAnswer(invocation -> invocation.getArgument(0));

        institutionService.deactivateInstitution(1L);

        assertFalse(sampleInstitution.isActive());
        verify(institutionRepository).save(sampleInstitution);
    }
}
