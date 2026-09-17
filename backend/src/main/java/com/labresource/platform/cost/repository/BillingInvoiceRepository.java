package com.labresource.platform.cost.repository;

import com.labresource.platform.cost.BillingInvoice;
import com.labresource.platform.cost.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillingInvoiceRepository extends JpaRepository<BillingInvoice, Long> {

    Optional<BillingInvoice> findByInvoiceNumber(String invoiceNumber);

    boolean existsByInvoiceNumber(String invoiceNumber);

    List<BillingInvoice> findByIssuingInstitutionId(Long issuingInstitutionId);

    List<BillingInvoice> findByBilledInstitutionId(Long billedInstitutionId);

    List<BillingInvoice> findByBilledDepartmentId(Long billedDepartmentId);

    List<BillingInvoice> findBySharingAgreementId(Long sharingAgreementId);

    List<BillingInvoice> findByStatus(InvoiceStatus status);

    List<BillingInvoice> findByIssuingInstitutionIdAndStatus(Long issuingInstitutionId, InvoiceStatus status);

    List<BillingInvoice> findByBilledInstitutionIdAndStatus(Long billedInstitutionId, InvoiceStatus status);
}
