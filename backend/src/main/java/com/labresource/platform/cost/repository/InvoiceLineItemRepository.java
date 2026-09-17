package com.labresource.platform.cost.repository;

import com.labresource.platform.cost.InvoiceLineItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceLineItemRepository extends JpaRepository<InvoiceLineItem, Long> {

    List<InvoiceLineItem> findByInvoiceId(Long invoiceId);

    Optional<InvoiceLineItem> findByBookingId(Long bookingId);

    List<InvoiceLineItem> findByEquipmentId(Long equipmentId);

    boolean existsByBookingId(Long bookingId);
}
