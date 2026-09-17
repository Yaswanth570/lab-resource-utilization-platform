package com.labresource.platform.service;

import com.labresource.platform.common.exception.DuplicateResourceException;
import com.labresource.platform.common.exception.ResourceNotFoundException;
import com.labresource.platform.equipment.Equipment;
import com.labresource.platform.equipment.QualificationStatus;
import com.labresource.platform.equipment.UserEquipmentQualification;
import com.labresource.platform.equipment.repository.EquipmentRepository;
import com.labresource.platform.equipment.repository.UserEquipmentQualificationRepository;
import com.labresource.platform.equipment.service.UserEquipmentQualificationServiceImpl;
import com.labresource.platform.user.User;
import com.labresource.platform.user.repository.UserRepository;
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
class UserEquipmentQualificationServiceTest {

    @Mock
    private UserEquipmentQualificationRepository qualificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

    @InjectMocks
    private UserEquipmentQualificationServiceImpl qualificationService;

    private User user;
    private User certifier;
    private Equipment equipment;
    private UserEquipmentQualification qualification;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@mit.edu");

        certifier = new User();
        certifier.setId(2L);
        certifier.setEmail("trainer@mit.edu");

        equipment = new Equipment();
        equipment.setId(10L);
        equipment.setName("Laser Cutter");

        qualification = new UserEquipmentQualification();
        qualification.setId(100L);
        qualification.setUser(user);
        qualification.setEquipment(equipment);
        qualification.setCertifiedBy(certifier);
    }

    @Test
    void createQualification_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(equipmentRepository.findById(10L)).thenReturn(Optional.of(equipment));
        when(userRepository.findById(2L)).thenReturn(Optional.of(certifier));
        when(qualificationRepository.findByUserIdAndEquipmentId(1L, 10L)).thenReturn(Optional.empty());
        when(qualificationRepository.save(any(UserEquipmentQualification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserEquipmentQualification created = qualificationService.createQualification(qualification, 1L, 10L, 2L);

        assertNotNull(created);
        assertEquals(user, created.getUser());
        assertEquals(equipment, created.getEquipment());
        assertEquals(certifier, created.getCertifiedBy());
        assertEquals(QualificationStatus.ACTIVE, created.getStatus());
        verify(qualificationRepository).save(qualification);
    }

    @Test
    void createQualification_nonexistentUser_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> qualificationService.createQualification(qualification, 99L, 10L, 2L));
        verify(qualificationRepository, never()).save(any(UserEquipmentQualification.class));
    }

    @Test
    void createQualification_nonexistentEquipment_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(equipmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> qualificationService.createQualification(qualification, 1L, 99L, 2L));
        verify(qualificationRepository, never()).save(any(UserEquipmentQualification.class));
    }

    @Test
    void createQualification_duplicate_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(equipmentRepository.findById(10L)).thenReturn(Optional.of(equipment));
        when(userRepository.findById(2L)).thenReturn(Optional.of(certifier));
        when(qualificationRepository.findByUserIdAndEquipmentId(1L, 10L)).thenReturn(Optional.of(qualification));

        assertThrows(DuplicateResourceException.class,
                () -> qualificationService.createQualification(qualification, 1L, 10L, 2L));
        verify(qualificationRepository, never()).save(any(UserEquipmentQualification.class));
    }

    @Test
    void listQualificationsByUser_success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(qualificationRepository.findByUserId(1L)).thenReturn(List.of(qualification));

        List<UserEquipmentQualification> list = qualificationService.listQualificationsByUser(1L);

        assertEquals(1, list.size());
        assertEquals(equipment, list.get(0).getEquipment());
    }

    @Test
    void updateQualificationStatus_success() {
        when(qualificationRepository.findById(100L)).thenReturn(Optional.of(qualification));
        when(qualificationRepository.save(any(UserEquipmentQualification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserEquipmentQualification updated = qualificationService.updateQualificationStatus(100L, QualificationStatus.EXPIRED, "Annual expiry");

        assertEquals(QualificationStatus.EXPIRED, updated.getStatus());
        assertEquals("Annual expiry", updated.getNotes());
        verify(qualificationRepository).save(qualification);
    }

    @Test
    void revokeQualification_success() {
        when(qualificationRepository.findById(100L)).thenReturn(Optional.of(qualification));
        when(qualificationRepository.save(any(UserEquipmentQualification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        qualificationService.revokeQualification(100L, "Safety violation");

        assertEquals(QualificationStatus.REVOKED, qualification.getStatus());
        assertTrue(qualification.getNotes().contains("Safety violation"));
        verify(qualificationRepository).save(qualification);
    }
}
