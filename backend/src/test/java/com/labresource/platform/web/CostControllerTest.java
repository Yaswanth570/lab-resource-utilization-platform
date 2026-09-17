package com.labresource.platform.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.labresource.platform.booking.Booking;
import com.labresource.platform.booking.BookingBillingStatus;
import com.labresource.platform.common.exception.InvalidOperationException;
import com.labresource.platform.common.exception.ResourceNotFoundException;
import com.labresource.platform.cost.BillingInvoice;
import com.labresource.platform.cost.InvoiceLineItem;
import com.labresource.platform.cost.InvoiceStatus;
import com.labresource.platform.cost.service.CostService;
import com.labresource.platform.cost.web.*;
import com.labresource.platform.department.Department;
import com.labresource.platform.equipment.Equipment;
import com.labresource.platform.institution.Institution;
import com.labresource.platform.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "jwt.secret=${JWT_SECRET:dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLW1pbi0zMi1ieXRlcw==}",
        "spring.datasource.password=${DB_PASSWORD:}"
})
class CostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private CostService costService;

    private String validToken;
    private Institution testInst;
    private Department testDept;
    private BillingInvoice testInvoice;
    private InvoiceLineItem testLineItem;

    @BeforeEach
    void setUp() {
        validToken = "Bearer " + jwtService.generateToken(1L, "admin@nsti.edu", 1L, List.of("ROLE_INSTITUTION_ADMINISTRATOR"));

        testInst = new Institution();
        testInst.setId(1L);
        testInst.setName("National Science & Technology Institute");

        testDept = new Department();
        testDept.setId(10L);
        testDept.setName("Nanotechnology Lab");
        testDept.setInstitution(testInst);

        testInvoice = new BillingInvoice();
        testInvoice.setId(100L);
        testInvoice.setInvoiceNumber("INV-2026-001");
        testInvoice.setIssuingInstitution(testInst);
        testInvoice.setBilledInstitution(testInst);
        testInvoice.setBilledDepartment(testDept);
        testInvoice.setBillingPeriodStart(LocalDate.parse("2026-04-01"));
        testInvoice.setBillingPeriodEnd(LocalDate.parse("2026-04-30"));
        testInvoice.setStatus(InvoiceStatus.DRAFT);
        testInvoice.setSubtotalAmount(BigDecimal.valueOf(250.00));
        testInvoice.setDiscountAmount(BigDecimal.ZERO);
        testInvoice.setTotalAmount(BigDecimal.valueOf(250.00));
        testInvoice.setCreatedAt(Instant.now());
        testInvoice.setUpdatedAt(Instant.now());

        Equipment eq = new Equipment();
        eq.setId(20L);
        eq.setName("Atomic Force Microscope");
        eq.setAssetTag("EQ-AFM-01");

        Booking bk = new Booking();
        bk.setId(300L);
        bk.setBookingReference("BK-2026-AFM");

        testLineItem = new InvoiceLineItem();
        testLineItem.setId(1000L);
        testLineItem.setInvoice(testInvoice);
        testLineItem.setEquipment(eq);
        testLineItem.setBooking(bk);
        testLineItem.setDescription("Reservation BK-2026-AFM");
        testLineItem.setBillableHours(BigDecimal.valueOf(2.5));
        testLineItem.setHourlyRate(BigDecimal.valueOf(100.00));
        testLineItem.setTotalLineCost(BigDecimal.valueOf(250.00));
        testLineItem.setPenaltyAmount(BigDecimal.ZERO);
        testLineItem.setCreatedAt(Instant.now());
    }

    // ==========================================
    // Authentication Protection Tests
    // ==========================================

    @Test
    @DisplayName("GET /api/cost/usage without auth token returns 401 Unauthorized")
    void testListUsageCostsUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/cost/usage"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/cost/invoices without auth token returns 401 Unauthorized")
    void testListInvoicesUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/cost/invoices"))
                .andExpect(status().isUnauthorized());
    }

    // ==========================================
    // Usage Cost Tests
    // ==========================================

    @Test
    @DisplayName("GET /api/cost/usage - Success returns 200 OK list")
    void testListUsageCostsSuccess() throws Exception {
        UsageCostResponse usage = new UsageCostResponse();
        usage.setBookingId(300L);
        usage.setBookingReference("BK-2026-AFM");
        usage.setEquipmentName("Atomic Force Microscope");
        usage.setBillableHours(BigDecimal.valueOf(2.5));
        usage.setHourlyRate(BigDecimal.valueOf(100.00));
        usage.setTotalCost(BigDecimal.valueOf(250.00));
        usage.setBillingStatus(BookingBillingStatus.UNBILLED);

        when(costService.listUsageCosts(eq(1L), any(), any(), any())).thenReturn(List.of(usage));

        mockMvc.perform(get("/api/cost/usage")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].bookingReference", is("BK-2026-AFM")))
                .andExpect(jsonPath("$[0].totalCost", is(250.00)))
                .andExpect(jsonPath("$[0].billingStatus", is("UNBILLED")));
    }

    @Test
    @DisplayName("GET /api/cost/usage/{bookingId} - Success returns 200 OK")
    void testGetUsageCostByIdSuccess() throws Exception {
        UsageCostResponse usage = new UsageCostResponse();
        usage.setBookingId(300L);
        usage.setBookingReference("BK-2026-AFM");
        usage.setTotalCost(BigDecimal.valueOf(250.00));

        when(costService.getUsageCostByBookingId(300L)).thenReturn(usage);

        mockMvc.perform(get("/api/cost/usage/300")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingReference", is("BK-2026-AFM")));
    }

    @Test
    @DisplayName("GET /api/cost/usage/{bookingId} - Not Found returns 404")
    void testGetUsageCostByIdNotFound() throws Exception {
        when(costService.getUsageCostByBookingId(999L))
                .thenThrow(new ResourceNotFoundException("Booking", "id", 999L));

        mockMvc.perform(get("/api/cost/usage/999")
                        .header("Authorization", validToken))
                .andExpect(status().isNotFound());
    }

    // ==========================================
    // Department Cost Summary Tests
    // ==========================================

    @Test
    @DisplayName("GET /api/cost/department - Success returns 200 OK list")
    void testListDepartmentCostSummariesSuccess() throws Exception {
        DepartmentCostSummaryResponse deptSummary = new DepartmentCostSummaryResponse();
        deptSummary.setDepartmentId(10L);
        deptSummary.setDepartmentName("Nanotechnology Lab");
        deptSummary.setTotalBookingsCount(5);
        deptSummary.setTotalCost(BigDecimal.valueOf(1250.00));

        when(costService.listDepartmentCostSummaries(1L)).thenReturn(List.of(deptSummary));

        mockMvc.perform(get("/api/cost/department")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].departmentName", is("Nanotechnology Lab")))
                .andExpect(jsonPath("$[0].totalCost", is(1250.00)));
    }

    @Test
    @DisplayName("GET /api/cost/department/{id} - Success returns 200 OK")
    void testGetDepartmentCostSummaryByIdSuccess() throws Exception {
        DepartmentCostSummaryResponse deptSummary = new DepartmentCostSummaryResponse();
        deptSummary.setDepartmentId(10L);
        deptSummary.setDepartmentName("Nanotechnology Lab");
        deptSummary.setTotalCost(BigDecimal.valueOf(1250.00));

        when(costService.getDepartmentCostSummary(10L, 1L)).thenReturn(deptSummary);

        mockMvc.perform(get("/api/cost/department/10")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.departmentName", is("Nanotechnology Lab")));
    }

    // ==========================================
    // Invoice Tests
    // ==========================================

    @Test
    @DisplayName("POST /api/cost/invoices - Success returns 201 Created")
    void testCreateInvoiceSuccess() throws Exception {
        CreateInvoiceRequest req = new CreateInvoiceRequest();
        req.setIssuingInstitutionId(1L);
        req.setBilledInstitutionId(1L);
        req.setBilledDepartmentId(10L);
        req.setInvoiceNumber("INV-2026-001");
        req.setBillingPeriodStart(LocalDate.parse("2026-04-01"));
        req.setBillingPeriodEnd(LocalDate.parse("2026-04-30"));
        req.setBookingIds(List.of(300L));

        when(costService.createInvoice(any(), any(), eq(1L), eq(1L), eq(10L), any(), eq(1L)))
                .thenReturn(testInvoice);
        when(costService.listLineItemsByInvoiceId(100L)).thenReturn(List.of(testLineItem));

        mockMvc.perform(post("/api/cost/invoices")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/cost/invoices/100"))
                .andExpect(jsonPath("$.invoiceNumber", is("INV-2026-001")))
                .andExpect(jsonPath("$.status", is("DRAFT")))
                .andExpect(jsonPath("$.subtotalAmount", is(250.00)))
                .andExpect(jsonPath("$.lineItems", hasSize(1)));
    }

    @Test
    @DisplayName("POST /api/cost/invoices - Validation failure (missing start date) returns 400")
    void testCreateInvoiceValidationFailure() throws Exception {
        CreateInvoiceRequest req = new CreateInvoiceRequest();
        req.setIssuingInstitutionId(1L);
        req.setBilledInstitutionId(1L);
        // missing billingPeriodStart & billingPeriodEnd

        mockMvc.perform(post("/api/cost/invoices")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/cost/invoices - Success returns 200 OK list")
    void testListInvoicesSuccess() throws Exception {
        when(costService.listInvoices(any(), any(), any(), any(), any(), eq(1L)))
                .thenReturn(List.of(testInvoice));
        when(costService.listLineItemsByInvoiceId(100L)).thenReturn(List.of(testLineItem));

        mockMvc.perform(get("/api/cost/invoices")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].invoiceNumber", is("INV-2026-001")));
    }

    @Test
    @DisplayName("GET /api/cost/invoices/{id} - Success returns 200 OK")
    void testGetInvoiceByIdSuccess() throws Exception {
        when(costService.getInvoiceById(100L)).thenReturn(testInvoice);
        when(costService.listLineItemsByInvoiceId(100L)).thenReturn(List.of(testLineItem));

        mockMvc.perform(get("/api/cost/invoices/100")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invoiceNumber", is("INV-2026-001")))
                .andExpect(jsonPath("$.lineItems", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/cost/invoices/{id} - Not Found returns 404")
    void testGetInvoiceByIdNotFound() throws Exception {
        when(costService.getInvoiceById(999L))
                .thenThrow(new ResourceNotFoundException("BillingInvoice", "id", 999L));

        mockMvc.perform(get("/api/cost/invoices/999")
                        .header("Authorization", validToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /api/cost/invoices/{id}/status - Success returns 200 OK")
    void testUpdateInvoiceStatusSuccess() throws Exception {
        testInvoice.setStatus(InvoiceStatus.ISSUED);
        testInvoice.setIssuedAt(Instant.now());

        UpdateInvoiceStatusRequest req = new UpdateInvoiceStatusRequest();
        req.setStatus(InvoiceStatus.ISSUED);

        when(costService.updateInvoiceStatus(eq(100L), eq(InvoiceStatus.ISSUED), any(), eq(1L)))
                .thenReturn(testInvoice);
        when(costService.listLineItemsByInvoiceId(100L)).thenReturn(List.of(testLineItem));

        mockMvc.perform(patch("/api/cost/invoices/100/status")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ISSUED")));
    }

    @Test
    @DisplayName("DELETE /api/cost/invoices/{id} - Success returns 204 No Content")
    void testDeleteDraftInvoiceSuccess() throws Exception {
        doNothing().when(costService).deleteInvoice(100L, 1L);

        mockMvc.perform(delete("/api/cost/invoices/100")
                        .header("Authorization", validToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/cost/invoices/{id} - Non-draft invoice returns 400 Bad Request")
    void testDeleteIssuedInvoiceReturns400() throws Exception {
        doThrow(new InvalidOperationException("Only DRAFT invoices can be deleted"))
                .when(costService).deleteInvoice(100L, 1L);

        mockMvc.perform(delete("/api/cost/invoices/100")
                        .header("Authorization", validToken))
                .andExpect(status().isBadRequest());
    }

    // ==========================================
    // Line Item Tests
    // ==========================================

    @Test
    @DisplayName("GET /api/cost/invoices/{id}/lines - Success returns 200 OK")
    void testListInvoiceLinesSuccess() throws Exception {
        when(costService.listLineItemsByInvoiceId(100L)).thenReturn(List.of(testLineItem));

        mockMvc.perform(get("/api/cost/invoices/100/lines")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].description", is("Reservation BK-2026-AFM")));
    }

    @Test
    @DisplayName("POST /api/cost/invoices/{id}/lines - Success returns 201 Created")
    void testAddInvoiceLineSuccess() throws Exception {
        AddInvoiceLineRequest req = new AddInvoiceLineRequest();
        req.setBookingId(300L);
        req.setCustomRate(BigDecimal.valueOf(100.00));
        req.setDescription("Custom line description");

        when(costService.addLineItem(eq(100L), eq(300L), eq(BigDecimal.valueOf(100.00)), eq("Custom line description"), eq(1L)))
                .thenReturn(testLineItem);

        mockMvc.perform(post("/api/cost/invoices/100/lines")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1000)));
    }
}
