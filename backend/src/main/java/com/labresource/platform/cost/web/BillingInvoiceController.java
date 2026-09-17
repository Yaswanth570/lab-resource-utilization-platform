package com.labresource.platform.cost.web;

import com.labresource.platform.cost.BillingInvoice;
import com.labresource.platform.cost.InvoiceLineItem;
import com.labresource.platform.cost.InvoiceStatus;
import com.labresource.platform.cost.service.CostService;
import com.labresource.platform.security.principal.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/cost/invoices")
@PreAuthorize("hasAnyRole('ROLE_DEPARTMENT_HEAD', 'ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
public class BillingInvoiceController {

    private final CostService costService;

    public BillingInvoiceController(CostService costService) {
        this.costService = costService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<InvoiceResponse> createInvoice(@Valid @RequestBody CreateInvoiceRequest request) {
        Long operatorInstitutionId = SecurityUtils.getCurrentInstitutionId().orElse(null);

        BillingInvoice invoice = new BillingInvoice();
        invoice.setInvoiceNumber(request.getInvoiceNumber());
        invoice.setBillingPeriodStart(request.getBillingPeriodStart());
        invoice.setBillingPeriodEnd(request.getBillingPeriodEnd());
        invoice.setDiscountAmount(request.getDiscountAmount());
        invoice.setDueDate(request.getDueDate());
        invoice.setNotes(request.getNotes());

        Long issuingInstId = request.getIssuingInstitutionId() != null
                ? request.getIssuingInstitutionId()
                : (request.getInstitutionId() != null ? request.getInstitutionId() : operatorInstitutionId);
        Long billedInstId = request.getBilledInstitutionId() != null
                ? request.getBilledInstitutionId()
                : (request.getInstitutionId() != null ? request.getInstitutionId() : issuingInstId);
        Long billedDeptId = request.getBilledDepartmentId() != null
                ? request.getBilledDepartmentId()
                : request.getDepartmentId();

        BillingInvoice created = costService.createInvoice(
                invoice,
                request.getBookingIds(),
                issuingInstId,
                billedInstId,
                billedDeptId,
                request.getSharingAgreementId(),
                operatorInstitutionId
        );

        List<InvoiceLineItem> lines = costService.listLineItemsByInvoiceId(created.getId());
        InvoiceResponse response = InvoiceResponse.fromEntity(created, lines);

        return ResponseEntity
                .created(URI.create("/api/cost/invoices/" + created.getId()))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<InvoiceResponse>> listInvoices(
            @RequestParam(required = false) Long issuingInstitutionId,
            @RequestParam(required = false) Long billedInstitutionId,
            @RequestParam(required = false) Long billedDepartmentId,
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) Long sharingAgreementId) {

        Long currentInstitutionId = SecurityUtils.getCurrentInstitutionId().orElse(null);

        List<BillingInvoice> list = costService.listInvoices(
                issuingInstitutionId,
                billedInstitutionId,
                billedDepartmentId,
                status,
                sharingAgreementId,
                currentInstitutionId
        );

        List<InvoiceResponse> response = list.stream()
                .map(inv -> {
                    List<InvoiceLineItem> lines = costService.listLineItemsByInvoiceId(inv.getId());
                    return InvoiceResponse.fromEntity(inv, lines);
                })
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> getInvoiceById(@PathVariable Long id) {
        BillingInvoice invoice = costService.getInvoiceById(id);
        List<InvoiceLineItem> lines = costService.listLineItemsByInvoiceId(id);
        return ResponseEntity.ok(InvoiceResponse.fromEntity(invoice, lines));
    }

    @GetMapping("/number/{invoiceNumber}")
    public ResponseEntity<InvoiceResponse> getInvoiceByNumber(@PathVariable String invoiceNumber) {
        BillingInvoice invoice = costService.getInvoiceByNumber(invoiceNumber);
        List<InvoiceLineItem> lines = costService.listLineItemsByInvoiceId(invoice.getId());
        return ResponseEntity.ok(InvoiceResponse.fromEntity(invoice, lines));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<InvoiceResponse> updateInvoiceStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInvoiceStatusRequest request) {

        Long operatorInstitutionId = SecurityUtils.getCurrentInstitutionId().orElse(null);

        BillingInvoice updated = costService.updateInvoiceStatus(
                id,
                request.getStatus(),
                request.getPaymentReference(),
                operatorInstitutionId
        );

        List<InvoiceLineItem> lines = costService.listLineItemsByInvoiceId(id);
        return ResponseEntity.ok(InvoiceResponse.fromEntity(updated, lines));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id) {
        Long operatorInstitutionId = SecurityUtils.getCurrentInstitutionId().orElse(null);
        costService.deleteInvoice(id, operatorInstitutionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/lines")
    public ResponseEntity<List<InvoiceLineItemResponse>> listInvoiceLines(@PathVariable Long id) {
        List<InvoiceLineItem> lines = costService.listLineItemsByInvoiceId(id);
        return ResponseEntity.ok(lines.stream().map(InvoiceLineItemResponse::fromEntity).toList());
    }

    @PostMapping("/{id}/lines")
    @PreAuthorize("hasAnyRole('ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<InvoiceLineItemResponse> addInvoiceLine(
            @PathVariable Long id,
            @Valid @RequestBody AddInvoiceLineRequest request) {

        Long operatorInstitutionId = SecurityUtils.getCurrentInstitutionId().orElse(null);

        InvoiceLineItem line = costService.addLineItem(
                id,
                request.getBookingId(),
                request.getCustomRate(),
                request.getDescription(),
                operatorInstitutionId
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(InvoiceLineItemResponse.fromEntity(line));
    }
}
