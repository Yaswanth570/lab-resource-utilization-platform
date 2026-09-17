package com.labresource.platform.config;

import com.labresource.platform.booking.Booking;
import com.labresource.platform.booking.BookingBillingStatus;
import com.labresource.platform.booking.BookingStatus;
import com.labresource.platform.booking.repository.BookingRepository;
import com.labresource.platform.cost.BillingInvoice;
import com.labresource.platform.cost.InvoiceLineItem;
import com.labresource.platform.cost.InvoiceStatus;
import com.labresource.platform.cost.repository.BillingInvoiceRepository;
import com.labresource.platform.cost.repository.InvoiceLineItemRepository;
import com.labresource.platform.department.Department;
import com.labresource.platform.department.repository.DepartmentRepository;
import com.labresource.platform.equipment.*;
import com.labresource.platform.equipment.repository.EquipmentCategoryRepository;
import com.labresource.platform.equipment.repository.EquipmentRepository;
import com.labresource.platform.equipment.repository.EquipmentSpecificationRepository;
import com.labresource.platform.equipment.repository.UserEquipmentQualificationRepository;
import com.labresource.platform.institution.Institution;
import com.labresource.platform.institution.repository.InstitutionRepository;
import com.labresource.platform.maintenance.*;
import com.labresource.platform.maintenance.repository.EquipmentDowntimeLogRepository;
import com.labresource.platform.maintenance.repository.MaintenanceRequestRepository;
import com.labresource.platform.maintenance.repository.MaintenanceWorkOrderRepository;
import com.labresource.platform.notification.Notification;
import com.labresource.platform.notification.NotificationEventType;
import com.labresource.platform.notification.NotificationPriority;
import com.labresource.platform.notification.repository.NotificationRepository;
import com.labresource.platform.sharing.ResourceSharingAgreement;
import com.labresource.platform.sharing.ResourceSharingRequest;
import com.labresource.platform.sharing.SharedEquipmentAllocation;
import com.labresource.platform.sharing.SharingAgreementStatus;
import com.labresource.platform.sharing.SharingRequestStatus;
import com.labresource.platform.sharing.repository.ResourceSharingAgreementRepository;
import com.labresource.platform.sharing.repository.ResourceSharingRequestRepository;
import com.labresource.platform.sharing.repository.SharedEquipmentAllocationRepository;
import com.labresource.platform.user.Role;
import com.labresource.platform.user.User;
import com.labresource.platform.user.UserRoleType;
import com.labresource.platform.user.UserStatus;
import com.labresource.platform.user.repository.RoleRepository;
import com.labresource.platform.user.repository.UserRepository;
import com.labresource.platform.utilization.*;
import com.labresource.platform.utilization.repository.EquipmentIdleEventRepository;
import com.labresource.platform.utilization.repository.EquipmentUsageSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Realistic Demonstration Dataset Seeder for Andhra Pradesh Institute of Technology & Research (APITR)
 * and partner institution Guntur Research & Innovation University (GRIU).
 * Fully idempotent, repeatable, and non-destructive.
 */
