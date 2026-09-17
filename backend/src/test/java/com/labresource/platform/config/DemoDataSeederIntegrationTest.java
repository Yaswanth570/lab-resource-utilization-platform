package com.labresource.platform.config;

import com.labresource.platform.booking.Booking;
import com.labresource.platform.booking.BookingStatus;
import com.labresource.platform.booking.repository.BookingRepository;
import com.labresource.platform.cost.BillingInvoice;
import com.labresource.platform.cost.InvoiceStatus;
import com.labresource.platform.cost.repository.BillingInvoiceRepository;
import com.labresource.platform.department.Department;
import com.labresource.platform.department.repository.DepartmentRepository;
import com.labresource.platform.equipment.Equipment;
import com.labresource.platform.equipment.EquipmentStatus;
import com.labresource.platform.equipment.QualificationStatus;
import com.labresource.platform.equipment.UserEquipmentQualification;
import com.labresource.platform.equipment.repository.EquipmentRepository;
import com.labresource.platform.equipment.repository.UserEquipmentQualificationRepository;
import com.labresource.platform.institution.Institution;
import com.labresource.platform.institution.repository.InstitutionRepository;
import com.labresource.platform.maintenance.MaintenanceWorkOrder;
import com.labresource.platform.maintenance.WorkOrderStatus;
import com.labresource.platform.maintenance.repository.MaintenanceWorkOrderRepository;
import com.labresource.platform.notification.Notification;
import com.labresource.platform.notification.repository.NotificationRepository;
import com.labresource.platform.sharing.ResourceSharingAgreement;
import com.labresource.platform.sharing.SharingAgreementStatus;
import com.labresource.platform.sharing.repository.ResourceSharingAgreementRepository;
import com.labresource.platform.user.User;
import com.labresource.platform.user.UserRoleType;
import com.labresource.platform.user.repository.UserRepository;
import com.labresource.platform.utilization.repository.EquipmentIdleEventRepository;
import com.labresource.platform.utilization.repository.EquipmentUsageSessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@TestPropertySource(properties = {
        "jwt.secret=${JWT_SECRET:dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLW1pbi0zMi1ieXRlcw==}",
        "app.seed-demo-data=true"
})
class DemoDataSeederIntegrationTest {

    @Autowired
    private DemoDataSeeder demoDataSeeder;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private UserEquipmentQualificationRepository qualificationRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private EquipmentUsageSessionRepository sessionRepository;

    @Autowired
    private EquipmentIdleEventRepository idleEventRepository;

    @Autowired
    private MaintenanceWorkOrderRepository workOrderRepository;

    @Autowired
    private ResourceSharingAgreementRepository sharingAgreementRepository;

