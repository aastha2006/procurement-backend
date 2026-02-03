package com.example.procurement.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.procurement.DTO.ApiResponse;
import com.example.procurement.DTO.RFQRequest;
import com.example.procurement.DTO.RfqComparisonResponse;
import com.example.procurement.DTO.SelectVendorRequest;
import com.example.procurement.DTO.SubmitQuotationDto;
import com.example.procurement.DTO.PaymentDTO;
import com.example.procurement.entity.GoodsReceipt;
import com.example.procurement.entity.Invoice;
import com.example.procurement.entity.Payment;
import com.example.procurement.entity.PurchaseOrder;
import com.example.procurement.entity.PurchaseRequisition;
import com.example.procurement.entity.RFQ;
import com.example.procurement.service.ProcurementService;

@RestController
@RequestMapping("/api/procurement")

public class ProcurementController {

    private final ProcurementService service;

    public ProcurementController(ProcurementService service) {
        this.service = service;
    }

    @GetMapping("/version")
    public ResponseEntity<String> getVersion() {
        return ResponseEntity.ok("v2-bypass-active-" + java.time.LocalDateTime.now());
    }

    // ------------------ CREATE APIs ------------------

    @PostMapping("/pr")
    public ResponseEntity<PurchaseRequisition> createPR(@RequestBody PurchaseRequisition pr) {
        return ResponseEntity.ok(service.createPR(pr));
    }

    @PostMapping("/rfq")
    public ResponseEntity<RFQ> createRFQ(@RequestBody RFQRequest request) {
        return ResponseEntity.ok(service.createRFQ(request));
    }