@Component
@Profile("!prod")
@Order(10)
public class DemoDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final InstitutionRepository institutionRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final EquipmentCategoryRepository categoryRepository;
    private final EquipmentRepository equipmentRepository;
    private final EquipmentSpecificationRepository specificationRepository;
    private final UserEquipmentQualificationRepository qualificationRepository;
    private final BookingRepository bookingRepository;
    private final EquipmentUsageSessionRepository sessionRepository;
    private final EquipmentIdleEventRepository idleEventRepository;
    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final MaintenanceWorkOrderRepository workOrderRepository;
    private final EquipmentDowntimeLogRepository downtimeLogRepository;
    private final ResourceSharingRequestRepository sharingRequestRepository;
    private final ResourceSharingAgreementRepository sharingAgreementRepository;
    private final SharedEquipmentAllocationRepository sharedAllocationRepository;
    private final BillingInvoiceRepository invoiceRepository;
    private final InvoiceLineItemRepository invoiceLineItemRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed-demo-data:false}")
    private boolean seedEnabled;

    public DemoDataSeeder(InstitutionRepository institutionRepository,
                          DepartmentRepository departmentRepository,
                          RoleRepository roleRepository,
                          UserRepository userRepository,
                          EquipmentCategoryRepository categoryRepository,
                          EquipmentRepository equipmentRepository,
                          EquipmentSpecificationRepository specificationRepository,
                          UserEquipmentQualificationRepository qualificationRepository,
                          BookingRepository bookingRepository,
                          EquipmentUsageSessionRepository sessionRepository,
                          EquipmentIdleEventRepository idleEventRepository,
                          MaintenanceRequestRepository maintenanceRequestRepository,
                          MaintenanceWorkOrderRepository workOrderRepository,
                          EquipmentDowntimeLogRepository downtimeLogRepository,
                          ResourceSharingRequestRepository sharingRequestRepository,
                          ResourceSharingAgreementRepository sharingAgreementRepository,
                          SharedEquipmentAllocationRepository sharedAllocationRepository,
                          BillingInvoiceRepository invoiceRepository,
                          InvoiceLineItemRepository invoiceLineItemRepository,
                          NotificationRepository notificationRepository,
                          PasswordEncoder passwordEncoder) {
        this.institutionRepository = institutionRepository;
        this.departmentRepository = departmentRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.equipmentRepository = equipmentRepository;
        this.specificationRepository = specificationRepository;
        this.qualificationRepository = qualificationRepository;
        this.bookingRepository = bookingRepository;
        this.sessionRepository = sessionRepository;
        this.idleEventRepository = idleEventRepository;
        this.maintenanceRequestRepository = maintenanceRequestRepository;
        this.workOrderRepository = workOrderRepository;
        this.downtimeLogRepository = downtimeLogRepository;
        this.sharingRequestRepository = sharingRequestRepository;
        this.sharingAgreementRepository = sharingAgreementRepository;
        this.sharedAllocationRepository = sharedAllocationRepository;
        this.invoiceRepository = invoiceRepository;
        this.invoiceLineItemRepository = invoiceLineItemRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!seedEnabled) {
            log.info("Demo data seeding is disabled via configuration (app.seed-demo-data=false). Skipping.");
            return;
        }
        log.info("Initiating Realistic Andhra Pradesh Demo Dataset seeding...");
        DemoDataSummary summary = seedAll();
        log.info("Demo Dataset seeding complete: {}", summary);
    }

    @Transactional
    public DemoDataSummary seedAll() {
        // 1. Institutions
        Institution apitr = getOrCreateInstitution("Andhra Pradesh Institute of Technology & Research", "APITR", "apitr.edu",
                "NH-16, Bypass Road", "Guntur", "Andhra Pradesh", "India", "522508", "contact@apitr.edu", "+91 863 2345678");
        Institution griu = getOrCreateInstitution("Guntur Research & Innovation University", "GRIU", "griu.edu",
                "University Campus, Amaravathi Road", "Guntur", "Andhra Pradesh", "India", "522002", "partner@griu.edu", "+91 863 9876543");

        // 2. Departments
        Department cse = getOrCreateDepartment(apitr, "Computer Science & Engineering", "CSE", "CSE-DEPT-01");
        Department ece = getOrCreateDepartment(apitr, "Electronics & Communication Engineering", "ECE", "ECE-DEPT-02");
        Department eee = getOrCreateDepartment(apitr, "Electrical & Electronics Engineering", "EEE", "EEE-DEPT-03");
        Department me = getOrCreateDepartment(apitr, "Mechanical Engineering", "ME", "ME-DEPT-04");
        Department ce = getOrCreateDepartment(apitr, "Civil Engineering", "CE", "CE-DEPT-05");
        Department aids = getOrCreateDepartment(apitr, "Artificial Intelligence & Data Science", "AI&DS", "AIDS-DEPT-06");
        Department ard = getOrCreateDepartment(griu, "Advanced Research Department", "ARD", "GRIU-ARD-01");

        // 3. Roles
        Map<UserRoleType, Role> roles = loadRoles();

        // 4. Users (All 6 roles)
        String defaultPasswordHash = passwordEncoder.encode("Password123!");

        User sysAdmin = getOrCreateUser(apitr, cse, "sysadmin@apitr.edu", "Arun", "Kumar", "+91 98480 11111",
                defaultPasswordHash, Set.of(roles.get(UserRoleType.ROLE_SYSTEM_ADMINISTRATOR)));

        User instAdmin = getOrCreateUser(apitr, cse, "admin@apitr.edu", "Priya", "Reddy", "+91 98480 22222",
                defaultPasswordHash, Set.of(roles.get(UserRoleType.ROLE_INSTITUTION_ADMINISTRATOR)));

        User deptHead = getOrCreateUser(apitr, cse, "hod.cse@apitr.edu", "Suresh", "Babu", "+91 98480 33333",
                defaultPasswordHash, Set.of(roles.get(UserRoleType.ROLE_DEPARTMENT_HEAD)));

        // Link department head
        if (cse.getHeadUser() == null) {
            cse.setHeadUser(deptHead);
            departmentRepository.save(cse);
        }

        User labManager = getOrCreateUser(apitr, cse, "labmanager.cse@apitr.edu", "Lakshmi", "Devi", "+91 98480 44444",
                defaultPasswordHash, Set.of(roles.get(UserRoleType.ROLE_LAB_MANAGER)));

        User labTech = getOrCreateUser(apitr, cse, "technician.cse@apitr.edu", "Ravi", "Teja", "+91 98480 55555",
                defaultPasswordHash, Set.of(roles.get(UserRoleType.ROLE_LAB_TECHNICIAN)));

        User researcher = getOrCreateUser(apitr, cse, "researcher@apitr.edu", "Anjali", "Rao", "+91 98480 66666",
                defaultPasswordHash, Set.of(roles.get(UserRoleType.ROLE_RESEARCHER_STUDENT)));

        User student1 = getOrCreateUser(apitr, cse, "student@apitr.edu", "Rahul", "Varma", "+91 98480 77777",
                defaultPasswordHash, Set.of(roles.get(UserRoleType.ROLE_RESEARCHER_STUDENT)));

        User student2 = getOrCreateUser(apitr, ece, "student2@apitr.edu", "Sneha", "Reddy", "+91 98480 88888",
                defaultPasswordHash, Set.of(roles.get(UserRoleType.ROLE_RESEARCHER_STUDENT)));

        User partnerResearcher = getOrCreateUser(griu, ard, "kalyan.c@griu.edu", "Kalyan", "Chakravarthy", "+91 98480 99999",
                defaultPasswordHash, Set.of(roles.get(UserRoleType.ROLE_RESEARCHER_STUDENT)));

        // 5. Equipment Categories
        EquipmentCategory catComputing = getOrCreateCategory("Computing & AI Workstations", "High-performance compute clusters and AI workstations");
        EquipmentCategory catTesting = getOrCreateCategory("Testing & Measurement Instruments", "Electronic, RF, and optical measurement devices");
        EquipmentCategory catManufacturing = getOrCreateCategory("Manufacturing & Prototyping", "CNC machines, lathes, 3D printers, and fabrication gear");
        EquipmentCategory catCivil = getOrCreateCategory("Surveying & Civil Equipment", "Geotechnical, structural, and surveying instruments");
        EquipmentCategory catPower = getOrCreateCategory("Power & Renewable Systems", "Solar trainers, motor benches, and grid analyzers");
        EquipmentCategory catIoT = getOrCreateCategory("IoT & Embedded Systems", "Microcontrollers, development kits, and sensor arrays");

        // 6. Equipment (26 realistic instruments distributed across departments)
        // CSE Equipment
        Equipment eqDellWorkstation = getOrCreateEquipment(apitr, cse, catComputing, labManager,
                "Dell Precision AI Workstation", "APITR-EQ-CSE-001", "SN-DPAI-9821", "Precision 7960", "Dell Technologies",
                "Ramanujan Block", "Lab 301", EquipmentStatus.AVAILABLE, "Operational and calibrated",
                false, new BigDecimal("250.00"), new BigDecimal("400.00"), 30, 480, 15, true, true,
                LocalDate.of(2025, 1, 15), new BigDecimal("450000.00"), LocalDate.of(2028, 1, 15));

        Equipment eqNvidiaGpu = getOrCreateEquipment(apitr, cse, catComputing, labManager,
                "NVIDIA GPU Workstation", "APITR-EQ-CSE-002", "SN-NV-A100-4412", "DGX Station A100", "NVIDIA",
                "Ramanujan Block", "High Performance Lab 304", EquipmentStatus.AVAILABLE, "System ready for deep learning workflows",
                false, new BigDecimal("250.00"), new BigDecimal("500.00"), 30, 480, 15, true, true,
                LocalDate.of(2025, 3, 10), new BigDecimal("1800000.00"), LocalDate.of(2028, 3, 10));

        Equipment eqHpcServer = getOrCreateEquipment(apitr, cse, catComputing, labManager,
                "High Performance Computing Server", "APITR-EQ-CSE-003", "SN-HPC-88192", "PowerEdge R750xa", "Dell Technologies",
                "Ramanujan Block", "Server Room 102", EquipmentStatus.IN_USE, "Active batch processing job running",
                false, new BigDecimal("300.00"), new BigDecimal("600.00"), 60, 480, 30, false, true,
                LocalDate.of(2024, 8, 20), new BigDecimal("1250000.00"), LocalDate.of(2027, 8, 20));

        Equipment eqIotKit = getOrCreateEquipment(apitr, cse, catIoT, labManager,
                "IoT Development Kit", "APITR-EQ-CSE-004", "SN-IOT-6621", "SenseCAP K1100", "Seeed Studio",
                "Ramanujan Block", "Embedded Lab 205", EquipmentStatus.AVAILABLE, "Sensors and gateways tested",
                false, new BigDecimal("50.00"), new BigDecimal("100.00"), 30, 240, 15, false, false,
                LocalDate.of(2025, 6, 1), new BigDecimal("45000.00"), LocalDate.of(2027, 6, 1));

        Equipment eqEmbeddedTrainer = getOrCreateEquipment(apitr, cse, catIoT, labManager,
                "Embedded Systems Trainer", "APITR-EQ-CSE-005", "SN-EMB-3310", "STM32 Discovery Pro", "STMicroelectronics",
                "Ramanujan Block", "Embedded Lab 205", EquipmentStatus.AVAILABLE, "Firmware updated",
                false, new BigDecimal("50.00"), new BigDecimal("100.00"), 30, 240, 15, false, false,
                LocalDate.of(2025, 6, 1), new BigDecimal("65000.00"), LocalDate.of(2027, 6, 1));

        // AI&DS Equipment
        Equipment eqAimServer = getOrCreateEquipment(apitr, aids, catComputing, labManager,
                "AI/ML GPU Server", "APITR-EQ-AIDS-001", "SN-AIML-4090-8", "HGX H100 Dual", "Supermicro",
                "Aryabhata Research Centre", "Room 401", EquipmentStatus.AVAILABLE, "Ready for transformer training",
                false, new BigDecimal("300.00"), new BigDecimal("600.00"), 60, 480, 15, true, true,
                LocalDate.of(2025, 2, 1), new BigDecimal("2400000.00"), LocalDate.of(2028, 2, 1));

        Equipment eqDataSciWs = getOrCreateEquipment(apitr, aids, catComputing, labManager,
                "Data Science Workstation", "APITR-EQ-AIDS-002", "SN-DSW-7712", "ThinkStation P620", "Lenovo",
                "Aryabhata Research Centre", "Room 402", EquipmentStatus.AVAILABLE, "Operational",
                false, new BigDecimal("150.00"), new BigDecimal("250.00"), 30, 360, 15, false, false,
                LocalDate.of(2025, 4, 15), new BigDecimal("320000.00"), LocalDate.of(2028, 4, 15));

        Equipment eqDeepLearnWs = getOrCreateEquipment(apitr, aids, catComputing, labManager,
                "Deep Learning Workstation", "APITR-EQ-AIDS-003", "SN-DLW-5541", "RTX 6000 Ada Station", "HP Z8 Fury",
                "Aryabhata Research Centre", "Room 403", EquipmentStatus.AVAILABLE, "Operational",
                false, new BigDecimal("200.00"), new BigDecimal("350.00"), 30, 360, 15, false, false,
                LocalDate.of(2025, 5, 20), new BigDecimal("580000.00"), LocalDate.of(2028, 5, 20));

        // ECE Equipment
        Equipment eqOscilloscope = getOrCreateEquipment(apitr, ece, catTesting, labManager,
                "Digital Oscilloscope", "APITR-EQ-ECE-001", "SN-DSO-2004B", "DSOX2004A 70MHz 4-Ch", "Keysight Technologies",
                "Sarabhai Block", "RF Lab 101", EquipmentStatus.AVAILABLE, "Annual calibration passed",
                false, new BigDecimal("150.00"), new BigDecimal("250.00"), 30, 360, 15, true, false,
                LocalDate.of(2024, 7, 10), new BigDecimal("220000.00"), LocalDate.of(2027, 7, 10));

        Equipment eqSpectrumAnalyzer = getOrCreateEquipment(apitr, ece, catTesting, labManager,
                "Spectrum Analyzer", "APITR-EQ-ECE-002", "SN-SPA-9030", "FSW26 26.5 GHz", "Rohde & Schwarz",
                "Sarabhai Block", "Microwave Lab 105", EquipmentStatus.IN_USE, "Cross-institutional research active",
                true, new BigDecimal("400.00"), new BigDecimal("600.00"), 60, 480, 30, true, true,
                LocalDate.of(2024, 5, 12), new BigDecimal("1650000.00"), LocalDate.of(2027, 5, 12));

        Equipment eqFunctionGen = getOrCreateEquipment(apitr, ece, catTesting, labManager,
                "Function Generator", "APITR-EQ-ECE-003", "SN-AFG-3102", "AFG31022 100MHz Dual", "Tektronix",
                "Sarabhai Block", "Circuits Lab 102", EquipmentStatus.AVAILABLE, "Bench ready",
                false, new BigDecimal("100.00"), new BigDecimal("180.00"), 30, 240, 15, false, false,
                LocalDate.of(2024, 9, 15), new BigDecimal("180000.00"), LocalDate.of(2027, 9, 15));

        Equipment eqFpgaKit = getOrCreateEquipment(apitr, ece, catTesting, labManager,
                "FPGA Development Kit", "APITR-EQ-ECE-004", "SN-ZYNQ-7020", "ZedBoard Zynq-7000", "Digilent",
                "Sarabhai Block", "VLSI Design Lab 108", EquipmentStatus.AVAILABLE, "Operational",
                false, new BigDecimal("100.00"), new BigDecimal("180.00"), 30, 300, 15, false, false,
                LocalDate.of(2025, 2, 28), new BigDecimal("85000.00"), LocalDate.of(2027, 2, 28));

        Equipment eqCommTrainer = getOrCreateEquipment(apitr, ece, catTesting, labManager,
                "Digital Communication Trainer", "APITR-EQ-ECE-005", "SN-DCT-401", "ST-4011 Digital Comm", "Scientech",
                "Sarabhai Block", "Comm Lab 104", EquipmentStatus.RETIRED, "Decommissioned; superseded by SDR modules",
                false, new BigDecimal("50.00"), new BigDecimal("100.00"), 30, 180, 15, false, false,
                LocalDate.of(2020, 1, 10), new BigDecimal("55000.00"), LocalDate.of(2023, 1, 10));

        // EEE Equipment
        Equipment eqMultimeter = getOrCreateEquipment(apitr, eee, catTesting, labManager,
                "Digital Multimeter", "APITR-EQ-EEE-001", "SN-DMM-8846A", "Fluke 8846A 6.5 Digit", "Fluke Corporation",
                "Visvesvaraya Block", "Instrumentation Lab 201", EquipmentStatus.AVAILABLE, "Calibrated",
                false, new BigDecimal("80.00"), new BigDecimal("150.00"), 30, 240, 15, false, false,
                LocalDate.of(2024, 11, 5), new BigDecimal("140000.00"), LocalDate.of(2027, 11, 5));

        Equipment eqPowerQuality = getOrCreateEquipment(apitr, eee, catTesting, labManager,
                "Power Quality Analyzer", "APITR-EQ-EEE-002", "SN-PQA-435-II", "Fluke 435 Series II 3-Phase", "Fluke Corporation",
                "Visvesvaraya Block", "Power Systems Lab 203", EquipmentStatus.AVAILABLE, "Battery and leads checked",
                false, new BigDecimal("200.00"), new BigDecimal("350.00"), 30, 360, 15, false, false,
                LocalDate.of(2025, 3, 1), new BigDecimal("380000.00"), LocalDate.of(2028, 3, 1));

        Equipment eqSolarTrainer = getOrCreateEquipment(apitr, eee, catPower, labManager,
                "Solar PV Trainer", "APITR-EQ-EEE-003", "SN-SPV-500W", "SPVT-100 Solar Simulator", "Emona Instruments",
                "Visvesvaraya Block", "Renewable Energy Lab 207", EquipmentStatus.UNDER_MAINTENANCE, "Controller malfunction - awaiting replacement IC",
                false, new BigDecimal("150.00"), new BigDecimal("250.00"), 30, 300, 15, false, false,
                LocalDate.of(2024, 6, 18), new BigDecimal("290000.00"), LocalDate.of(2027, 6, 18));

        Equipment eqMachinesTrainer = getOrCreateEquipment(apitr, eee, catPower, labManager,
                "Electrical Machines Trainer", "APITR-EQ-EEE-004", "SN-EMT-3PH-5HP", "EMT-500 Coupled Dynamo", "Kirloskar Electric",
                "Visvesvaraya Block", "Machines Lab 101", EquipmentStatus.OUT_OF_SERVICE, "Stator winding insulation fault detected",
                false, new BigDecimal("150.00"), new BigDecimal("250.00"), 30, 300, 15, false, false,
                LocalDate.of(2023, 4, 12), new BigDecimal("340000.00"), LocalDate.of(2026, 4, 12));

        // ME Equipment
        Equipment eqCncMill = getOrCreateEquipment(apitr, me, catManufacturing, labManager,
                "CNC Milling Machine", "APITR-EQ-ME-001", "SN-CNC-VMC-600", "Super VF-2 3-Axis VMC", "Haas Automation",
                "Workshop Complex", "CNC Bay 1", EquipmentStatus.UNDER_MAINTENANCE, "Spindle vibration detected - corrective maintenance in progress",
                false, new BigDecimal("600.00"), new BigDecimal("1000.00"), 60, 480, 30, true, true,
                LocalDate.of(2023, 10, 15), new BigDecimal("3200000.00"), LocalDate.of(2026, 10, 15));

        Equipment eqLathe = getOrCreateEquipment(apitr, me, catManufacturing, labManager,
                "Lathe Machine", "APITR-EQ-ME-002", "SN-LTH-1650G", "Precision Geared Head Lathe 1650", "HMT Machine Tools",
                "Workshop Complex", "Machine Shop", EquipmentStatus.AVAILABLE, "Tooling and oil checked",
                false, new BigDecimal("120.00"), new BigDecimal("200.00"), 30, 360, 15, false, false,
                LocalDate.of(2022, 5, 20), new BigDecimal("450000.00"), LocalDate.of(2025, 5, 20));

        Equipment eq3dPrinter = getOrCreateEquipment(apitr, me, catManufacturing, labManager,
                "3D Printer", "APITR-EQ-ME-003", "SN-3DP-S5-891", "Ultimaker S5 Dual Extruder", "Ultimaker",
                "Workshop Complex", "Rapid Prototyping Lab", EquipmentStatus.AVAILABLE, "Nozzles cleaned and leveled",
                false, new BigDecimal("180.00"), new BigDecimal("300.00"), 30, 480, 15, false, false,
                LocalDate.of(2024, 12, 1), new BigDecimal("620000.00"), LocalDate.of(2027, 12, 1));

        Equipment eqThermalCam = getOrCreateEquipment(apitr, me, catTesting, labManager,
                "Thermal Imaging Camera", "APITR-EQ-ME-004", "SN-TIC-E8-XT", "FLIR E8-XT Wi-Fi 320x240", "Teledyne FLIR",
                "Workshop Complex", "Heat Transfer Lab", EquipmentStatus.AVAILABLE, "Operational",
                false, new BigDecimal("150.00"), new BigDecimal("250.00"), 30, 240, 15, false, false,
                LocalDate.of(2025, 1, 8), new BigDecimal("210000.00"), LocalDate.of(2028, 1, 8));

        Equipment eqUtm = getOrCreateEquipment(apitr, me, catTesting, labManager,
                "Universal Testing Machine", "APITR-EQ-ME-005", "SN-UTM-100KN", "Instron 5982 100kN Floor", "Instron",
                "Workshop Complex", "Materials Testing Lab", EquipmentStatus.AVAILABLE, "Load cell calibrated",
                false, new BigDecimal("350.00"), new BigDecimal("550.00"), 60, 360, 30, true, false,
                LocalDate.of(2024, 3, 22), new BigDecimal("1950000.00"), LocalDate.of(2027, 3, 22));

        // CE Equipment
        Equipment eqTotalStation = getOrCreateEquipment(apitr, ce, catCivil, labManager,
                "Total Station", "APITR-EQ-CE-001", "SN-TS-TS07-5", "FlexLine TS07 5\" Manual", "Leica Geosystems",
                "Civil Engineering Complex", "Survey Store 101", EquipmentStatus.AVAILABLE, "Prisms and tribrach verified",
                false, new BigDecimal("200.00"), new BigDecimal("350.00"), 60, 480, 15, false, false,
                LocalDate.of(2024, 4, 10), new BigDecimal("480000.00"), LocalDate.of(2027, 4, 10));

        Equipment eqCompressionTester = getOrCreateEquipment(apitr, ce, catCivil, labManager,
                "Concrete Compression Testing Machine", "APITR-EQ-CE-002", "SN-CTM-2000KN", "CTM-2000 Digital Display", "Aimil Ltd",
                "Civil Engineering Complex", "Concrete Lab 102", EquipmentStatus.AVAILABLE, "Hydraulic pressure tested",
                false, new BigDecimal("150.00"), new BigDecimal("250.00"), 30, 240, 15, false, false,
                LocalDate.of(2023, 11, 14), new BigDecimal("310000.00"), LocalDate.of(2026, 11, 14));

        Equipment eqTheodolite = getOrCreateEquipment(apitr, ce, catCivil, labManager,
                "Digital Theodolite", "APITR-EQ-CE-003", "SN-DT-DT500", "DT-500 2\" Electronic", "Topcon",
                "Civil Engineering Complex", "Survey Store 101", EquipmentStatus.AVAILABLE, "Operational",
                false, new BigDecimal("100.00"), new BigDecimal("180.00"), 30, 360, 15, false, false,
                LocalDate.of(2024, 8, 5), new BigDecimal("190000.00"), LocalDate.of(2027, 8, 5));

        Equipment eqSoilTester = getOrCreateEquipment(apitr, ce, catCivil, labManager,
                "Soil Testing Apparatus", "APITR-EQ-CE-004", "SN-STA-CBR-01", "Direct Shear & CBR Automated", "Aimil Ltd",
                "Civil Engineering Complex", "Geotechnical Lab 104", EquipmentStatus.AVAILABLE, "Proving rings calibrated",
                false, new BigDecimal("120.00"), new BigDecimal("200.00"), 30, 300, 15, false, false,
                LocalDate.of(2024, 9, 28), new BigDecimal("260000.00"), LocalDate.of(2027, 9, 28));

        // 7. Equipment Specifications
        seedSpecifications(eqNvidiaGpu, Map.of(
                "GPU", "4x NVIDIA A100 Tensor Core 80GB SXM4",
                "VRAM", "320 GB HBM2e",
                "CPU", "AMD EPYC 7742 64-Core Processor",
                "RAM", "512 GB DDR4-3200 ECC",
                "Storage", "15.36 TB NVMe Gen4 SSD"));

        seedSpecifications(eqOscilloscope, Map.of(
                "Bandwidth", "70 MHz (upgradeable to 200 MHz)",
                "Channels", "4 Analog Channels",
                "Sample Rate", "2 GSa/s",
                "Memory Depth", "1 Mpts per channel"));

        seedSpecifications(eqSpectrumAnalyzer, Map.of(
                "Frequency Range", "2 Hz to 26.5 GHz",
                "Resolution Bandwidth", "1 Hz to 10 MHz",
                "Phase Noise", "-137 dBc/Hz at 1 GHz (10 kHz offset)"));

        seedSpecifications(eqCncMill, Map.of(
                "Spindle Speed", "10,000 RPM",
                "Work Area", "762 x 406 x 508 mm",
                "Spindle Power", "30 hp (22.4 kW)",
                "Tool Changer", "24+1 Side-Mount Carousel"));

        seedSpecifications(eq3dPrinter, Map.of(
                "Build Volume", "330 x 240 x 300 mm",
                "Layer Resolution", "20 to 600 microns",
                "Print Materials", "PLA, Tough PLA, ABS, Nylon, TPU 95A, PVA"));

        seedSpecifications(eqTotalStation, Map.of(
                "Angular Accuracy", "5 arc-seconds",
                "Reflectorless Range", "500 meters",
                "Distance Accuracy", "1.5 mm + 2 ppm with Prism"));

        seedSpecifications(eqUtm, Map.of(
                "Maximum Load", "100 kN (22,500 lbf)",
                "Crosshead Speed", "0.001 to 1016 mm/min",
                "Test Types", "Tensile, Compression, Flexure, Shear"));

        // 8. Qualifications (Active and Expired)
        getOrCreateQualification(researcher, eqNvidiaGpu, labManager,
                Instant.now().minus(180, ChronoUnit.DAYS), Instant.now().plus(180, ChronoUnit.DAYS),
                QualificationStatus.ACTIVE, "GPU Cluster Architecture & Slurm Queue Training Completed");

        getOrCreateQualification(student1, eqOscilloscope, labManager,
                Instant.now().minus(90, ChronoUnit.DAYS), Instant.now().plus(270, ChronoUnit.DAYS),
                QualificationStatus.ACTIVE, "High-frequency probe handling & grounding safety certified");

        getOrCreateQualification(student2, eqCncMill, labManager,
                Instant.now().minus(400, ChronoUnit.DAYS), Instant.now().minus(35, ChronoUnit.DAYS),
                QualificationStatus.EXPIRED, "Initial basic milling certification expired; annual safety refresher required before booking");

        // 9. Resource Sharing (APITR & GRIU)
        ResourceSharingRequest sharingReq = getOrCreateSharingRequest(griu, apitr, partnerResearcher, eqSpectrumAnalyzer, ece,
                "Inter-Institutional RF Characterization Collaborative Agreement",
                "Joint DST research project on 5G millimeter-wave harmonic analysis requiring 26.5 GHz spectrum analyzer.",
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 12, 31),
                SharingRequestStatus.APPROVED, instAdmin);

        ResourceSharingAgreement sharingAgreement = getOrCreateSharingAgreement(sharingReq, "RSA-APITR-GRIU-2026-01",
                griu, apitr, new BigDecimal("1.25"), 40,
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 12, 31),
                SharingAgreementStatus.ACTIVE);

        SharedEquipmentAllocation sharedAllocation = getOrCreateSharedAllocation(sharingAgreement, eqSpectrumAnalyzer,
                new BigDecimal("500.00"), true);

        // 10. Bookings (Covering ALL 6 Statuses + External Sharing)
        // 1: PENDING_APPROVAL - Anjali Rao on NVIDIA GPU Workstation (Sep 10, 2026)
        Booking bkPending = getOrCreateBooking("APITR-BK-001", eqNvidiaGpu, researcher, cse, apitr,
                Instant.parse("2026-09-10T10:00:00Z"), Instant.parse("2026-09-10T12:00:00Z"),
                BookingStatus.PENDING_APPROVAL, BookingBillingStatus.UNBILLED,
                "Deep learning training for medical image segmentation", "DST-MED-2026",
                null, null, null, null, null,
                false, null, new BigDecimal("250.00"), new BigDecimal("500.00"), null);

        // 2: CONFIRMED - Rahul Varma on Digital Oscilloscope (Sep 11, 2026)
        Booking bkConfirmed = getOrCreateBooking("APITR-BK-002", eqOscilloscope, student1, cse, apitr,
                Instant.parse("2026-09-11T14:00:00Z"), Instant.parse("2026-09-11T15:00:00Z"),
                BookingStatus.CONFIRMED, BookingBillingStatus.UNBILLED,
                "Signal transient response analysis for capstone circuit", "UG-CAPSTONE-402",
                labManager, Instant.parse("2026-09-08T09:30:00Z"), null, null, null,
                false, null, new BigDecimal("150.00"), new BigDecimal("150.00"), null);

        // 3: COMPLETED - Sneha Reddy on IoT Development Kit (Sep 8, 2026) - High utilization
        Booking bkCompleted1 = getOrCreateBooking("APITR-BK-003", eqIotKit, student2, ece, apitr,
                Instant.parse("2026-09-08T10:00:00Z"), Instant.parse("2026-09-08T12:00:00Z"),
                BookingStatus.COMPLETED, BookingBillingStatus.UNBILLED,
                "LoRaWAN environmental telemetry field testing", "ECE-IOT-881",
                labManager, Instant.parse("2026-09-07T14:00:00Z"), null, null, null,
                false, null, new BigDecimal("50.00"), new BigDecimal("100.00"), new BigDecimal("100.00"));

        // 4: COMPLETED - Anjali Rao on AI/ML GPU Server (Sep 7, 2026) - High utilization
        Booking bkCompleted2 = getOrCreateBooking("APITR-BK-004", eqAimServer, researcher, aids, apitr,
                Instant.parse("2026-09-07T09:00:00Z"), Instant.parse("2026-09-07T11:00:00Z"),
                BookingStatus.COMPLETED, BookingBillingStatus.UNBILLED,
                "Hyperparameter tuning on transformer model", "AI-TRANS-90",
                labManager, Instant.parse("2026-09-06T16:00:00Z"), null, null, null,
                false, null, new BigDecimal("300.00"), new BigDecimal("600.00"), new BigDecimal("600.00"));

        // 5: COMPLETED - Anjali Rao on Universal Testing Machine (Sep 7, 2026) - Medium utilization
        Booking bkCompleted3 = getOrCreateBooking("APITR-BK-005", eqUtm, researcher, me, apitr,
                Instant.parse("2026-09-07T13:00:00Z"), Instant.parse("2026-09-07T15:00:00Z"),
                BookingStatus.COMPLETED, BookingBillingStatus.INVOICED,
                "Tensile strength testing of 3D printed polymer specimens", "MAT-RES-31",
                labManager, Instant.parse("2026-09-06T15:00:00Z"), null, null, null,
                false, null, new BigDecimal("350.00"), new BigDecimal("700.00"), new BigDecimal("700.00"));

        // 6: CANCELLED - Rahul Varma on Function Generator (Sep 5, 2026)
        Booking bkCancelled = getOrCreateBooking("APITR-BK-006", eqFunctionGen, student1, cse, apitr,
                Instant.parse("2026-09-05T14:00:00Z"), Instant.parse("2026-09-05T15:00:00Z"),
                BookingStatus.CANCELLED, BookingBillingStatus.UNBILLED,
                "Filter frequency response calibration lab", "CSE-HW-201",
                null, null, student1, Instant.parse("2026-09-05T09:15:00Z"), "Rescheduled lab session due to lecture conflict",
                false, null, new BigDecimal("100.00"), new BigDecimal("100.00"), BigDecimal.ZERO);

        // 7: NO_SHOW - Sneha Reddy on 3D Printer (Sep 4, 2026)
        Booking bkNoShow = getOrCreateBooking("APITR-BK-007", eq3dPrinter, student2, me, apitr,
                Instant.parse("2026-09-04T11:00:00Z"), Instant.parse("2026-09-04T13:00:00Z"),
                BookingStatus.NO_SHOW, BookingBillingStatus.UNBILLED,
                "Rapid prototyping casing for robotics contest", "ROBO-ME-11",
                labManager, Instant.parse("2026-09-03T10:00:00Z"), null, null, null,
                false, null, new BigDecimal("180.00"), new BigDecimal("360.00"), BigDecimal.ZERO);

        // 8: IN_USE - Anjali Rao on Spectrum Analyzer (Sep 9, 2026)
        Booking bkInUse = getOrCreateBooking("APITR-BK-008", eqSpectrumAnalyzer, researcher, ece, apitr,
                Instant.parse("2026-09-09T15:00:00Z"), Instant.parse("2026-09-09T17:00:00Z"),
                BookingStatus.IN_USE, BookingBillingStatus.UNBILLED,
                "Inter-institution harmonic emission characterization", "DST-5G-HARMONICS",
                labManager, Instant.parse("2026-09-09T12:00:00Z"), null, null, null,
                false, null, new BigDecimal("400.00"), new BigDecimal("800.00"), null);

        // 9: External Booking - Kalyan Chakravarthy (GRIU) on Spectrum Analyzer (Sep 12, 2026)
        Booking bkExternal = getOrCreateBooking("APITR-BK-009", eqSpectrumAnalyzer, partnerResearcher, ard, griu,
                Instant.parse("2026-09-12T10:00:00Z"), Instant.parse("2026-09-12T14:00:00Z"),
                BookingStatus.CONFIRMED, BookingBillingStatus.UNBILLED,
                "Partner institution wideband RF spectrum characterization", "GRIU-COLLAB-01",
                labManager, Instant.parse("2026-09-08T11:00:00Z"), null, null, null,
                true, sharedAllocation, new BigDecimal("500.00"), new BigDecimal("2000.00"), null);

        // 11. Usage Sessions
        EquipmentUsageSession sess1 = getOrCreateUsageSession(bkCompleted1, eqIotKit, student2,
                Instant.parse("2026-09-08T10:02:00Z"), Instant.parse("2026-09-08T11:58:00Z"),
                116, 120, SessionStatus.COMPLETED, "Completed telemetry transmission trial successfully");

        EquipmentUsageSession sess2 = getOrCreateUsageSession(bkCompleted2, eqAimServer, researcher,
                Instant.parse("2026-09-07T09:00:00Z"), Instant.parse("2026-09-07T11:00:00Z"),
                120, 120, SessionStatus.COMPLETED, "Transformer convergence attained after 20 epochs");

        EquipmentUsageSession sess3 = getOrCreateUsageSession(bkCompleted3, eqUtm, researcher,
                Instant.parse("2026-09-07T13:05:00Z"), Instant.parse("2026-09-07T14:55:00Z"),
                110, 120, SessionStatus.COMPLETED, "Tensile yield points recorded for 6 dogbone specimens");

        EquipmentUsageSession sess4 = getOrCreateUsageSession(bkInUse, eqSpectrumAnalyzer, researcher,
                Instant.parse("2026-09-09T15:00:00Z"), null,
                null, 120, SessionStatus.ACTIVE, "Harmonic sweeps currently logging");

        // 12. Idle Events (Low utilization demonstration)
        getOrCreateIdleEvent(eqCncMill, null, null, IdleDetectionSource.SCHEDULED_INSPECTION_CRON,
                Instant.parse("2026-09-03T09:00:00Z"), Instant.parse("2026-09-03T17:00:00Z"),
                480, IdleEventStatus.RESOLVED, labTech, "Machine idle during scheduled daytime operating window");

        getOrCreateIdleEvent(eqTotalStation, null, null, IdleDetectionSource.MANUAL_LAB_AUDIT,
                Instant.parse("2026-09-02T09:00:00Z"), Instant.parse("2026-09-02T17:00:00Z"),
                480, IdleEventStatus.ACKNOWLEDGED, labManager, "Low booking demand identified during early semester");

        getOrCreateIdleEvent(eq3dPrinter, bkNoShow, null, IdleDetectionSource.BOOKING_NO_SHOW,
                Instant.parse("2026-09-04T11:15:00Z"), Instant.parse("2026-09-04T13:00:00Z"),
                105, IdleEventStatus.RESOLVED, labTech, "Booking no-show triggered auto-idle event");

        // 13. Maintenance & Work Orders & Downtimes
        // Scenario 1: CNC Milling Machine - Spindle vibration (In Progress)
        MaintenanceRequest mr1 = getOrCreateMaintenanceRequest("MR-2026-001", eqCncMill, labManager,
                MaintenancePriority.HIGH, "Spindle vibration detected",
                "Spindle vibration exceeded 4.2 mm/s threshold during high-speed face milling. Safety cutoff engaged.",
                MaintenanceRequestStatus.WORK_ORDER_CREATED, labManager, Instant.parse("2026-09-08T09:10:00Z"));

        MaintenanceWorkOrder mwo1 = getOrCreateWorkOrder("MWO-2026-001", mr1, eqCncMill, labTech,
                WorkOrderType.CORRECTIVE, MaintenancePriority.HIGH, WorkOrderStatus.IN_PROGRESS,
                Instant.parse("2026-09-08T09:30:00Z"), Instant.parse("2026-09-10T18:00:00Z"),
                Instant.parse("2026-09-08T10:00:00Z"), null,
                new BigDecimal("6.50"), new BigDecimal("3250.00"), new BigDecimal("8500.00"), new BigDecimal("11750.00"),
                "Bearing inspection and dynamic balancing of high-speed spindle assembly.",
                "Main bearing pre-load relaxation due to thermal cycling.",
                "Replacement ceramic hybrid bearings ordered and installed.");

        getOrCreateDowntimeLog(eqCncMill, mwo1, DowntimeReasonCategory.UNSCHEDULED_BREAKDOWN,
                Instant.parse("2026-09-08T09:30:00Z"), null, null,
                "CNC spindle vibration emergency shutdown");

        // Scenario 2: Solar PV Trainer - Controller malfunction (Waiting for Parts)
        MaintenanceRequest mr2 = getOrCreateMaintenanceRequest("MR-2026-002", eqSolarTrainer, labTech,
                MaintenancePriority.MEDIUM, "Controller malfunction",
                "Solar simulator charge controller MPPT tracking failure. Display showing Err-42.",
                MaintenanceRequestStatus.WORK_ORDER_CREATED, labManager, Instant.parse("2026-09-07T10:30:00Z"));

        MaintenanceWorkOrder mwo2 = getOrCreateWorkOrder("MWO-2026-002", mr2, eqSolarTrainer, labTech,
                WorkOrderType.CORRECTIVE, MaintenancePriority.MEDIUM, WorkOrderStatus.WAITING_FOR_PARTS,
                Instant.parse("2026-09-07T11:00:00Z"), Instant.parse("2026-09-14T17:00:00Z"),
                Instant.parse("2026-09-07T11:30:00Z"), null,
                new BigDecimal("2.00"), new BigDecimal("1000.00"), new BigDecimal("4200.00"), new BigDecimal("5200.00"),
                "Charge controller diagnostics performed; MOSFET driver IC blown.",
                "Grid transient surge caused driver gate breakdown.",
                "Awaiting replacement MPPT board from OEM vendor.");

        getOrCreateDowntimeLog(eqSolarTrainer, mwo2, DowntimeReasonCategory.UNSCHEDULED_BREAKDOWN,
                Instant.parse("2026-09-07T11:00:00Z"), null, null,
                "Solar simulator charge controller replacement on order");

        // Scenario 3: Digital Oscilloscope - Preventive maintenance (Completed)
        MaintenanceRequest mr3 = getOrCreateMaintenanceRequest("MR-2026-003", eqOscilloscope, labTech,
                MaintenancePriority.LOW, "Scheduled annual preventive maintenance & probe calibration",
                "Periodic calibration and attenuator contact cleaning as per ISO/IEC 17025 schedule.",
                MaintenanceRequestStatus.RESOLVED, labManager, Instant.parse("2026-09-01T08:30:00Z"));

        MaintenanceWorkOrder mwo3 = getOrCreateWorkOrder("MWO-2026-003", mr3, eqOscilloscope, labTech,
                WorkOrderType.PREVENTIVE, MaintenancePriority.LOW, WorkOrderStatus.COMPLETED,
                Instant.parse("2026-09-01T09:00:00Z"), Instant.parse("2026-09-01T13:00:00Z"),
                Instant.parse("2026-09-01T09:00:00Z"), Instant.parse("2026-09-01T12:30:00Z"),
                new BigDecimal("3.50"), new BigDecimal("1750.00"), BigDecimal.ZERO, new BigDecimal("1750.00"),
                "Channel deoxidation, probe compensation adjustment, and reference clock frequency verification.",
                "Normal preventative wear.",
                "Calibration certificate renewed through Sep 2027.");

        getOrCreateDowntimeLog(eqOscilloscope, mwo3, DowntimeReasonCategory.SCHEDULED_MAINTENANCE,
                Instant.parse("2026-09-01T09:00:00Z"), Instant.parse("2026-09-01T12:30:00Z"), 210,
                "Quarterly channel calibration and contact deoxidation");

        // 14. Cost & Billing
        // Invoice 1: SETTLED - August Departmental Usage
        BillingInvoice invSettled = getOrCreateInvoice("INV-2026-001", null, apitr, apitr, cse,
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31),
                new BigDecimal("1850.00"), BigDecimal.ZERO, new BigDecimal("1850.00"),
                InvoiceStatus.SETTLED, Instant.parse("2026-08-31T17:00:00Z"), LocalDate.of(2026, 9, 15),
                Instant.parse("2026-09-02T14:30:00Z"), "NEFT-SBI-892187391", "CSE Department August Research Allocation Settlement");

        // Invoice 2: ISSUED - Linked to Booking 5 (Universal Testing Machine)
        BillingInvoice invIssued = getOrCreateInvoice("INV-2026-002", null, apitr, apitr, me,
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 7),
                new BigDecimal("700.00"), BigDecimal.ZERO, new BigDecimal("700.00"),
                InvoiceStatus.ISSUED, Instant.parse("2026-09-08T10:00:00Z"), LocalDate.of(2026, 9, 30),
                null, null, "Mechanical Engineering Department UTM Utilization Invoice");

        getOrCreateLineItem(invIssued, bkCompleted3, sess3, eqUtm,
                "Universal Testing Machine - 2.0 Hours Tensile Testing",
                new BigDecimal("2.00"), new BigDecimal("350.00"), new BigDecimal("700.00"), BigDecimal.ZERO);

        // Invoice 3: DRAFT - Inter-institutional Resource Sharing with GRIU
        BillingInvoice invDraft = getOrCreateInvoice("INV-2026-003", sharingAgreement, apitr, griu, null,
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30),
                new BigDecimal("2000.00"), BigDecimal.ZERO, new BigDecimal("2000.00"),
                InvoiceStatus.DRAFT, null, LocalDate.of(2026, 10, 15),
                null, null, "GRIU Inter-Institutional Resource Sharing Agreement (Pending Final Approval)");

        // 15. Notifications (Mix of read & unread for various roles)
        getOrCreateNotification(researcher, "Booking Pending Approval",
                "Your booking request for the NVIDIA GPU Workstation (Ref: APITR-BK-001) is awaiting Lab Manager approval.",
                NotificationEventType.BOOKING_PENDING, NotificationPriority.INFO, false, null, "Booking", bkPending.getId());

        getOrCreateNotification(student1, "Booking Confirmed",
                "Your booking for the Digital Oscilloscope (Ref: APITR-BK-002) on Sep 11 has been confirmed by Lab Manager.",
                NotificationEventType.BOOKING_APPROVED, NotificationPriority.INFO, true, Instant.parse("2026-09-09T11:00:00Z"), "Booking", bkConfirmed.getId());

        getOrCreateNotification(labTech, "Work Order Assigned",
                "Work order MWO-2026-001 (CNC Milling Machine - Spindle Vibration) has been assigned to you.",
                NotificationEventType.WORK_ORDER_ASSIGNED, NotificationPriority.URGENT, false, null, "MaintenanceWorkOrder", mwo1.getId());

        getOrCreateNotification(labManager, "Equipment Under Maintenance",
                "The CNC Milling Machine (Asset: APITR-EQ-ME-001) is currently under maintenance. Spindle vibration detected.",
                NotificationEventType.MAINTENANCE_SCHEDULED, NotificationPriority.WARNING, false, null, "Equipment", eqCncMill.getId());

        getOrCreateNotification(instAdmin, "Invoice Issued",
                "Invoice INV-2026-002 for Mechanical Engineering department has been issued.",
                NotificationEventType.INVOICE_GENERATED, NotificationPriority.INFO, false, null, "BillingInvoice", invIssued.getId());

        getOrCreateNotification(deptHead, "Idle Equipment Alert",
                "Total Station (Asset: APITR-EQ-CE-001) was detected idle during standard laboratory operating hours.",
                NotificationEventType.IDLE_ALERT, NotificationPriority.INFO, true, Instant.parse("2026-09-03T18:00:00Z"), "Equipment", eqTotalStation.getId());

        return buildSummary();
    }

    // Helper methods for idempotent lookup/creation
    private Institution getOrCreateInstitution(String name, String code, String domain, String address1,
                                               String city, String state, String country, String postalCode,
                                               String email, String phone) {
        return institutionRepository.findByCode(code).orElseGet(() -> {
            Institution inst = new Institution();
            inst.setName(name);
            inst.setCode(code);
            inst.setDomain(domain);
            inst.setAddressLine1(address1);
            inst.setCity(city);
            inst.setState(state);
            inst.setCountry(country);
            inst.setPostalCode(postalCode);
            inst.setContactEmail(email);
            inst.setContactPhone(phone);
            inst.setActive(true);
            return institutionRepository.save(inst);
        });
    }

    private Department getOrCreateDepartment(Institution institution, String name, String code, String billingCode) {
        return departmentRepository.findByInstitutionIdAndCode(institution.getId(), code).orElseGet(() -> {
            Department dept = new Department();
            dept.setInstitution(institution);
            dept.setName(name);
            dept.setCode(code);
            dept.setBillingAccountCode(billingCode);
            dept.setActive(true);
            return departmentRepository.save(dept);
        });
    }

    private Map<UserRoleType, Role> loadRoles() {
        Map<UserRoleType, Role> map = new EnumMap<>(UserRoleType.class);
        for (UserRoleType type : UserRoleType.values()) {
            roleRepository.findByName(type).ifPresent(role -> map.put(type, role));
        }
        return map;
    }

    private User getOrCreateUser(Institution institution, Department department, String email,
                                 String firstName, String lastName, String phone,
                                 String passwordHash, Set<Role> userRoles) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = new User();
            user.setInstitution(institution);
            user.setDepartment(department);
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPhone(phone);
            user.setPasswordHash(passwordHash);
            user.setStatus(UserStatus.ACTIVE);
            user.setVerified(true);
            user.setRoles(new HashSet<>(userRoles));
            return userRepository.save(user);
        });
    }

    private EquipmentCategory getOrCreateCategory(String name, String description) {
        return categoryRepository.findByName(name).orElseGet(() -> {
            EquipmentCategory cat = new EquipmentCategory();
            cat.setName(name);
            cat.setDescription(description);
            return categoryRepository.save(cat);
        });
    }

    private Equipment getOrCreateEquipment(Institution institution, Department department, EquipmentCategory category,
                                          User labManager, String name, String assetTag, String serialNumber,
                                          String modelNumber, String manufacturer, String building, String room,
                                          EquipmentStatus status, String statusReason, boolean shareableExternally,
                                          BigDecimal hourlyRateInternal, BigDecimal hourlyRateExternal,
                                          int minBookingMins, int maxBookingMins, int bufferMins,
                                          boolean requiresQualification, boolean requiresApproval,
                                          LocalDate purchaseDate, BigDecimal purchaseCost, LocalDate warrantyDate) {
        return equipmentRepository.findByAssetTag(assetTag).orElseGet(() -> {
            Equipment eq = new Equipment();
            eq.setInstitution(institution);
            eq.setDepartment(department);
            eq.setCategory(category);
            eq.setPrimaryLabManager(labManager);
            eq.setName(name);
            eq.setAssetTag(assetTag);
            eq.setSerialNumber(serialNumber);
            eq.setModelNumber(modelNumber);
            eq.setManufacturer(manufacturer);
            eq.setLocationBuilding(building);
            eq.setLocationRoom(room);
            eq.setStatus(status);
            eq.setOperationalStatusReason(statusReason);
            eq.setShareableExternally(shareableExternally);
            eq.setHourlyRateInternal(hourlyRateInternal);
            eq.setHourlyRateExternal(hourlyRateExternal);
            eq.setMinBookingDurationMins(minBookingMins);
            eq.setMaxBookingDurationMins(maxBookingMins);
            eq.setBufferTimeMins(bufferMins);
            eq.setRequiresTrainingCertification(requiresQualification);
            eq.setRequiresApproval(requiresApproval);
            eq.setPurchaseDate(purchaseDate);
            eq.setPurchaseCost(purchaseCost);
            eq.setWarrantyExpiryDate(warrantyDate);
            return equipmentRepository.save(eq);
        });
    }

    private void seedSpecifications(Equipment equipment, Map<String, String> specs) {
        for (Map.Entry<String, String> entry : specs.entrySet()) {
            if (specificationRepository.findByEquipmentIdAndSpecName(equipment.getId(), entry.getKey()).isEmpty()) {
                EquipmentSpecification spec = new EquipmentSpecification();
                spec.setEquipment(equipment);
                spec.setSpecName(entry.getKey());
                spec.setSpecValue(entry.getValue());
                specificationRepository.save(spec);
            }
        }
    }

    private void getOrCreateQualification(User user, Equipment equipment, User certifier,
                                          Instant certifiedAt, Instant expiresAt,
                                          QualificationStatus status, String notes) {
        if (qualificationRepository.findByUserIdAndEquipmentId(user.getId(), equipment.getId()).isEmpty()) {
            UserEquipmentQualification qual = new UserEquipmentQualification();
            qual.setUser(user);
            qual.setEquipment(equipment);
            qual.setCertifiedBy(certifier);
            qual.setCertifiedAt(certifiedAt);
            qual.setExpiresAt(expiresAt);
            qual.setStatus(status);
            qual.setNotes(notes);
            qualificationRepository.save(qual);
        }
    }

    private ResourceSharingRequest getOrCreateSharingRequest(Institution requestingInst, Institution ownerInst,
                                                             User requestedBy, Equipment targetEquipment,
                                                             Department targetDept, String title, String justification,
                                                             LocalDate start, LocalDate end, SharingRequestStatus status,
                                                             User reviewer) {
        List<ResourceSharingRequest> existing = sharingRequestRepository.findByRequestingInstitutionIdAndStatus(requestingInst.getId(), status);
        for (ResourceSharingRequest r : existing) {
            if (r.getRequestTitle().equals(title)) {
                return r;
            }
        }
        ResourceSharingRequest req = new ResourceSharingRequest();
        req.setRequestingInstitution(requestingInst);
        req.setOwnerInstitution(ownerInst);
        req.setRequestedByUser(requestedBy);
        req.setTargetEquipment(targetEquipment);
        req.setTargetDepartment(targetDept);
        req.setRequestTitle(title);
        req.setJustification(justification);
        req.setRequestedStartDate(start);
        req.setRequestedEndDate(end);
        req.setStatus(status);
        req.setReviewedByUser(reviewer);
        req.setReviewedAt(Instant.now());
        return sharingRequestRepository.save(req);
    }

    private ResourceSharingAgreement getOrCreateSharingAgreement(ResourceSharingRequest req, String code,
                                                                 Institution requestingInst, Institution ownerInst,
                                                                 BigDecimal multiplier, int maxHours,
                                                                 LocalDate start, LocalDate end, SharingAgreementStatus status) {
        return sharingAgreementRepository.findByAgreementCode(code).orElseGet(() -> {
            ResourceSharingAgreement agreement = new ResourceSharingAgreement();
            agreement.setSharingRequest(req);
            agreement.setAgreementCode(code);
            agreement.setRequestingInstitution(requestingInst);
            agreement.setOwnerInstitution(ownerInst);
            agreement.setBillingRateMultiplier(multiplier);
            agreement.setMaxMonthlyHours(maxHours);
            agreement.setStartDate(start);
            agreement.setEndDate(end);
            agreement.setStatus(status);
            return sharingAgreementRepository.save(agreement);
        });
    }

    private SharedEquipmentAllocation getOrCreateSharedAllocation(ResourceSharingAgreement agreement, Equipment equipment,
                                                                  BigDecimal customRate, boolean isActive) {
        return sharedAllocationRepository.findBySharingAgreementIdAndEquipmentId(agreement.getId(), equipment.getId())
                .orElseGet(() -> {
                    SharedEquipmentAllocation alloc = new SharedEquipmentAllocation();
                    alloc.setSharingAgreement(agreement);
                    alloc.setEquipment(equipment);
                    alloc.setCustomHourlyRate(customRate);
                    alloc.setActive(isActive);
                    return sharedAllocationRepository.save(alloc);
                });
    }

    private Booking getOrCreateBooking(String ref, Equipment eq, User user, Department dept, Institution inst,
                                       Instant start, Instant end, BookingStatus status, BookingBillingStatus billingStatus,
                                       String purpose, String projectCode, User approver, Instant approvedAt,
                                       User canceller, Instant cancelledAt, String cancelReason,
                                       boolean isExternal, SharedEquipmentAllocation sharedAlloc,
                                       BigDecimal hourlyRate, BigDecimal estCost, BigDecimal actualCost) {
        return bookingRepository.findByBookingReference(ref).orElseGet(() -> {
            Booking b = new Booking();
            b.setBookingReference(ref);
            b.setEquipment(eq);
            b.setUser(user);
            b.setDepartment(dept);
            b.setInstitution(inst);
            b.setStartTime(start);
            b.setEndTime(end);
            b.setStatus(status);
            b.setBillingStatus(billingStatus);
            b.setPurpose(purpose);
            b.setProjectCode(projectCode);
            b.setApprovedByUser(approver);
            b.setApprovedAt(approvedAt);
            b.setCancelledByUser(canceller);
            b.setCancelledAt(cancelledAt);
            b.setCancellationReason(cancelReason);
            b.setExternalBooking(isExternal);
            b.setSharedEquipmentAllocation(sharedAlloc);
            b.setBaseHourlyRate(hourlyRate);
            b.setEstimatedCost(estCost);
            b.setActualCost(actualCost);
            return bookingRepository.save(b);
        });
    }

    private EquipmentUsageSession getOrCreateUsageSession(Booking booking, Equipment equipment, User user,
                                                          Instant checkIn, Instant checkOut,
                                                          Integer actualMins, Integer schedMins,
                                                          SessionStatus status, String notes) {
        return sessionRepository.findByBookingId(booking.getId()).orElseGet(() -> {
            EquipmentUsageSession sess = new EquipmentUsageSession();
            sess.setBooking(booking);
            sess.setEquipment(equipment);
            sess.setUser(user);
            sess.setCheckedInAt(checkIn);
            sess.setCheckedOutAt(checkOut);
            sess.setActualDurationMinutes(actualMins);
            sess.setScheduledDurationMinutes(schedMins);
            sess.setSessionStatus(status);
            sess.setNotes(notes);
            return sessionRepository.save(sess);
        });
    }

    private void getOrCreateIdleEvent(Equipment eq, Booking bk, EquipmentUsageSession sess,
                                      IdleDetectionSource source, Instant start, Instant end,
                                      Integer durationMins, IdleEventStatus status, User loggedBy, String notes) {
        List<EquipmentIdleEvent> existing = idleEventRepository.findByEquipmentIdAndStatus(eq.getId(), status);
        boolean found = existing.stream().anyMatch(e -> e.getIdleStartTime().equals(start));
        if (!found) {
            EquipmentIdleEvent event = new EquipmentIdleEvent();
            event.setEquipment(eq);
            event.setBooking(bk);
            event.setUsageSession(sess);
            event.setDetectionSource(source);
            event.setIdleStartTime(start);
            event.setIdleEndTime(end);
            event.setIdleDurationMinutes(durationMins);
            event.setStatus(status);
            event.setLoggedByUser(loggedBy);
            event.setNotes(notes);
            idleEventRepository.save(event);
        }
    }

    private MaintenanceRequest getOrCreateMaintenanceRequest(String reqNum, Equipment eq, User reporter,
                                                             MaintenancePriority priority, String title, String desc,
                                                             MaintenanceRequestStatus status, User triager, Instant triagedAt) {
        return maintenanceRequestRepository.findByRequestNumber(reqNum).orElseGet(() -> {
            MaintenanceRequest req = new MaintenanceRequest();
            req.setRequestNumber(reqNum);
            req.setEquipment(eq);
            req.setReportedByUser(reporter);
            req.setPriority(priority);
            req.setIssueTitle(title);
            req.setIssueDescription(desc);
            req.setStatus(status);
            req.setTriagedByUser(triager);
            req.setTriagedAt(triagedAt);
            return maintenanceRequestRepository.save(req);
        });
    }

    private MaintenanceWorkOrder getOrCreateWorkOrder(String woNum, MaintenanceRequest req, Equipment eq, User tech,
                                                      WorkOrderType type, MaintenancePriority priority, WorkOrderStatus status,
                                                      Instant schedStart, Instant schedEnd, Instant actStart, Instant actEnd,
                                                      BigDecimal laborHrs, BigDecimal laborCost, BigDecimal partsCost, BigDecimal totalCost,
                                                      String summary, String rootCause, String notes) {
        return workOrderRepository.findByWorkOrderNumber(woNum).orElseGet(() -> {
            MaintenanceWorkOrder wo = new MaintenanceWorkOrder();
            wo.setWorkOrderNumber(woNum);
            wo.setMaintenanceRequest(req);
            wo.setEquipment(eq);
            wo.setAssignedTechnician(tech);
            wo.setType(type);
            wo.setPriority(priority);
            wo.setStatus(status);
            wo.setScheduledStart(schedStart);
            wo.setScheduledEnd(schedEnd);
            wo.setActualStart(actStart);
            wo.setActualEnd(actEnd);
            wo.setLaborHours(laborHrs);
            wo.setLaborCost(laborCost);
            wo.setPartsCost(partsCost);
            wo.setTotalCost(totalCost);
            wo.setWorkPerformedSummary(summary);
            wo.setFailureRootCause(rootCause);
            wo.setResolutionNotes(notes);
            return workOrderRepository.save(wo);
        });
    }

    private void getOrCreateDowntimeLog(Equipment eq, MaintenanceWorkOrder wo, DowntimeReasonCategory category,
                                        Instant start, Instant end, Integer durationMins, String desc) {
        List<EquipmentDowntimeLog> existing = downtimeLogRepository.findByEquipmentId(eq.getId());
        boolean found = existing.stream().anyMatch(d -> d.getDowntimeStart().equals(start));
        if (!found) {
            EquipmentDowntimeLog log = new EquipmentDowntimeLog();
            log.setEquipment(eq);
            log.setWorkOrder(wo);
            log.setReasonCategory(category);
            log.setDowntimeStart(start);
            log.setDowntimeEnd(end);
            log.setDurationMinutes(durationMins);
            log.setDescription(desc);
            downtimeLogRepository.save(log);
        }
    }

    private BillingInvoice getOrCreateInvoice(String invNum, ResourceSharingAgreement agreement,
                                              Institution issuingInst, Institution billedInst, Department billedDept,
                                              LocalDate periodStart, LocalDate periodEnd,
                                              BigDecimal subtotal, BigDecimal discount, BigDecimal total,
                                              InvoiceStatus status, Instant issuedAt, LocalDate dueDate,
                                              Instant paidAt, String paymentRef, String notes) {
        return invoiceRepository.findByInvoiceNumber(invNum).orElseGet(() -> {
            BillingInvoice inv = new BillingInvoice();
            inv.setInvoiceNumber(invNum);
            inv.setSharingAgreement(agreement);
            inv.setIssuingInstitution(issuingInst);
            inv.setBilledInstitution(billedInst);
            inv.setBilledDepartment(billedDept);
            inv.setBillingPeriodStart(periodStart);
            inv.setBillingPeriodEnd(periodEnd);
            inv.setSubtotalAmount(subtotal);
            inv.setDiscountAmount(discount);
            inv.setTotalAmount(total);
            inv.setStatus(status);
            inv.setIssuedAt(issuedAt);
            inv.setDueDate(dueDate);
            inv.setPaidAt(paidAt);
            inv.setPaymentReference(paymentRef);
            inv.setNotes(notes);
            return invoiceRepository.save(inv);
        });
    }

    private void getOrCreateLineItem(BillingInvoice inv, Booking booking, EquipmentUsageSession sess,
                                     Equipment eq, String desc, BigDecimal billableHrs,
                                     BigDecimal rate, BigDecimal totalCost, BigDecimal penalty) {
        if (invoiceLineItemRepository.findByBookingId(booking.getId()).isEmpty()) {
            InvoiceLineItem item = new InvoiceLineItem();
            item.setInvoice(inv);
            item.setBooking(booking);
            item.setUsageSession(sess);
            item.setEquipment(eq);
            item.setDescription(desc);
            item.setBillableHours(billableHrs);
            item.setHourlyRate(rate);
            item.setTotalLineCost(totalCost);
            item.setPenaltyAmount(penalty);
            invoiceLineItemRepository.save(item);
        }
    }

    private void getOrCreateNotification(User user, String title, String message,
                                         NotificationEventType type, NotificationPriority priority,
                                         boolean isRead, Instant readAt, String relType, Long relId) {
        List<Notification> existing = notificationRepository.findByUserId(user.getId());
        boolean found = existing.stream().anyMatch(n -> n.getTitle().equals(title));
        if (!found) {
            Notification notif = new Notification();
            notif.setUser(user);
            notif.setTitle(title);
            notif.setMessage(message);
            notif.setEventType(type);
            notif.setPriority(priority);
            notif.setRead(isRead);
            notif.setReadAt(readAt);
            notif.setRelatedEntityType(relType);
            notif.setRelatedEntityId(relId);
            notificationRepository.save(notif);
        }
    }

    public DemoDataSummary buildSummary() {
        return new DemoDataSummary(
                institutionRepository.count(),
                departmentRepository.count(),
                userRepository.count(),
                equipmentRepository.count(),
                bookingRepository.count(),
                sessionRepository.count(),
                idleEventRepository.count(),
                maintenanceRequestRepository.count(),
                workOrderRepository.count(),
                sharingAgreementRepository.count(),
                invoiceRepository.count(),
                notificationRepository.count()
        );
    }

    public record DemoDataSummary(
            long institutionCount,
            long departmentCount,
            long userCount,
            long equipmentCount,
            long bookingCount,
            long sessionCount,
            long idleEventCount,
            long maintenanceRequestCount,
            long workOrderCount,
            long sharingAgreementCount,
            long invoiceCount,
            long notificationCount
    ) {}
}