    @Autowired
    private BillingInvoiceRepository invoiceRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Verify comprehensive demo dataset seeding and strict idempotency")
    void testDemoDataSeedingAndIdempotency() {
        // First seed pass (runs automatically via ApplicationRunner, but explicit invocation ensures coverage)
        DemoDataSeeder.DemoDataSummary summary1 = demoDataSeeder.seedAll();

        assertNotNull(summary1);
        assertTrue(summary1.institutionCount() >= 2, "Expected at least APITR and GRIU institutions");
        assertTrue(summary1.departmentCount() >= 7, "Expected at least 7 departments across APITR and GRIU");
        assertTrue(summary1.userCount() >= 9, "Expected at least 9 users covering all 6 roles");
        assertTrue(summary1.equipmentCount() >= 25, "Expected at least 25 equipment items");
        assertTrue(summary1.bookingCount() >= 9, "Expected at least 9 bookings");
        assertTrue(summary1.sessionCount() >= 4, "Expected usage sessions");
        assertTrue(summary1.idleEventCount() >= 3, "Expected idle events");
        assertTrue(summary1.workOrderCount() >= 3, "Expected maintenance work orders");
        assertTrue(summary1.sharingAgreementCount() >= 1, "Expected resource sharing agreement");
        assertTrue(summary1.invoiceCount() >= 3, "Expected at least 3 invoices");
        assertTrue(summary1.notificationCount() >= 6, "Expected at least 6 notifications");

        // Verify Institutions
        Optional<Institution> apitrOpt = institutionRepository.findByCode("APITR");
        assertTrue(apitrOpt.isPresent(), "APITR institution must exist");
        Institution apitr = apitrOpt.get();
        assertEquals("Andhra Pradesh Institute of Technology & Research", apitr.getName());
        assertEquals("Guntur", apitr.getCity());

        Optional<Institution> griuOpt = institutionRepository.findByCode("GRIU");
        assertTrue(griuOpt.isPresent(), "GRIU partner institution must exist");

        // Verify Departments
        List<Department> apitrDepts = departmentRepository.findByInstitutionId(apitr.getId());
        assertTrue(apitrDepts.stream().anyMatch(d -> d.getCode().equals("CSE")));
        assertTrue(apitrDepts.stream().anyMatch(d -> d.getCode().equals("ECE")));
        assertTrue(apitrDepts.stream().anyMatch(d -> d.getCode().equals("EEE")));
        assertTrue(apitrDepts.stream().anyMatch(d -> d.getCode().equals("ME")));
        assertTrue(apitrDepts.stream().anyMatch(d -> d.getCode().equals("CE")));
        assertTrue(apitrDepts.stream().anyMatch(d -> d.getCode().equals("AI&DS")));

        // Verify Users and Roles
        Optional<User> sysAdmin = userRepository.findByEmail("sysadmin@apitr.edu");
        assertTrue(sysAdmin.isPresent());
        assertTrue(sysAdmin.get().getRoles().stream().anyMatch(r -> r.getName() == UserRoleType.ROLE_SYSTEM_ADMINISTRATOR));
        assertTrue(passwordEncoder.matches("Password123!", sysAdmin.get().getPasswordHash()));

        Optional<User> instAdmin = userRepository.findByEmail("admin@apitr.edu");
        assertTrue(instAdmin.isPresent());
        assertTrue(instAdmin.get().getRoles().stream().anyMatch(r -> r.getName() == UserRoleType.ROLE_INSTITUTION_ADMINISTRATOR));

        Optional<User> deptHead = userRepository.findByEmail("hod.cse@apitr.edu");
        assertTrue(deptHead.isPresent());
        assertTrue(deptHead.get().getRoles().stream().anyMatch(r -> r.getName() == UserRoleType.ROLE_DEPARTMENT_HEAD));

        Optional<User> labManager = userRepository.findByEmail("labmanager.cse@apitr.edu");
        assertTrue(labManager.isPresent());
        assertTrue(labManager.get().getRoles().stream().anyMatch(r -> r.getName() == UserRoleType.ROLE_LAB_MANAGER));

        Optional<User> labTech = userRepository.findByEmail("technician.cse@apitr.edu");
        assertTrue(labTech.isPresent());
        assertTrue(labTech.get().getRoles().stream().anyMatch(r -> r.getName() == UserRoleType.ROLE_LAB_TECHNICIAN));

        Optional<User> student = userRepository.findByEmail("student@apitr.edu");
        assertTrue(student.isPresent());
        assertTrue(student.get().getRoles().stream().anyMatch(r -> r.getName() == UserRoleType.ROLE_RESEARCHER_STUDENT));

        // Verify Equipment & Operational Statuses
        List<Equipment> equipmentList = equipmentRepository.findByInstitutionId(apitr.getId());
        assertTrue(equipmentList.stream().anyMatch(e -> e.getStatus() == EquipmentStatus.AVAILABLE));
        assertTrue(equipmentList.stream().anyMatch(e -> e.getStatus() == EquipmentStatus.IN_USE));
        assertTrue(equipmentList.stream().anyMatch(e -> e.getStatus() == EquipmentStatus.UNDER_MAINTENANCE));
        assertTrue(equipmentList.stream().anyMatch(e -> e.getStatus() == EquipmentStatus.OUT_OF_SERVICE));
        assertTrue(equipmentList.stream().anyMatch(e -> e.getStatus() == EquipmentStatus.RETIRED));

        // Verify Qualifications (Active & Expired rule)
        List<UserEquipmentQualification> quals = qualificationRepository.findAll();
        assertTrue(quals.stream().anyMatch(q -> q.getStatus() == QualificationStatus.ACTIVE));
        assertTrue(quals.stream().anyMatch(q -> q.getStatus() == QualificationStatus.EXPIRED));

        // Verify Booking Lifecycle Statuses
        List<Booking> bookings = bookingRepository.findByInstitutionId(apitr.getId());
        assertTrue(bookings.stream().anyMatch(b -> b.getStatus() == BookingStatus.PENDING_APPROVAL));
        assertTrue(bookings.stream().anyMatch(b -> b.getStatus() == BookingStatus.CONFIRMED));
        assertTrue(bookings.stream().anyMatch(b -> b.getStatus() == BookingStatus.IN_USE));
        assertTrue(bookings.stream().anyMatch(b -> b.getStatus() == BookingStatus.COMPLETED));
        assertTrue(bookings.stream().anyMatch(b -> b.getStatus() == BookingStatus.CANCELLED));
        assertTrue(bookings.stream().anyMatch(b -> b.getStatus() == BookingStatus.NO_SHOW));

        // Verify Resource Sharing
        Optional<ResourceSharingAgreement> rsaOpt = sharingAgreementRepository.findByAgreementCode("RSA-APITR-GRIU-2026-01");
        assertTrue(rsaOpt.isPresent());
        assertEquals(SharingAgreementStatus.ACTIVE, rsaOpt.get().getStatus());

        // Verify Work Orders
        List<MaintenanceWorkOrder> wos = workOrderRepository.findAll();
        assertTrue(wos.stream().anyMatch(w -> w.getStatus() == WorkOrderStatus.IN_PROGRESS));
        assertTrue(wos.stream().anyMatch(w -> w.getStatus() == WorkOrderStatus.WAITING_FOR_PARTS));
        assertTrue(wos.stream().anyMatch(w -> w.getStatus() == WorkOrderStatus.COMPLETED));

        // Verify Invoices
        List<BillingInvoice> invoices = invoiceRepository.findAll();
        assertTrue(invoices.stream().anyMatch(i -> i.getStatus() == InvoiceStatus.SETTLED));
        assertTrue(invoices.stream().anyMatch(i -> i.getStatus() == InvoiceStatus.ISSUED));
        assertTrue(invoices.stream().anyMatch(i -> i.getStatus() == InvoiceStatus.DRAFT));

        // Verify Notifications
        List<Notification> notifs = notificationRepository.findAll();
        assertTrue(notifs.stream().anyMatch(n -> !n.isRead()), "Should have unread notifications");
        assertTrue(notifs.stream().anyMatch(Notification::isRead), "Should have read notifications");

        // SECOND PASS: Idempotency Test
        DemoDataSeeder.DemoDataSummary summary2 = demoDataSeeder.seedAll();
        assertEquals(summary1.institutionCount(), summary2.institutionCount(), "Institution count must not change on re-seed");
        assertEquals(summary1.departmentCount(), summary2.departmentCount(), "Department count must not change on re-seed");
        assertEquals(summary1.userCount(), summary2.userCount(), "User count must not change on re-seed");
        assertEquals(summary1.equipmentCount(), summary2.equipmentCount(), "Equipment count must not change on re-seed");
        assertEquals(summary1.bookingCount(), summary2.bookingCount(), "Booking count must not change on re-seed");
        assertEquals(summary1.sessionCount(), summary2.sessionCount(), "Session count must not change on re-seed");
        assertEquals(summary1.idleEventCount(), summary2.idleEventCount(), "Idle event count must not change on re-seed");
        assertEquals(summary1.workOrderCount(), summary2.workOrderCount(), "Work order count must not change on re-seed");
        assertEquals(summary1.sharingAgreementCount(), summary2.sharingAgreementCount(), "Sharing agreement count must not change on re-seed");
        assertEquals(summary1.invoiceCount(), summary2.invoiceCount(), "Invoice count must not change on re-seed");
        assertEquals(summary1.notificationCount(), summary2.notificationCount(), "Notification count must not change on re-seed");
    }
}