    @PostMapping("/rfq/submit")
    public ResponseEntity<ApiResponse> submit(@RequestBody SubmitQuotationDto dto) {

        service.submitQuotation(dto);

        ApiResponse response = new ApiResponse(
                true,
                "Quotation submitted successfully"

        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/po")
    public ResponseEntity<PurchaseOrder> issuePO(@RequestParam Long prId,
            @RequestParam Long vendorId,
            @RequestParam Double totalAmount,
            @RequestParam Double gst) {
        return ResponseEntity.ok(service.issuePO(prId, vendorId, totalAmount, gst));
    }

    @PostMapping("/grn")
    public ResponseEntity<GoodsReceipt> createGRN(@RequestParam Long poId,
            @RequestParam String receivedBy,
            @RequestParam(required = false) String note) {
        return ResponseEntity.ok(service.createGRN(poId, receivedBy, note));
    }

    @PostMapping("/invoice")
    public ResponseEntity<Invoice> submitInvoice(@RequestParam Long poId,
            @RequestParam Long vendorId,
            @RequestParam String invoiceNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate invoiceDate,
            @RequestParam Double amount,
            @RequestParam Double gst) {
        return ResponseEntity.ok(service.submitInvoice(poId, vendorId, invoiceNumber, invoiceDate, amount, gst));
    }

    @PostMapping("/payment")
    public ResponseEntity<PaymentDTO> recordPayment(@RequestParam Long invoiceId,
            @RequestParam Double amount,
            @RequestParam String mode,
            @RequestParam(required = false) String reference) {
        Payment payment = service.recordPayment(invoiceId, amount, mode, reference);
        PaymentDTO dto = PaymentDTO.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .paymentMode(payment.getPaymentMode())
                .reference(payment.getReference())
                .paidOn(payment.getPaidOn())
                .invoiceNumber(payment.getInvoice().getInvoiceNumber())
                .build();
        return ResponseEntity.ok(dto);
    }

    // ------------------ GET APIs ------------------

    @GetMapping("/pr/{id}")
    public ResponseEntity<PurchaseRequisition> getPR(@PathVariable Long id) {
        return ResponseEntity.ok(service.getPR(id));
    }

    @GetMapping("/pr")
    public ResponseEntity<Page<PurchaseRequisition>> getAllPR(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(service.getAllPR(page, pageSize));
    }

    @GetMapping("/rfq/{id}")
    public ResponseEntity<RFQ> getRFQ(@PathVariable Long id) {
        return ResponseEntity.ok(service.getRFQ(id));
    }

    @GetMapping("/rfq")
    public ResponseEntity<Page<RFQ>> getAllRFQ(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(service.getAllRFQ(page, pageSize));
    }

    @GetMapping("/pr/approved")
    public ResponseEntity<Page<PurchaseRequisition>> getAllApprovedPR(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(service.getAllApprovedPR(page, pageSize));
    }

    // GET /api/procurement/pr/recent?limit=10
    @GetMapping("/pr/recent")
    public ResponseEntity<?> getRecentPr(
            @RequestParam(defaultValue = "10") int limit) {

        return ResponseEntity.ok(service.getRecentPRs(limit));
    }

    // GET /api/procurement/rfq/recent?limit=10
    @GetMapping("/rfq/recent")
    public ResponseEntity<?> getRecentRFQ(
            @RequestParam(defaultValue = "10") int limit) {

        return ResponseEntity.ok(service.getRecentRFQs(limit));
    }

    @GetMapping("/pr/by-created")
    public ResponseEntity<Page<PurchaseRequisition>> getPRByCreatedBy(
            @RequestParam Long createdById,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(
                service.getPRByCreatedBy(createdById, page, pageSize));
    }

    @GetMapping("/rfq/{rfqId}/comparison")
    public ResponseEntity<RfqComparisonResponse> getComparison(@PathVariable Long rfqId) {
        return ResponseEntity.ok(service.getComparison(rfqId));
    }

    @PostMapping("/rfq/{rfqId}/select-vendor")
    public ResponseEntity<ApiResponse> selectVendor(@PathVariable Long rfqId,
            @RequestBody SelectVendorRequest req) {
        service.selectVendor(rfqId, req);

        ApiResponse response = new ApiResponse(
                true,
                "Vendor selected successfully"

        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/rfq/vendor/{vendorId}")
    public ResponseEntity<List<RFQ>> getRfqsByVendor(@PathVariable Long vendorId) {
        return ResponseEntity.ok(service.getRfqsByVendor(vendorId));
    }

    // @GetMapping("/quotation/{id}")
    // public ResponseEntity<Quotation> getQuotation(@PathVariable Long id) {
    // return ResponseEntity.ok(service.getQuotation(id));
    // }

    // @GetMapping("/quotation")
    // public ResponseEntity<List<Quotation>> getAllQuotations() {
    // return ResponseEntity.ok(service.getAllQuotations());
    // }

    @GetMapping("/po/{id}")
    public ResponseEntity<PurchaseOrder> getPO(@PathVariable Long id) {
        return ResponseEntity.ok(service.getPO(id));
    }

    @GetMapping("/po")
    public ResponseEntity<List<PurchaseOrder>> getAllPO() {
        return ResponseEntity.ok(service.getAllPO());
    }

    @GetMapping("/grn/{id}")
    public ResponseEntity<GoodsReceipt> getGRN(@PathVariable Long id) {
        return ResponseEntity.ok(service.getGRN(id));
    }

    @GetMapping("/grn")
    public ResponseEntity<List<GoodsReceipt>> getAllGRN() {
        return ResponseEntity.ok(service.getAllGRN());
    }

    @PostMapping("/pr/{id}/approve")
    public ResponseEntity<PurchaseRequisition> approvePR(
            @PathVariable Long id,
            @RequestParam String approvedBy) {
        return ResponseEntity.ok(service.approvePR(id, approvedBy));
    }

    @GetMapping("/invoice/{id}")
    public ResponseEntity<Invoice> getInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(service.getInvoice(id)); // Note: service.getInvoice currently missing too? verify.
    }

    @GetMapping("/invoice")
    public ResponseEntity<List<Invoice>> getAllInvoices() {
        return ResponseEntity.ok(service.getAllInvoices());
    }

    @GetMapping("/payment/{id}")
    public ResponseEntity<PaymentDTO> getPayment(@PathVariable Long id) {
        Payment payment = service.getPayment(id);
        PaymentDTO dto = PaymentDTO.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .paymentMode(payment.getPaymentMode())
                .reference(payment.getReference())
                .paidOn(payment.getPaidOn())
                .invoiceNumber(payment.getInvoice().getInvoiceNumber())
                .build();
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/payment")
    public ResponseEntity<List<PaymentDTO>> getAllPayments() {
        List<Payment> payments = service.getAllPayments();
        List<PaymentDTO> dtos = payments.stream().map(payment -> PaymentDTO.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .paymentMode(payment.getPaymentMode())
                .reference(payment.getReference())
                .paidOn(payment.getPaidOn())
                .invoiceNumber(payment.getInvoice().getInvoiceNumber())
                .build()).collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
