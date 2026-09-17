package com.labresource.platform.cost.service;

import com.labresource.platform.booking.BookingBillingStatus;
import com.labresource.platform.cost.BillingInvoice;
import com.labresource.platform.cost.InvoiceLineItem;
import com.labresource.platform.cost.InvoiceStatus;
import com.labresource.platform.cost.web.DepartmentCostSummaryResponse;
import com.labresource.platform.cost.web.UsageCostResponse;

import java.math.BigDecimal;
import java.util.List;

public interface CostService {

    // ==========================================
    // Usage Cost Operations
    // ==========================================

    List<UsageCostResponse> listUsageCosts(Long institutionId, Long departmentId, Long equipmentId, BookingBillingStatus billingStatus);

    UsageCostResponse getUsageCostByBookingId(Long bookingId);

    // ==========================================
    // Department Cost Summary Operations
    // ==========================================

    List<DepartmentCostSummaryResponse> listDepartmentCostSummaries(Long institutionId);

    DepartmentCostSummaryResponse getDepartmentCostSummary(Long departmentId, Long institutionId);

    // ==========================================
    // Invoice Operations
    // ==========================================

    BillingInvoice createInvoice(BillingInvoice invoice,
                                 List<Long> bookingIds,
                                 Long issuingInstitutionId,
                                 Long billedInstitutionId,
                                 Long billedDepartmentId,
                                 Long sharingAgreementId,
                                 Long operatorInstitutionId);

    BillingInvoice getInvoiceById(Long id);

    BillingInvoice getInvoiceByNumber(String invoiceNumber);

    List<BillingInvoice> listInvoices(Long issuingInstitutionId,
                                      Long billedInstitutionId,
                                      Long billedDepartmentId,
                                      InvoiceStatus status,
                                      Long sharingAgreementId,
                                      Long institutionId);

    BillingInvoice updateInvoiceStatus(Long id, InvoiceStatus newStatus, String paymentReference, Long operatorInstitutionId);

    void deleteInvoice(Long id, Long operatorInstitutionId);

    // ==========================================
    // Line Item Operations
    // ==========================================

    List<InvoiceLineItem> listLineItemsByInvoiceId(Long invoiceId);

    InvoiceLineItem addLineItem(Long invoiceId, Long bookingId, BigDecimal customRate, String description, Long operatorInstitutionId);
}
