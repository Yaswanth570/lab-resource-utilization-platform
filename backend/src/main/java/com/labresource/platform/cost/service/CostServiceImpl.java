package com.labresource.platform.cost.service;

import com.labresource.platform.booking.Booking;
import com.labresource.platform.booking.BookingBillingStatus;
import com.labresource.platform.booking.repository.BookingRepository;
import com.labresource.platform.common.exception.DuplicateResourceException;
import com.labresource.platform.common.exception.InvalidOperationException;
import com.labresource.platform.common.exception.ResourceNotFoundException;
import com.labresource.platform.cost.BillingInvoice;
import com.labresource.platform.cost.InvoiceLineItem;
import com.labresource.platform.cost.InvoiceStatus;
import com.labresource.platform.cost.repository.BillingInvoiceRepository;
import com.labresource.platform.cost.repository.InvoiceLineItemRepository;
import com.labresource.platform.cost.web.DepartmentCostSummaryResponse;
import com.labresource.platform.cost.web.UsageCostResponse;
import com.labresource.platform.department.Department;
import com.labresource.platform.department.repository.DepartmentRepository;
import com.labresource.platform.equipment.Equipment;
import com.labresource.platform.equipment.repository.EquipmentRepository;
import com.labresource.platform.institution.Institution;
import com.labresource.platform.institution.repository.InstitutionRepository;
import com.labresource.platform.sharing.ResourceSharingAgreement;
import com.labresource.platform.sharing.repository.ResourceSharingAgreementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
@Transactional
public class CostServiceImpl implements CostService {

    private final BillingInvoiceRepository invoiceRepository;
    private final InvoiceLineItemRepository lineItemRepository;
    private final BookingRepository bookingRepository;
    private final InstitutionRepository institutionRepository;
    private final DepartmentRepository departmentRepository;
    private final ResourceSharingAgreementRepository sharingAgreementRepository;
    private final EquipmentRepository equipmentRepository;

    public CostServiceImpl(BillingInvoiceRepository invoiceRepository,
                           InvoiceLineItemRepository lineItemRepository,
                           BookingRepository bookingRepository,
                           InstitutionRepository institutionRepository,
                           DepartmentRepository departmentRepository,
                           ResourceSharingAgreementRepository sharingAgreementRepository,
                           EquipmentRepository equipmentRepository) {
        this.invoiceRepository = invoiceRepository;
        this.lineItemRepository = lineItemRepository;
        this.bookingRepository = bookingRepository;
        this.institutionRepository = institutionRepository;
        this.departmentRepository = departmentRepository;
        this.sharingAgreementRepository = sharingAgreementRepository;
        this.equipmentRepository = equipmentRepository;
    }

