-- =============================================================================
-- Lab Resource Utilization Platform - Database Schema
-- Target Engine: MySQL 8.0+
-- Design Specification: Task 1B.2 (Final Approved Design)
-- =============================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------------------------------
-- 1. INSTITUTION & DEPARTMENT DOMAIN
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS institutions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL,
    domain VARCHAR(100) NULL,
    address_line1 VARCHAR(255) NULL,
    address_line2 VARCHAR(255) NULL,
    city VARCHAR(100) NULL,
    state VARCHAR(100) NULL,
    country VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NULL,
    contact_email VARCHAR(150) NOT NULL,
    contact_phone VARCHAR(50) NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT uq_institutions_name UNIQUE (name),
    CONSTRAINT uq_institutions_code UNIQUE (code),
    INDEX idx_institutions_domain (domain),
    INDEX idx_institutions_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    institution_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    code VARCHAR(50) NOT NULL,
    head_user_id BIGINT NULL,
    billing_account_code VARCHAR(100) NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT uq_inst_dept_code UNIQUE (institution_id, code),
    CONSTRAINT uq_inst_dept_name UNIQUE (institution_id, name),
    CONSTRAINT uq_dept_id_institution UNIQUE (id, institution_id),
    CONSTRAINT fk_dept_institution FOREIGN KEY (institution_id) REFERENCES institutions(id) ON DELETE RESTRICT,
    INDEX idx_dept_institution_id (institution_id),
    INDEX idx_dept_head_user_id (head_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 2. USERS & ROLES DOMAIN
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(255) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT uq_roles_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    institution_id BIGINT NOT NULL,
    department_id BIGINT NULL,
    email VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(50) NULL,
    status ENUM('ACTIVE', 'PENDING_APPROVAL', 'SUSPENDED', 'DEACTIVATED') NOT NULL DEFAULT 'ACTIVE',
    is_verified TINYINT(1) NOT NULL DEFAULT 0,
    last_login_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at DATETIME(6) NULL,
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT uq_users_id_institution UNIQUE (id, institution_id),
    CONSTRAINT fk_users_institution FOREIGN KEY (institution_id) REFERENCES institutions(id) ON DELETE RESTRICT,
    CONSTRAINT fk_users_department FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE RESTRICT,
    CONSTRAINT fk_users_dept_inst FOREIGN KEY (department_id, institution_id) REFERENCES departments(id, institution_id) ON DELETE RESTRICT,
    INDEX idx_users_inst_dept (institution_id, department_id),
    INDEX idx_users_status (status),
    INDEX idx_users_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Safe execution of circular FK post-users creation
ALTER TABLE departments
    ADD CONSTRAINT fk_dept_head_user FOREIGN KEY (head_user_id) REFERENCES users(id) ON DELETE SET NULL;

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT,
    INDEX idx_ur_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 3. EQUIPMENT DOMAIN
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS equipment_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT uq_eq_cat_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS equipment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    institution_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    primary_lab_manager_id BIGINT NULL,
    name VARCHAR(200) NOT NULL,
    asset_tag VARCHAR(100) NOT NULL,
    serial_number VARCHAR(100) NOT NULL,
    model_number VARCHAR(100) NULL,
    manufacturer VARCHAR(150) NULL,
    location_building VARCHAR(100) NOT NULL,
    location_room VARCHAR(50) NOT NULL,
    status ENUM('AVAILABLE', 'IN_USE', 'UNDER_MAINTENANCE', 'OUT_OF_SERVICE', 'RETIRED') NOT NULL DEFAULT 'AVAILABLE',
    operational_status_reason VARCHAR(255) NULL,
    is_shareable_externally TINYINT(1) NOT NULL DEFAULT 0,
    hourly_rate_internal DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    hourly_rate_external DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    min_booking_duration_mins INT NOT NULL DEFAULT 30,
    max_booking_duration_mins INT NOT NULL DEFAULT 480,
    buffer_time_mins INT NOT NULL DEFAULT 15,
    requires_training_certification TINYINT(1) NOT NULL DEFAULT 0,
    requires_approval TINYINT(1) NOT NULL DEFAULT 0,
    purchase_date DATE NULL,
    purchase_cost DECIMAL(12,2) NULL,
    warranty_expiry_date DATE NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at DATETIME(6) NULL,
    CONSTRAINT uq_eq_asset_tag UNIQUE (asset_tag),
    CONSTRAINT uq_eq_serial_number UNIQUE (serial_number),
    CONSTRAINT uq_eq_id_institution UNIQUE (id, institution_id),
    CONSTRAINT fk_eq_institution FOREIGN KEY (institution_id) REFERENCES institutions(id) ON DELETE RESTRICT,
    CONSTRAINT fk_eq_department FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE RESTRICT,
    CONSTRAINT fk_eq_dept_inst FOREIGN KEY (department_id, institution_id) REFERENCES departments(id, institution_id) ON DELETE RESTRICT,
    CONSTRAINT fk_eq_category FOREIGN KEY (category_id) REFERENCES equipment_categories(id) ON DELETE RESTRICT,
    CONSTRAINT fk_eq_manager FOREIGN KEY (primary_lab_manager_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_eq_search (institution_id, department_id, category_id, status),
    INDEX idx_eq_shareable (is_shareable_externally),
    INDEX idx_eq_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS equipment_specifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    equipment_id BIGINT NOT NULL,
    spec_name VARCHAR(100) NOT NULL,
    spec_value VARCHAR(255) NOT NULL,
    unit VARCHAR(50) NULL,
    CONSTRAINT uq_eq_spec UNIQUE (equipment_id, spec_name),
    CONSTRAINT fk_eq_specs_equip FOREIGN KEY (equipment_id) REFERENCES equipment(id) ON DELETE CASCADE,
    INDEX idx_eq_specs_equip (equipment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_equipment_qualifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    equipment_id BIGINT NOT NULL,
    certified_by_user_id BIGINT NOT NULL,
    certified_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    expires_at DATETIME(6) NULL,
    status ENUM('ACTIVE', 'REVOKED', 'EXPIRED') NOT NULL DEFAULT 'ACTIVE',
    notes TEXT NULL,
    CONSTRAINT uq_user_equip_qual UNIQUE (user_id, equipment_id),
    CONSTRAINT fk_ueq_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ueq_equip FOREIGN KEY (equipment_id) REFERENCES equipment(id) ON DELETE CASCADE,
    CONSTRAINT fk_ueq_certifier FOREIGN KEY (certified_by_user_id) REFERENCES users(id) ON DELETE RESTRICT,
    INDEX idx_ueq_lookup (user_id, equipment_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 4. INTER-INSTITUTION RESOURCE SHARING DOMAIN
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS resource_sharing_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    requesting_institution_id BIGINT NOT NULL,
    owner_institution_id BIGINT NOT NULL,
    requested_by_user_id BIGINT NOT NULL,
    target_equipment_id BIGINT NULL,
    target_department_id BIGINT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'EXPIRED', 'REVOKED') NOT NULL DEFAULT 'PENDING',
    request_title VARCHAR(200) NOT NULL,
    justification TEXT NOT NULL,
    requested_start_date DATE NOT NULL,
    requested_end_date DATE NOT NULL,
    reviewed_by_user_id BIGINT NULL,
    reviewed_at DATETIME(6) NULL,
    rejection_reason TEXT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_sr_req_inst FOREIGN KEY (requesting_institution_id) REFERENCES institutions(id) ON DELETE RESTRICT,
    CONSTRAINT fk_sr_own_inst FOREIGN KEY (owner_institution_id) REFERENCES institutions(id) ON DELETE RESTRICT,
    CONSTRAINT fk_sr_user FOREIGN KEY (requested_by_user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_sr_equip FOREIGN KEY (target_equipment_id) REFERENCES equipment(id) ON DELETE SET NULL,
    CONSTRAINT fk_sr_dept FOREIGN KEY (target_department_id) REFERENCES departments(id) ON DELETE SET NULL,
    CONSTRAINT fk_sr_reviewer FOREIGN KEY (reviewed_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_sr_pair (requesting_institution_id, owner_institution_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS resource_sharing_agreements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sharing_request_id BIGINT NOT NULL,
    agreement_code VARCHAR(50) NOT NULL,
    requesting_institution_id BIGINT NOT NULL,
    owner_institution_id BIGINT NOT NULL,
    billing_rate_multiplier DECIMAL(4,2) NOT NULL DEFAULT 1.00,
    max_monthly_hours INT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status ENUM('ACTIVE', 'SUSPENDED', 'TERMINATED', 'EXPIRED') NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT uq_sa_code UNIQUE (agreement_code),
    CONSTRAINT uq_sa_request_id UNIQUE (sharing_request_id),
    CONSTRAINT fk_sa_request FOREIGN KEY (sharing_request_id) REFERENCES resource_sharing_requests(id) ON DELETE RESTRICT,
    CONSTRAINT fk_sa_req_inst FOREIGN KEY (requesting_institution_id) REFERENCES institutions(id) ON DELETE RESTRICT,
    CONSTRAINT fk_sa_own_inst FOREIGN KEY (owner_institution_id) REFERENCES institutions(id) ON DELETE RESTRICT,
    INDEX idx_sa_institutions (requesting_institution_id, owner_institution_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS shared_equipment_allocations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sharing_agreement_id BIGINT NOT NULL,
    equipment_id BIGINT NOT NULL,
    custom_hourly_rate DECIMAL(10,2) NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT uq_sea_agreement_equip UNIQUE (sharing_agreement_id, equipment_id),
    CONSTRAINT fk_sea_agreement FOREIGN KEY (sharing_agreement_id) REFERENCES resource_sharing_agreements(id) ON DELETE CASCADE,
    CONSTRAINT fk_sea_equip FOREIGN KEY (equipment_id) REFERENCES equipment(id) ON DELETE RESTRICT,
    INDEX idx_sea_equip (equipment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 5. BOOKING & SCHEDULING DOMAIN
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS recurring_booking_series (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    equipment_id BIGINT NOT NULL,
    recurrence_pattern ENUM('DAILY', 'WEEKLY', 'BIWEEKLY', 'MONTHLY') NOT NULL,
    days_of_week VARCHAR(30) NULL,
    series_start_date DATE NOT NULL,
    series_end_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status ENUM('ACTIVE', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_rbs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_rbs_equip FOREIGN KEY (equipment_id) REFERENCES equipment(id) ON DELETE RESTRICT,
    INDEX idx_rbs_user (user_id),
    INDEX idx_rbs_equip (equipment_id),
    INDEX idx_rbs_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_reference VARCHAR(50) NOT NULL,
    equipment_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    institution_id BIGINT NOT NULL,
    recurring_series_id BIGINT NULL,
    shared_equipment_allocation_id BIGINT NULL,
    start_time DATETIME(6) NOT NULL,
    end_time DATETIME(6) NOT NULL,
    status ENUM('PENDING_APPROVAL', 'CONFIRMED', 'IN_USE', 'COMPLETED', 'CANCELLED', 'NO_SHOW') NOT NULL DEFAULT 'PENDING_APPROVAL',
    billing_status ENUM('UNBILLED', 'INVOICED', 'SETTLED', 'WAIVED') NOT NULL DEFAULT 'UNBILLED',
    purpose TEXT NOT NULL,
    project_code VARCHAR(100) NULL,
    approved_by_user_id BIGINT NULL,
    approved_at DATETIME(6) NULL,
    rejection_reason TEXT NULL,
    cancellation_reason TEXT NULL,
    cancelled_at DATETIME(6) NULL,
    cancelled_by_user_id BIGINT NULL,
    is_external_booking TINYINT(1) NOT NULL DEFAULT 0,
    base_hourly_rate DECIMAL(10,2) NOT NULL,
    estimated_cost DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    actual_cost DECIMAL(10,2) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT uq_bk_reference UNIQUE (booking_reference),
    CONSTRAINT fk_bk_equip FOREIGN KEY (equipment_id) REFERENCES equipment(id) ON DELETE RESTRICT,
    CONSTRAINT fk_bk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_bk_dept FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE RESTRICT,
    CONSTRAINT fk_bk_inst FOREIGN KEY (institution_id) REFERENCES institutions(id) ON DELETE RESTRICT,
    CONSTRAINT fk_bk_user_inst FOREIGN KEY (user_id, institution_id) REFERENCES users(id, institution_id) ON DELETE RESTRICT,
    CONSTRAINT fk_bk_dept_inst FOREIGN KEY (department_id, institution_id) REFERENCES departments(id, institution_id) ON DELETE RESTRICT,
    CONSTRAINT fk_bk_recurring FOREIGN KEY (recurring_series_id) REFERENCES recurring_booking_series(id) ON DELETE SET NULL,
    CONSTRAINT fk_bk_shared_alloc FOREIGN KEY (shared_equipment_allocation_id) REFERENCES shared_equipment_allocations(id) ON DELETE RESTRICT,
    CONSTRAINT fk_bk_approver FOREIGN KEY (approved_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_bk_canceller FOREIGN KEY (cancelled_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_bk_calendar (equipment_id, status, start_time, end_time),
    INDEX idx_bk_user (user_id),
    INDEX idx_bk_dept_inst (institution_id, department_id),
    INDEX idx_bk_billing (billing_status),
    INDEX idx_bk_shared (shared_equipment_allocation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS booking_waitlists (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    equipment_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    desired_start_time DATETIME(6) NOT NULL,
    desired_end_time DATETIME(6) NOT NULL,
    position INT NOT NULL DEFAULT 1,
    status ENUM('WAITING', 'NOTIFIED', 'CONVERTED', 'EXPIRED', 'CANCELLED') NOT NULL DEFAULT 'WAITING',
    notified_at DATETIME(6) NULL,
    offer_expiry_time DATETIME(6) NULL,
    converted_booking_id BIGINT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_wl_equip FOREIGN KEY (equipment_id) REFERENCES equipment(id) ON DELETE CASCADE,
    CONSTRAINT fk_wl_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_wl_booking FOREIGN KEY (converted_booking_id) REFERENCES bookings(id) ON DELETE SET NULL,
    INDEX idx_wl_queue (equipment_id, desired_start_time, status, position)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 6. UTILIZATION & USAGE TRACKING DOMAIN
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS equipment_usage_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NULL,
    equipment_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    checked_in_at DATETIME(6) NOT NULL,
    checked_out_at DATETIME(6) NULL,
    actual_duration_minutes INT NULL,
    scheduled_duration_minutes INT NOT NULL,
    session_status ENUM('ACTIVE', 'COMPLETED', 'TERMINATED_EARLY', 'AUTO_CLOSED') NOT NULL DEFAULT 'ACTIVE',
    notes TEXT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_us_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE SET NULL,
    CONSTRAINT fk_us_equip FOREIGN KEY (equipment_id) REFERENCES equipment(id) ON DELETE RESTRICT,
    CONSTRAINT fk_us_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    INDEX idx_us_booking (booking_id),
    INDEX idx_us_equip_dates (equipment_id, checked_in_at, checked_out_at),
    INDEX idx_us_status (session_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS equipment_idle_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    equipment_id BIGINT NOT NULL,
    booking_id BIGINT NULL,
    usage_session_id BIGINT NULL,
    detection_source ENUM('SCHEDULED_INSPECTION_CRON', 'BOOKING_NO_SHOW', 'MANUAL_LAB_AUDIT') NOT NULL,
    idle_start_time DATETIME(6) NOT NULL,
    idle_end_time DATETIME(6) NULL,
    idle_duration_minutes INT NULL,
    status ENUM('ONGOING', 'RESOLVED', 'ACKNOWLEDGED') NOT NULL DEFAULT 'ONGOING',
    logged_by_user_id BIGINT NULL,
    notes VARCHAR(255) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_ie_equip FOREIGN KEY (equipment_id) REFERENCES equipment(id) ON DELETE RESTRICT,
    CONSTRAINT fk_ie_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE SET NULL,
    CONSTRAINT fk_ie_session FOREIGN KEY (usage_session_id) REFERENCES equipment_usage_sessions(id) ON DELETE SET NULL,
    CONSTRAINT fk_ie_logger FOREIGN KEY (logged_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_ie_equip_dates (equipment_id, idle_start_time),
    INDEX idx_ie_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 7. MAINTENANCE & DOWNTIME DOMAIN
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS maintenance_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_number VARCHAR(50) NOT NULL,
    equipment_id BIGINT NOT NULL,
    reported_by_user_id BIGINT NOT NULL,
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') NOT NULL DEFAULT 'MEDIUM',
    issue_title VARCHAR(200) NOT NULL,
    issue_description TEXT NOT NULL,
    status ENUM('SUBMITTED', 'TRIAGED', 'WORK_ORDER_CREATED', 'RESOLVED', 'REJECTED') NOT NULL DEFAULT 'SUBMITTED',
    triaged_by_user_id BIGINT NULL,
    triaged_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT uq_mr_number UNIQUE (request_number),
    CONSTRAINT fk_mr_equip FOREIGN KEY (equipment_id) REFERENCES equipment(id) ON DELETE RESTRICT,
    CONSTRAINT fk_mr_reporter FOREIGN KEY (reported_by_user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_mr_triager FOREIGN KEY (triaged_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_mr_equip_status (equipment_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS maintenance_work_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    work_order_number VARCHAR(50) NOT NULL,
    maintenance_request_id BIGINT NULL,
    equipment_id BIGINT NOT NULL,
    assigned_technician_id BIGINT NULL,
    type ENUM('CORRECTIVE', 'PREVENTIVE', 'EMERGENCY', 'OVERHAUL', 'DECOMMISSION') NOT NULL,
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') NOT NULL DEFAULT 'MEDIUM',
    status ENUM('SCHEDULED', 'IN_PROGRESS', 'WAITING_FOR_PARTS', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'SCHEDULED',
    scheduled_start DATETIME(6) NOT NULL,
    scheduled_end DATETIME(6) NOT NULL,
    actual_start DATETIME(6) NULL,
    actual_end DATETIME(6) NULL,
    labor_hours DECIMAL(6,2) NOT NULL DEFAULT 0.00,
    labor_cost DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    parts_cost DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_cost DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    work_performed_summary TEXT NULL,
    failure_root_cause TEXT NULL,
    resolution_notes TEXT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT uq_mwo_number UNIQUE (work_order_number),
    CONSTRAINT fk_mwo_request FOREIGN KEY (maintenance_request_id) REFERENCES maintenance_requests(id) ON DELETE SET NULL,
    CONSTRAINT fk_mwo_equip FOREIGN KEY (equipment_id) REFERENCES equipment(id) ON DELETE RESTRICT,
    CONSTRAINT fk_mwo_tech FOREIGN KEY (assigned_technician_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_mwo_equip_status (equipment_id, status),
    INDEX idx_mwo_tech (assigned_technician_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS equipment_downtime_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    equipment_id BIGINT NOT NULL,
    work_order_id BIGINT NULL,
    reason_category ENUM('UNSCHEDULED_BREAKDOWN', 'SCHEDULED_MAINTENANCE', 'CALIBRATION', 'FACILITY_OUTAGE', 'SAFETY_HOLD') NOT NULL,
    downtime_start DATETIME(6) NOT NULL,
    downtime_end DATETIME(6) NULL,
    duration_minutes INT NULL,
    description VARCHAR(255) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_dt_equip FOREIGN KEY (equipment_id) REFERENCES equipment(id) ON DELETE RESTRICT,
    CONSTRAINT fk_dt_work_order FOREIGN KEY (work_order_id) REFERENCES maintenance_work_orders(id) ON DELETE SET NULL,
    INDEX idx_dt_equip_window (equipment_id, downtime_start, downtime_end)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 8. CALIBRATION & CERTIFICATION DOMAIN
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS equipment_calibrations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    calibration_reference VARCHAR(50) NOT NULL,
    equipment_id BIGINT NOT NULL,
    performed_by_user_id BIGINT NULL,
    external_vendor_name VARCHAR(150) NULL,
    calibration_date DATE NOT NULL,
    next_due_date DATE NOT NULL,
    frequency_months INT NOT NULL DEFAULT 12,
    result ENUM('PASSED', 'PASSED_WITH_LIMITATIONS', 'FAILED') NOT NULL,
    calibration_standard VARCHAR(100) NULL,
    measured_deviation VARCHAR(100) NULL,
    certificate_number VARCHAR(100) NULL,
    certificate_document_url VARCHAR(500) NULL,
    status ENUM('VALID', 'DUE_SOON', 'EXPIRED', 'SUPERSEDED') NOT NULL DEFAULT 'VALID',
    notes TEXT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT uq_cal_ref UNIQUE (calibration_reference),
    CONSTRAINT fk_cal_equip FOREIGN KEY (equipment_id) REFERENCES equipment(id) ON DELETE RESTRICT,
    CONSTRAINT fk_cal_technician FOREIGN KEY (performed_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_cal_expiry (equipment_id, next_due_date, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS equipment_certifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    equipment_id BIGINT NOT NULL,
    certification_type VARCHAR(100) NOT NULL,
    certificate_number VARCHAR(100) NOT NULL,
    issuing_authority VARCHAR(150) NOT NULL,
    issued_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    is_mandatory TINYINT(1) NOT NULL DEFAULT 0,
    status ENUM('ACTIVE', 'EXPIRING_SOON', 'EXPIRED', 'REVOKED') NOT NULL DEFAULT 'ACTIVE',
    certificate_document_url VARCHAR(500) NULL,
    notes TEXT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_cert_equip FOREIGN KEY (equipment_id) REFERENCES equipment(id) ON DELETE RESTRICT,
    INDEX idx_cert_expiry (equipment_id, is_mandatory, expiry_date, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 9. COST & BILLING DOMAIN
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS billing_invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_number VARCHAR(50) NOT NULL,
    sharing_agreement_id BIGINT NULL,
    issuing_institution_id BIGINT NOT NULL,
    billed_institution_id BIGINT NOT NULL,
    billed_department_id BIGINT NULL,
    billing_period_start DATE NOT NULL,
    billing_period_end DATE NOT NULL,
    subtotal_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    status ENUM('DRAFT', 'ISSUED', 'DISPUTED', 'SETTLED', 'WRITTEN_OFF') NOT NULL DEFAULT 'DRAFT',
    issued_at DATETIME(6) NULL,
    due_date DATE NULL,
    paid_at DATETIME(6) NULL,
    payment_reference VARCHAR(100) NULL,
    notes TEXT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT uq_inv_number UNIQUE (invoice_number),
    CONSTRAINT fk_inv_agreement FOREIGN KEY (sharing_agreement_id) REFERENCES resource_sharing_agreements(id) ON DELETE RESTRICT,
    CONSTRAINT fk_inv_issuing_inst FOREIGN KEY (issuing_institution_id) REFERENCES institutions(id) ON DELETE RESTRICT,
    CONSTRAINT fk_inv_billed_inst FOREIGN KEY (billed_institution_id) REFERENCES institutions(id) ON DELETE RESTRICT,
    CONSTRAINT fk_inv_billed_dept FOREIGN KEY (billed_department_id) REFERENCES departments(id) ON DELETE RESTRICT,
    INDEX idx_inv_search (issuing_institution_id, billed_institution_id, billed_department_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS invoice_line_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    booking_id BIGINT NOT NULL,
    usage_session_id BIGINT NULL,
    equipment_id BIGINT NOT NULL,
    description VARCHAR(255) NOT NULL,
    billable_hours DECIMAL(6,2) NOT NULL,
    hourly_rate DECIMAL(10,2) NOT NULL,
    total_line_cost DECIMAL(10,2) NOT NULL,
    penalty_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT uq_ili_booking UNIQUE (booking_id),
    CONSTRAINT fk_ili_invoice FOREIGN KEY (invoice_id) REFERENCES billing_invoices(id) ON DELETE CASCADE,
    CONSTRAINT fk_ili_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE RESTRICT,
    CONSTRAINT fk_ili_session FOREIGN KEY (usage_session_id) REFERENCES equipment_usage_sessions(id) ON DELETE SET NULL,
    CONSTRAINT fk_ili_equip FOREIGN KEY (equipment_id) REFERENCES equipment(id) ON DELETE RESTRICT,
    INDEX idx_ili_invoice (invoice_id),
    INDEX idx_ili_equip (equipment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 10. NOTIFICATIONS DOMAIN
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    event_type ENUM('BOOKING_PENDING', 'BOOKING_APPROVED', 'BOOKING_REJECTED', 'BOOKING_CANCELLED', 'BOOKING_NO_SHOW', 'WAITLIST_AVAILABLE', 'MAINTENANCE_SCHEDULED', 'WORK_ORDER_ASSIGNED', 'CALIBRATION_EXPIRING_SOON', 'CALIBRATION_EXPIRED', 'CERTIFICATION_EXPIRING_SOON', 'CERTIFICATION_EXPIRED', 'SHARING_REQUEST_RECEIVED', 'SHARING_REQUEST_APPROVED', 'IDLE_ALERT', 'INVOICE_GENERATED') NOT NULL,
    priority ENUM('INFO', 'WARNING', 'URGENT', 'CRITICAL') NOT NULL DEFAULT 'INFO',
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    read_at DATETIME(6) NULL,
    related_entity_type VARCHAR(50) NULL,
    related_entity_id BIGINT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_notif_user_unread (user_id, is_read, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS notification_preferences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    event_category VARCHAR(50) NOT NULL,
    in_app_enabled TINYINT(1) NOT NULL DEFAULT 1,
    email_enabled TINYINT(1) NOT NULL DEFAULT 1,
    sms_enabled TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT uq_np_user_category UNIQUE (user_id, event_category),
    CONSTRAINT fk_np_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 11. AUDIT / HISTORY DOMAIN
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor_user_id BIGINT NULL,
    action VARCHAR(50) NOT NULL,
    entity_name VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    ip_address VARCHAR(45) NULL,
    user_agent VARCHAR(255) NULL,
    old_values JSON NULL,
    new_values JSON NULL,
    change_reason TEXT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_al_actor FOREIGN KEY (actor_user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_al_entity (entity_name, entity_id),
    INDEX idx_al_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
