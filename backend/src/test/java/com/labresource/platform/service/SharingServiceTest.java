package com.labresource.platform.service;

import com.labresource.platform.common.exception.DuplicateResourceException;
import com.labresource.platform.common.exception.InvalidOperationException;
import com.labresource.platform.common.exception.ResourceNotFoundException;
import com.labresource.platform.equipment.Equipment;
import com.labresource.platform.equipment.EquipmentStatus;
import com.labresource.platform.equipment.repository.EquipmentRepository;
import com.labresource.platform.institution.Institution;
import com.labresource.platform.institution.repository.InstitutionRepository;
import com.labresource.platform.sharing.*;
import com.labresource.platform.sharing.repository.ResourceSharingAgreementRepository;
import com.labresource.platform.sharing.repository.ResourceSharingRequestRepository;
import com.labresource.platform.sharing.repository.SharedEquipmentAllocationRepository;
import com.labresource.platform.sharing.service.SharingServiceImpl;
import com.labresource.platform.user.User;
import com.labresource.platform.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SharingServiceTest {

    @Mock
    private ResourceSharingAgreementRepository agreementRepository;

    @Mock
    private SharedEquipmentAllocationRepository allocationRepository;

    @Mock
    private ResourceSharingRequestRepository requestRepository;

    @Mock
    private InstitutionRepository institutionRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SharingServiceImpl sharingService;

    private Institution ownerInst;
    private Institution reqInst;
    private Equipment shareableEquip;
    private Equipment nonShareableEquip;
    private ResourceSharingAgreement activeAgreement;

    @BeforeEach
    void setUp() {
        ownerInst = new Institution();
        ownerInst.setId(10L);
        ownerInst.setName("Host Institute");

        reqInst = new Institution();
        reqInst.setId(20L);
        reqInst.setName("Partner Institute");

        shareableEquip = new Equipment();
        shareableEquip.setId(100L);
        shareableEquip.setName("NMR Spectrometer");
        shareableEquip.setInstitution(ownerInst);
        shareableEquip.setShareableExternally(true);
        shareableEquip.setStatus(EquipmentStatus.AVAILABLE);

        nonShareableEquip = new Equipment();
        nonShareableEquip.setId(101L);
        nonShareableEquip.setName("Confidential Prototype");
        nonShareableEquip.setInstitution(ownerInst);
        nonShareableEquip.setShareableExternally(false);

        activeAgreement = new ResourceSharingAgreement();
        activeAgreement.setId(1L);
        activeAgreement.setAgreementCode("AGR-TEST-001");
        activeAgreement.setOwnerInstitution(ownerInst);
        activeAgreement.setRequestingInstitution(reqInst);
        activeAgreement.setStartDate(LocalDate.now().minusDays(10));
        activeAgreement.setEndDate(LocalDate.now().plusDays(90));
        activeAgreement.setStatus(SharingAgreementStatus.ACTIVE);
    }

    // ==========================================
    // Agreement Domain Logic Tests
    // ==========================================

    @Test
    @DisplayName("Create agreement with same owner and requester throws InvalidOperationException")
    void testCreateAgreementSameInstitution() {
        ResourceSharingAgreement ag = new ResourceSharingAgreement();
        ag.setStartDate(LocalDate.now());
        ag.setEndDate(LocalDate.now().plusDays(30));

        assertThrows(InvalidOperationException.class, () ->
                sharingService.createAgreement(ag, 10L, 10L, null));
    }

    @Test
    @DisplayName("Create agreement with end date before start date throws InvalidOperationException")
    void testCreateAgreementInvalidDates() {
        when(institutionRepository.findById(20L)).thenReturn(Optional.of(reqInst));
        when(institutionRepository.findById(10L)).thenReturn(Optional.of(ownerInst));

        ResourceSharingAgreement ag = new ResourceSharingAgreement();
        ag.setStartDate(LocalDate.now().plusDays(10));
        ag.setEndDate(LocalDate.now().minusDays(1)); // Invalid

        assertThrows(InvalidOperationException.class, () ->
                sharingService.createAgreement(ag, 20L, 10L, null));
    }

    @Test
    @DisplayName("Create agreement with duplicate code throws DuplicateResourceException")
    void testCreateAgreementDuplicateCode() {
        when(institutionRepository.findById(20L)).thenReturn(Optional.of(reqInst));
        when(institutionRepository.findById(10L)).thenReturn(Optional.of(ownerInst));
        when(agreementRepository.existsByAgreementCode("AGR-EXISTING")).thenReturn(true);

        ResourceSharingAgreement ag = new ResourceSharingAgreement();
        ag.setAgreementCode("AGR-EXISTING");
        ag.setStartDate(LocalDate.now());
        ag.setEndDate(LocalDate.now().plusDays(30));

        assertThrows(DuplicateResourceException.class, () ->
                sharingService.createAgreement(ag, 20L, 10L, null));
    }

    @Test
    @DisplayName("Create agreement auto-provisions backing request and succeeds")
    void testCreateAgreementAutoRequestSuccess() {
        when(institutionRepository.findById(20L)).thenReturn(Optional.of(reqInst));
        when(institutionRepository.findById(10L)).thenReturn(Optional.of(ownerInst));
        when(agreementRepository.existsByAgreementCode(any())).thenReturn(false);

        User testUser = new User();
        testUser.setId(5L);
        when(userRepository.findByInstitutionId(20L)).thenReturn(List.of(testUser));

        ResourceSharingRequest savedReq = new ResourceSharingRequest();
        savedReq.setId(55L);
        when(requestRepository.save(any(ResourceSharingRequest.class))).thenReturn(savedReq);

        when(agreementRepository.save(any(ResourceSharingAgreement.class))).thenAnswer(inv -> {
            ResourceSharingAgreement a = inv.getArgument(0);
            a.setId(99L);
            return a;
        });

        ResourceSharingAgreement ag = new ResourceSharingAgreement();
        ag.setStartDate(LocalDate.now());
        ag.setEndDate(LocalDate.now().plusDays(60));
        ag.setBillingRateMultiplier(new BigDecimal("1.20"));

        ResourceSharingAgreement created = sharingService.createAgreement(ag, 20L, 10L, null);

        assertNotNull(created);
        assertEquals(99L, created.getId());
        assertEquals(SharingAgreementStatus.ACTIVE, created.getStatus());
        verify(requestRepository).save(any(ResourceSharingRequest.class));
    }

    // ==========================================
    // Agreement Status Lifecycle Tests
    // ==========================================

    @Test
    @DisplayName("Transition ACTIVE -> SUSPENDED -> ACTIVE succeeds")
    void testAgreementStatusTransitions() {
        when(agreementRepository.findById(1L)).thenReturn(Optional.of(activeAgreement));
        when(agreementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ResourceSharingAgreement suspended = sharingService.updateAgreementStatus(1L, SharingAgreementStatus.SUSPENDED, 10L);
        assertEquals(SharingAgreementStatus.SUSPENDED, suspended.getStatus());

        ResourceSharingAgreement reactivated = sharingService.updateAgreementStatus(1L, SharingAgreementStatus.ACTIVE, 10L);
        assertEquals(SharingAgreementStatus.ACTIVE, reactivated.getStatus());
    }

    @Test
    @DisplayName("Transition from TERMINATED throws InvalidOperationException")
    void testAgreementTerminatedCannotChange() {
        activeAgreement.setStatus(SharingAgreementStatus.TERMINATED);
        when(agreementRepository.findById(1L)).thenReturn(Optional.of(activeAgreement));

        assertThrows(InvalidOperationException.class, () ->
                sharingService.updateAgreementStatus(1L, SharingAgreementStatus.ACTIVE, 10L));
    }

    // ==========================================
    // Allocation Domain Integrity Tests
    // ==========================================

    @Test
    @DisplayName("Allocate non-shareable equipment throws InvalidOperationException")
    void testAllocateNonShareableEquipment() {
        when(agreementRepository.findById(1L)).thenReturn(Optional.of(activeAgreement));
        when(equipmentRepository.findById(101L)).thenReturn(Optional.of(nonShareableEquip));

        assertThrows(InvalidOperationException.class, () ->
                sharingService.createAllocation(1L, 101L, null, true, 10L));
    }

    @Test
    @DisplayName("Allocate equipment to inactive agreement throws InvalidOperationException")
    void testAllocateToInactiveAgreement() {
        activeAgreement.setStatus(SharingAgreementStatus.SUSPENDED);
        when(agreementRepository.findById(1L)).thenReturn(Optional.of(activeAgreement));

        assertThrows(InvalidOperationException.class, () ->
                sharingService.createAllocation(1L, 100L, null, true, 10L));
    }

    @Test
    @DisplayName("Allocate equipment with institution mismatch throws InvalidOperationException")
    void testAllocateInstitutionMismatch() {
        Institution otherInst = new Institution();
        otherInst.setId(999L);
        shareableEquip.setInstitution(otherInst);

        when(agreementRepository.findById(1L)).thenReturn(Optional.of(activeAgreement));
        when(equipmentRepository.findById(100L)).thenReturn(Optional.of(shareableEquip));

        assertThrows(InvalidOperationException.class, () ->
                sharingService.createAllocation(1L, 100L, null, true, 10L));
    }

    @Test
    @DisplayName("Duplicate equipment allocation throws DuplicateResourceException")
    void testDuplicateAllocationThrows() {
        when(agreementRepository.findById(1L)).thenReturn(Optional.of(activeAgreement));
        when(equipmentRepository.findById(100L)).thenReturn(Optional.of(shareableEquip));
        when(allocationRepository.findBySharingAgreementIdAndEquipmentId(1L, 100L))
                .thenReturn(Optional.of(new SharedEquipmentAllocation()));

        assertThrows(DuplicateResourceException.class, () ->
                sharingService.createAllocation(1L, 100L, null, true, 10L));
    }

    @Test
    @DisplayName("Create valid allocation succeeds with custom rate")
    void testCreateValidAllocation() {
        when(agreementRepository.findById(1L)).thenReturn(Optional.of(activeAgreement));
        when(equipmentRepository.findById(100L)).thenReturn(Optional.of(shareableEquip));
        when(allocationRepository.findBySharingAgreementIdAndEquipmentId(1L, 100L))
                .thenReturn(Optional.empty());
        when(allocationRepository.save(any())).thenAnswer(inv -> {
            SharedEquipmentAllocation alloc = inv.getArgument(0);
            alloc.setId(777L);
            return alloc;
        });

        SharedEquipmentAllocation created = sharingService.createAllocation(1L, 100L, new BigDecimal("125.00"), true, 10L);

        assertNotNull(created);
        assertEquals(777L, created.getId());
        assertEquals(new BigDecimal("125.00"), created.getCustomHourlyRate());
        assertTrue(created.isActive());
    }

    @Test
    @DisplayName("Toggle allocation active status succeeds")
    void testToggleAllocationActive() {
        SharedEquipmentAllocation alloc = new SharedEquipmentAllocation();
        alloc.setId(10L);
        alloc.setSharingAgreement(activeAgreement);
        alloc.setEquipment(shareableEquip);
        alloc.setActive(true);

        when(allocationRepository.findById(10L)).thenReturn(Optional.of(alloc));
        when(allocationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SharedEquipmentAllocation toggled = sharingService.updateAllocationActive(10L, false, 10L);
        assertFalse(toggled.isActive());
    }
}