    // ==========================================
    // Usage Cost Operations
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<UsageCostResponse> listUsageCosts(Long institutionId, Long departmentId, Long equipmentId, BookingBillingStatus billingStatus) {
        List<Booking> bookings;
        if (institutionId != null) {
            bookings = bookingRepository.findByInstitutionId(institutionId);
        } else {
            bookings = bookingRepository.findAll();
        }

        return bookings.stream()
                .filter(b -> departmentId == null || (b.getDepartment() != null && Objects.equals(b.getDepartment().getId(), departmentId)))
                .filter(b -> equipmentId == null || (b.getEquipment() != null && Objects.equals(b.getEquipment().getId(), equipmentId)))
                .filter(b -> billingStatus == null || b.getBillingStatus() == billingStatus)
                .map(this::mapBookingToUsageCost)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UsageCostResponse getUsageCostByBookingId(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));
        return mapBookingToUsageCost(booking);
    }

    private UsageCostResponse mapBookingToUsageCost(Booking b) {
        UsageCostResponse r = new UsageCostResponse();
        r.setBookingId(b.getId());
        r.setBookingReference(b.getBookingReference());
        r.setStartTime(b.getStartTime());
        r.setEndTime(b.getEndTime());
        r.setBillingStatus(b.getBillingStatus());
        r.setExternalBooking(b.isExternalBooking());
        r.setEstimatedCost(b.getEstimatedCost());
        r.setActualCost(b.getActualCost());

        if (b.getEquipment() != null) {
            r.setEquipmentId(b.getEquipment().getId());
            try {
                r.setEquipmentName(b.getEquipment().getName());
                r.setEquipmentAssetTag(b.getEquipment().getAssetTag());
            } catch (Exception ignored) {}
        }

        if (b.getUser() != null) {
            r.setUserId(b.getUser().getId());
            try {
                r.setUserName(b.getUser().getFirstName() + " " + b.getUser().getLastName());
                r.setUserEmail(b.getUser().getEmail());
            } catch (Exception ignored) {}
        }

        if (b.getDepartment() != null) {
            r.setDepartmentId(b.getDepartment().getId());
            try {
                r.setDepartmentName(b.getDepartment().getName());
            } catch (Exception ignored) {}
        }

        if (b.getInstitution() != null) {
            r.setInstitutionId(b.getInstitution().getId());
            try {
                r.setInstitutionName(b.getInstitution().getName());
            } catch (Exception ignored) {}
        }

        BigDecimal hours = calculateBillableHours(b);
        BigDecimal rate = calculateEffectiveRate(b);
        r.setBillableHours(hours);
        r.setHourlyRate(rate);

        BigDecimal total = b.getActualCost() != null ? b.getActualCost() :
                (b.getEstimatedCost() != null && b.getEstimatedCost().compareTo(BigDecimal.ZERO) > 0 ? b.getEstimatedCost() :
                        hours.multiply(rate).setScale(2, RoundingMode.HALF_UP));
        r.setTotalCost(total);

        // Find associated invoice line item if invoiced or settled
        Optional<InvoiceLineItem> lineOpt = lineItemRepository.findByBookingId(b.getId());
        if (lineOpt.isPresent()) {
            InvoiceLineItem line = lineOpt.get();
            if (line.getInvoice() != null) {
                r.setInvoiceId(line.getInvoice().getId());
                try {
                    r.setInvoiceNumber(line.getInvoice().getInvoiceNumber());
                } catch (Exception ignored) {}
            }
        }

        return r;
    }

    private BigDecimal calculateBillableHours(Booking b) {
        if (b.getStartTime() == null || b.getEndTime() == null) {
            return BigDecimal.ZERO;
        }
        long minutes = Math.max(0, Duration.between(b.getStartTime(), b.getEndTime()).toMinutes());
        return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateEffectiveRate(Booking b) {
        if (b.getBaseHourlyRate() != null && b.getBaseHourlyRate().compareTo(BigDecimal.ZERO) > 0) {
            return b.getBaseHourlyRate();
        }
        if (b.getEquipment() != null) {
            try {
                if (b.isExternalBooking() && b.getEquipment().getHourlyRateExternal() != null) {
                    return b.getEquipment().getHourlyRateExternal();
                }
                if (b.getEquipment().getHourlyRateInternal() != null) {
                    return b.getEquipment().getHourlyRateInternal();
                }
            } catch (Exception ignored) {}
        }
        return BigDecimal.ZERO;
    }

    // ==========================================
    // Department Cost Summary Operations
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentCostSummaryResponse> listDepartmentCostSummaries(Long institutionId) {
        List<Department> departments;
        if (institutionId != null) {
            departments = departmentRepository.findByInstitutionId(institutionId);
        } else {
            departments = departmentRepository.findAll();
        }

        return departments.stream()
                .map(dept -> buildDepartmentCostSummary(dept, institutionId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentCostSummaryResponse getDepartmentCostSummary(Long departmentId, Long institutionId) {
        Department dept = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", departmentId));

        if (institutionId != null && dept.getInstitution() != null && !Objects.equals(dept.getInstitution().getId(), institutionId)) {
            throw new InvalidOperationException("Department does not belong to the authenticated institution");
        }

        return buildDepartmentCostSummary(dept, institutionId);
    }

    private DepartmentCostSummaryResponse buildDepartmentCostSummary(Department dept, Long institutionId) {
        DepartmentCostSummaryResponse r = new DepartmentCostSummaryResponse();
        r.setDepartmentId(dept.getId());
        r.setDepartmentName(dept.getName());
        if (dept.getInstitution() != null) {
            r.setInstitutionId(dept.getInstitution().getId());
            try {
                r.setInstitutionName(dept.getInstitution().getName());
            } catch (Exception ignored) {}
        }

        List<Booking> bookings = bookingRepository.findByDepartmentId(dept.getId());
        r.setTotalBookingsCount(bookings.size());

        int unbilledCount = 0;
        BigDecimal unbilledTotal = BigDecimal.ZERO;
        BigDecimal invoicedTotal = BigDecimal.ZERO;
        BigDecimal settledTotal = BigDecimal.ZERO;

        for (Booking b : bookings) {
            BigDecimal cost = b.getActualCost() != null ? b.getActualCost() :
                    (b.getEstimatedCost() != null && b.getEstimatedCost().compareTo(BigDecimal.ZERO) > 0 ? b.getEstimatedCost() :
                            calculateBillableHours(b).multiply(calculateEffectiveRate(b)).setScale(2, RoundingMode.HALF_UP));

            if (b.getBillingStatus() == BookingBillingStatus.UNBILLED) {
                unbilledCount++;
                unbilledTotal = unbilledTotal.add(cost);
            } else if (b.getBillingStatus() == BookingBillingStatus.INVOICED) {
                invoicedTotal = invoicedTotal.add(cost);
            } else if (b.getBillingStatus() == BookingBillingStatus.SETTLED) {
                settledTotal = settledTotal.add(cost);
            }
        }

        r.setUnbilledBookingsCount(unbilledCount);
        r.setUnbilledCost(unbilledTotal);
        r.setInvoicedCost(invoicedTotal);
        r.setSettledCost(settledTotal);
        r.setTotalCost(unbilledTotal.add(invoicedTotal).add(settledTotal));

        return r;
    }

    // ==========================================
    // Invoice Operations
    // ==========================================

    @Override
    public BillingInvoice createInvoice(BillingInvoice invoice,
                                        List<Long> bookingIds,
                                        Long issuingInstitutionId,
                                        Long billedInstitutionId,
                                        Long billedDepartmentId,
                                        Long sharingAgreementId,
                                        Long operatorInstitutionId) {
        if (issuingInstitutionId == null || billedInstitutionId == null) {
            throw new InvalidOperationException("Both issuing institution ID and billed institution ID are required");
        }

        if (operatorInstitutionId != null && !Objects.equals(operatorInstitutionId, issuingInstitutionId)) {
            throw new InvalidOperationException("Only the issuing institution can generate invoices");
        }

        Institution issuing = institutionRepository.findById(issuingInstitutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Institution", "id", issuingInstitutionId));

        Institution billed = institutionRepository.findById(billedInstitutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Institution", "id", billedInstitutionId));

        Department billedDept = null;
        if (billedDepartmentId != null) {
            billedDept = departmentRepository.findById(billedDepartmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", billedDepartmentId));
            if (!Objects.equals(billedDept.getInstitution().getId(), billedInstitutionId)) {
                throw new InvalidOperationException("Billed department does not belong to billed institution #" + billedInstitutionId);
            }
        }

        ResourceSharingAgreement sharingAgreement = null;
        if (sharingAgreementId != null) {
            sharingAgreement = sharingAgreementRepository.findById(sharingAgreementId)
                    .orElseThrow(() -> new ResourceNotFoundException("ResourceSharingAgreement", "id", sharingAgreementId));
            if (!Objects.equals(sharingAgreement.getOwnerInstitution().getId(), issuingInstitutionId)) {
                throw new InvalidOperationException("Sharing agreement owner institution must match issuing institution");
            }
            if (!Objects.equals(sharingAgreement.getRequestingInstitution().getId(), billedInstitutionId)) {
                throw new InvalidOperationException("Sharing agreement requesting institution must match billed institution");
            }
        } else if (!issuingInstitutionId.equals(billedInstitutionId)) {
            throw new InvalidOperationException("Inter-institutional invoice requires a valid sharing agreement");
        }

        if (invoice.getBillingPeriodStart() == null || invoice.getBillingPeriodEnd() == null) {
            throw new InvalidOperationException("Billing period start and end dates are required");
        }
        if (invoice.getBillingPeriodEnd().isBefore(invoice.getBillingPeriodStart())) {
            throw new InvalidOperationException("Billing period end date cannot be before start date");
        }

        // Handle Invoice Number
        if (invoice.getInvoiceNumber() == null || invoice.getInvoiceNumber().isBlank()) {
            invoice.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        } else if (invoiceRepository.existsByInvoiceNumber(invoice.getInvoiceNumber())) {
            throw new DuplicateResourceException("Invoice with number '" + invoice.getInvoiceNumber() + "' already exists");
        }

        invoice.setIssuingInstitution(issuing);
        invoice.setBilledInstitution(billed);
        invoice.setBilledDepartment(billedDept);
        invoice.setSharingAgreement(sharingAgreement);
        if (invoice.getStatus() == null) {
            invoice.setStatus(InvoiceStatus.DRAFT);
        }
        if (invoice.getDiscountAmount() == null) {
            invoice.setDiscountAmount(BigDecimal.ZERO);
        }

        BillingInvoice savedInvoice = invoiceRepository.save(invoice);

        // Process line items for initial bookings if specified
        BigDecimal subtotal = BigDecimal.ZERO;
        if (bookingIds != null && !bookingIds.isEmpty()) {
            for (Long bId : bookingIds) {
                Booking booking = bookingRepository.findById(bId)
                        .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bId));

                if (booking.getBillingStatus() != BookingBillingStatus.UNBILLED) {
                    throw new InvalidOperationException("Booking '" + booking.getBookingReference() + "' is already " + booking.getBillingStatus());
                }

                if (lineItemRepository.existsByBookingId(bId)) {
                    throw new DuplicateResourceException("Booking '" + booking.getBookingReference() + "' already appears on an invoice line");
                }

                BigDecimal hours = calculateBillableHours(booking);
                BigDecimal rate = calculateEffectiveRate(booking);
                BigDecimal lineCost = booking.getActualCost() != null ? booking.getActualCost() :
                        (booking.getEstimatedCost() != null && booking.getEstimatedCost().compareTo(BigDecimal.ZERO) > 0 ? booking.getEstimatedCost() :
                                hours.multiply(rate).setScale(2, RoundingMode.HALF_UP));

                InvoiceLineItem line = new InvoiceLineItem();
                line.setInvoice(savedInvoice);
                line.setBooking(booking);
                line.setEquipment(booking.getEquipment());
                line.setDescription("Reservation " + booking.getBookingReference() + " - " + (booking.getEquipment() != null ? booking.getEquipment().getName() : "Equipment"));
                line.setBillableHours(hours);
                line.setHourlyRate(rate);
                line.setTotalLineCost(lineCost);
                line.setPenaltyAmount(BigDecimal.ZERO);
                lineItemRepository.save(line);

                booking.setBillingStatus(BookingBillingStatus.INVOICED);
                bookingRepository.save(booking);

                subtotal = subtotal.add(lineCost);
            }
        }

        savedInvoice.setSubtotalAmount(subtotal);
        BigDecimal discount = savedInvoice.getDiscountAmount() != null ? savedInvoice.getDiscountAmount() : BigDecimal.ZERO;
        savedInvoice.setTotalAmount(subtotal.subtract(discount).max(BigDecimal.ZERO));

        BillingInvoice updatedInvoice = invoiceRepository.save(savedInvoice);
        return initializeInvoice(updatedInvoice);
    }

    @Override
    @Transactional(readOnly = true)
    public BillingInvoice getInvoiceById(Long id) {
        BillingInvoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BillingInvoice", "id", id));
        return initializeInvoice(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public BillingInvoice getInvoiceByNumber(String invoiceNumber) {
        BillingInvoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("BillingInvoice", "invoiceNumber", invoiceNumber));
        return initializeInvoice(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillingInvoice> listInvoices(Long issuingInstitutionId,
                                            Long billedInstitutionId,
                                            Long billedDepartmentId,
                                            InvoiceStatus status,
                                            Long sharingAgreementId,
                                            Long institutionId) {
        List<BillingInvoice> list = invoiceRepository.findAll();
        return list.stream()
                .filter(inv -> {
                    if (institutionId != null &&
                        !Objects.equals(inv.getIssuingInstitution().getId(), institutionId) &&
                        !Objects.equals(inv.getBilledInstitution().getId(), institutionId)) {
                        return false;
                    }
                    if (issuingInstitutionId != null && !Objects.equals(inv.getIssuingInstitution().getId(), issuingInstitutionId)) {
                        return false;
                    }
                    if (billedInstitutionId != null && !Objects.equals(inv.getBilledInstitution().getId(), billedInstitutionId)) {
                        return false;
                    }
                    if (billedDepartmentId != null && (inv.getBilledDepartment() == null || !Objects.equals(inv.getBilledDepartment().getId(), billedDepartmentId))) {
                        return false;
                    }
                    if (status != null && inv.getStatus() != status) {
                        return false;
                    }
                    if (sharingAgreementId != null && (inv.getSharingAgreement() == null || !Objects.equals(inv.getSharingAgreement().getId(), sharingAgreementId))) {
                        return false;
                    }
                    return true;
                })
                .map(this::initializeInvoice)
                .toList();
    }

    @Override
    public BillingInvoice updateInvoiceStatus(Long id, InvoiceStatus newStatus, String paymentReference, Long operatorInstitutionId) {
        BillingInvoice invoice = getInvoiceById(id);

        if (operatorInstitutionId != null &&
            !Objects.equals(invoice.getIssuingInstitution().getId(), operatorInstitutionId) &&
            !Objects.equals(invoice.getBilledInstitution().getId(), operatorInstitutionId)) {
            throw new InvalidOperationException("Institution is not a party to this invoice");
        }

        if (invoice.getStatus() == InvoiceStatus.SETTLED || invoice.getStatus() == InvoiceStatus.WRITTEN_OFF) {
            throw new InvalidOperationException("Invoices in " + invoice.getStatus() + " status cannot change status");
        }

        if (invoice.getStatus() == newStatus) {
            return initializeInvoice(invoice);
        }

        // Validate lifecycle transitions
        if (invoice.getStatus() == InvoiceStatus.DRAFT) {
            if (newStatus != InvoiceStatus.ISSUED && newStatus != InvoiceStatus.WRITTEN_OFF) {
                throw new InvalidOperationException("Draft invoice can only transition to ISSUED or WRITTEN_OFF");
            }
        } else if (invoice.getStatus() == InvoiceStatus.ISSUED) {
            if (newStatus == InvoiceStatus.DRAFT) {
                throw new InvalidOperationException("Issued invoice cannot be reverted to DRAFT");
            }
        }

        if (newStatus == InvoiceStatus.ISSUED && invoice.getIssuedAt() == null) {
            invoice.setIssuedAt(Instant.now());
        }

        if (newStatus == InvoiceStatus.SETTLED) {
            invoice.setPaidAt(Instant.now());
            if (paymentReference != null && !paymentReference.isBlank()) {
                invoice.setPaymentReference(paymentReference);
            }
            // Mark all associated bookings as SETTLED
            List<InvoiceLineItem> lines = lineItemRepository.findByInvoiceId(id);
            for (InvoiceLineItem line : lines) {
                if (line.getBooking() != null) {
                    Booking b = line.getBooking();
                    b.setBillingStatus(BookingBillingStatus.SETTLED);
                    bookingRepository.save(b);
                }
            }
        }

        invoice.setStatus(newStatus);
        BillingInvoice saved = invoiceRepository.save(invoice);
        return initializeInvoice(saved);
    }

    @Override
    public void deleteInvoice(Long id, Long operatorInstitutionId) {
        BillingInvoice invoice = getInvoiceById(id);

        if (operatorInstitutionId != null && !Objects.equals(invoice.getIssuingInstitution().getId(), operatorInstitutionId)) {
            throw new InvalidOperationException("Only the issuing institution can delete an invoice");
        }

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new InvalidOperationException("Only DRAFT invoices can be deleted. Invoices with status " + invoice.getStatus() + " cannot be deleted.");
        }

        // Reset all associated bookings from INVOICED back to UNBILLED
        List<InvoiceLineItem> lines = lineItemRepository.findByInvoiceId(id);
        for (InvoiceLineItem line : lines) {
            if (line.getBooking() != null) {
                Booking b = line.getBooking();
                b.setBillingStatus(BookingBillingStatus.UNBILLED);
                bookingRepository.save(b);
            }
            lineItemRepository.delete(line);
        }

        invoiceRepository.delete(invoice);
    }

    // ==========================================
    // Line Item Operations
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceLineItem> listLineItemsByInvoiceId(Long invoiceId) {
        List<InvoiceLineItem> lines = lineItemRepository.findByInvoiceId(invoiceId);
        return lines.stream().map(this::initializeLineItem).toList();
    }

    @Override
    public InvoiceLineItem addLineItem(Long invoiceId, Long bookingId, BigDecimal customRate, String description, Long operatorInstitutionId) {
        BillingInvoice invoice = getInvoiceById(invoiceId);

        if (operatorInstitutionId != null && !Objects.equals(invoice.getIssuingInstitution().getId(), operatorInstitutionId)) {
            throw new InvalidOperationException("Only the issuing institution can modify invoice line items");
        }

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new InvalidOperationException("Issued invoice lines are immutable. Cannot add lines to an invoice with status " + invoice.getStatus());
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (booking.getBillingStatus() != BookingBillingStatus.UNBILLED) {
            throw new InvalidOperationException("Booking '" + booking.getBookingReference() + "' is already " + booking.getBillingStatus());
        }

        if (lineItemRepository.existsByBookingId(bookingId)) {
            throw new DuplicateResourceException("Booking '" + booking.getBookingReference() + "' already appears on an invoice");
        }

        BigDecimal hours = calculateBillableHours(booking);
        BigDecimal rate = customRate != null ? customRate : calculateEffectiveRate(booking);
        BigDecimal lineCost = hours.multiply(rate).setScale(2, RoundingMode.HALF_UP);

        InvoiceLineItem line = new InvoiceLineItem();
        line.setInvoice(invoice);
        line.setBooking(booking);
        line.setEquipment(booking.getEquipment());
        line.setDescription(description != null && !description.isBlank() ? description :
                "Reservation " + booking.getBookingReference() + " - " + (booking.getEquipment() != null ? booking.getEquipment().getName() : "Equipment"));
        line.setBillableHours(hours);
        line.setHourlyRate(rate);
        line.setTotalLineCost(lineCost);
        line.setPenaltyAmount(BigDecimal.ZERO);
        InvoiceLineItem savedLine = lineItemRepository.save(line);

        booking.setBillingStatus(BookingBillingStatus.INVOICED);
        bookingRepository.save(booking);

        // Update invoice subtotal & total
        BigDecimal currentSubtotal = invoice.getSubtotalAmount() != null ? invoice.getSubtotalAmount() : BigDecimal.ZERO;
        BigDecimal newSubtotal = currentSubtotal.add(lineCost);
        invoice.setSubtotalAmount(newSubtotal);
        BigDecimal discount = invoice.getDiscountAmount() != null ? invoice.getDiscountAmount() : BigDecimal.ZERO;
        invoice.setTotalAmount(newSubtotal.subtract(discount).max(BigDecimal.ZERO));
        invoiceRepository.save(invoice);

        return initializeLineItem(savedLine);
    }

    // ==========================================
    // Lazy Proxy Helpers
    // ==========================================

    private BillingInvoice initializeInvoice(BillingInvoice inv) {
        if (inv != null) {
            if (inv.getIssuingInstitution() != null) {
                try { inv.getIssuingInstitution().getName(); } catch (Exception ignored) {}
            }
            if (inv.getBilledInstitution() != null) {
                try { inv.getBilledInstitution().getName(); } catch (Exception ignored) {}
            }
            if (inv.getBilledDepartment() != null) {
                try { inv.getBilledDepartment().getName(); } catch (Exception ignored) {}
            }
            if (inv.getSharingAgreement() != null) {
                try { inv.getSharingAgreement().getAgreementCode(); } catch (Exception ignored) {}
            }
        }
        return inv;
    }

    private InvoiceLineItem initializeLineItem(InvoiceLineItem item) {
        if (item != null) {
            if (item.getBooking() != null) {
                try { item.getBooking().getBookingReference(); } catch (Exception ignored) {}
            }
            if (item.getEquipment() != null) {
                try { item.getEquipment().getName(); } catch (Exception ignored) {}
            }
            if (item.getInvoice() != null) {
                try { item.getInvoice().getInvoiceNumber(); } catch (Exception ignored) {}
            }
        }
        return item;
    }
}
