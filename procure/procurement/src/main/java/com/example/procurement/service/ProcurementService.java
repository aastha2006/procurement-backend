package com.example.procurement.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.procurement.DTO.InviteSupplierRequest;
import com.example.procurement.DTO.ItemPrice;
import com.example.procurement.DTO.RFQRequest;
import com.example.procurement.DTO.RfqComparisonResponse;
import com.example.procurement.DTO.RfqItemDto;
import com.example.procurement.DTO.RfqVendorComparisonDto;
import com.example.procurement.DTO.SelectVendorRequest;
import com.example.procurement.DTO.SubmitQuotationDto;
import com.example.procurement.entity.GoodsReceipt;
import com.example.procurement.entity.Invoice;
import com.example.procurement.entity.InvoiceStatus;
import com.example.procurement.entity.POStatus;
import com.example.procurement.entity.PRItem;
import com.example.procurement.entity.PRStatus;
import com.example.procurement.entity.Payment;
import com.example.procurement.entity.PurchaseOrder;
import com.example.procurement.entity.PurchaseRequisition;
import com.example.procurement.entity.Quotation;
import com.example.procurement.entity.RFQ;
import com.example.procurement.entity.RFQVendor;
import com.example.procurement.entity.Vendor;
import com.example.procurement.repository.GoodsReceiptRepository;
import com.example.procurement.repository.InvoiceRepository;
import com.example.procurement.repository.PaymentRepository;
import com.example.procurement.repository.PurchaseOrderRepository;
import com.example.procurement.repository.PurchaseRequisitionItemRepository;
import com.example.procurement.repository.PurchaseRequisitionRepository;
import com.example.procurement.repository.QuotationRepository;
import com.example.procurement.repository.RFQRepository;
import com.example.procurement.repository.RFQVendorRepository;
import com.example.procurement.repository.VendorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProcurementService {
    private final PurchaseRequisitionRepository prRepo;
    private final VendorRepository vendorRepo;
    private final QuotationRepository quoteRepo;
    private final PurchaseOrderRepository poRepo;
    private final RFQRepository rfqRepo;
    private final InvoiceRepository invoiceRepo;
    private final GoodsReceiptRepository grnRepo;
    private final PaymentRepository paymentRepo;
    private final SupplierInviteService inviteService;
    private final RFQVendorRepository rfqVendorRepo;
    private final PurchaseRequisitionItemRepository prItemRepo;

    public PurchaseRequisition createPR(PurchaseRequisition pr) {
        pr.setCreatedAt(Instant.now());
        pr.setPrNumber(generatePrNumber());

        if (pr.getItems() != null) {
            pr.getItems().forEach(item -> item.setPr(pr));
        }

        PurchaseRequisition savedPr = prRepo.save(pr);
        return prRepo.findByIdWithItems(savedPr.getId()).orElse(savedPr);
    }

    private String generatePrNumber() {
        return "PR-" + LocalDate.now().toString() + "-" + UUID.randomUUID().toString().substring(0, 6);
    }

    private String generateRFQNumber() {
        return "RFQ-" + LocalDate.now().toString() + "-" + UUID.randomUUID().toString().substring(0, 6);
    }

    public RFQ createRFQ(RFQRequest request) {
        PurchaseRequisition pr = prRepo.findByIdWithItems(request.getPrId())
                .orElseThrow(() -> new RuntimeException("PR not found"));

        // 2. Fetch vendors
        List<Vendor> vendors = vendorRepo.findAllById(request.getVendorIds());

        // 3. Create main RFQ entity
        RFQ rfq = new RFQ();
        rfq.setPr(pr);
        rfq.setCreatedAt(LocalDateTime.now());
        rfq.setStatus("OPEN");
        rfq.setRfqNumber(generateRFQNumber());
        RFQ savedRfq = rfqRepo.save(rfq);

        // 4. Link onboarded vendors with RFQ
        for (Vendor vendor : vendors) {
            RFQVendor rv = new RFQVendor();
            rv.setRfq(savedRfq);
            rv.setVendor(vendor);
            rfqVendorRepo.save(rv);
        }

        // 5. Handle invited suppliers
        if (request.getInviteSuppliers() != null) {
            for (InviteSupplierRequest sup : request.getInviteSuppliers()) {

                // add each supplier in vendor table and send their credentials
                // then assign the rfq to them
                // TODO → optional email invite
                // emailService.sendInvite(sup.getEmail(), sup.getName());
            }
        }
        pr.setStatus(PRStatus.RFQ_CREATED);
        prRepo.save(pr);

        return savedRfq;
    }

    public Page<PurchaseRequisition> getPRByCreatedBy(Long createdById, int page, int pageSize) {

        Pageable pageable = PageRequest.of(page, pageSize);

        return prRepo.findByRequestedBy(createdById, pageable);
    }

    public void submitQuotation(SubmitQuotationDto dto) {

        RFQ rfq = rfqRepo.findById(dto.getRfqId())
                .orElseThrow(() -> new RuntimeException("RFQ not found"));

        Vendor vendor = vendorRepo.findById(dto.getVendorId())
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        // Get next revision number
        Integer lastRev = quoteRepo.findLastRevision(dto.getRfqId(), dto.getVendorId());
        int nextRev = (lastRev == null) ? 1 : lastRev + 1;

        // For each item
        for (ItemPrice ip : dto.getItems()) {

            PRItem item = prItemRepo.findById(ip.getItemId())
                    .orElseThrow(() -> new RuntimeException("Item not found"));

            Quotation q = new Quotation();
            q.setRfq(rfq);
            q.setVendor(vendor);
            q.setItem(item);
            q.setPrice(ip.getPrice());
            q.setQty(ip.getQty());
            q.setTotal(ip.getPrice() * ip.getQty());
            q.setRevisionNo(nextRev);
            q.setDeliveryTerms(dto.getDeliveryTerms());
            q.setPaymentTerms(dto.getPaymentTerms());
            q.setWarranty(dto.getWarranty());

            quoteRepo.save(q);
        }
    }

    public PurchaseOrder issuePO(Long prId, Long vendorId, Double totalAmount, Double gst) {
        PurchaseRequisition pr = prRepo.findByIdWithItems(prId).orElseThrow(null);
        Vendor vendor = vendorRepo.findById(vendorId).orElseThrow(null);

        PurchaseOrder po = PurchaseOrder.builder()
                .poNumber(generatePoNumber())
                .pr(pr)
                .vendor(vendor)
                .totalAmount(totalAmount)
                .gst(gst)
                .status(POStatus.ISSUED)
                .createdAt(Instant.now())
                .build();

        pr.setStatus(PRStatus.APPROVED);
        prRepo.save(pr);
        return poRepo.save(po);
    }

    private String generatePoNumber() {
        return "PO-" + LocalDate.now().toString() + "-" + UUID.randomUUID().toString().substring(0, 6);
    }

    public GoodsReceipt createGRN(Long poId, String receivedBy, String note) {
        PurchaseOrder po = poRepo.findById(poId).orElseThrow(null);
        GoodsReceipt grn = GoodsReceipt.builder()
                .po(po)
                .receivedBy(receivedBy)
                .receivedOn(Instant.now())
                .note(note)
                .status(com.example.procurement.entity.GRNStatus.RECEIVED)
                .build();
        return grnRepo.save(grn);
    }

    public Invoice submitInvoice(Long poId, Long vendorId, String invoiceNumber, LocalDate invoiceDate, Double amount,
            Double gst) {
        PurchaseOrder po = poRepo.findByIdWithRelations(poId).orElseThrow(() -> new RuntimeException("PO not found"));
        Vendor vendor = vendorRepo.findById(vendorId).orElseThrow(null);
        Invoice inv = Invoice.builder()
                .po(po)
                .vendor(vendor)
                .invoiceNumber(invoiceNumber)
                .invoiceDate(invoiceDate)
                .amount(amount)
                .gst(gst)
                .status(InvoiceStatus.SUBMITTED)
                .createdAt(Instant.now())
                .build();
        return invoiceRepo.save(inv);
    }

    public Payment recordPayment(Long invoiceId, Double amount, String mode, String reference) {
        Invoice inv = invoiceRepo.findByIdWithRelations(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        Payment p = Payment.builder()
                .invoice(inv)
                .amount(amount)
                .paidOn(Instant.now())
                .paymentMode(mode)
                .reference(reference)
                .build();
        inv.setStatus(InvoiceStatus.PAID);
        invoiceRepo.save(inv);
        return paymentRepo.save(p);
    }

    public PurchaseRequisition getPR(Long id) {
        return prRepo.findById(id).orElseThrow();
    }

    public Page<PurchaseRequisition> getAllPR(int page, int pageSize) {

        Pageable pageable = PageRequest.of(page, pageSize);

        return prRepo.findAll(pageable);
    }

    public Page<PurchaseRequisition> getAllApprovedPR(int page, int pageSize) {

        Pageable pageable = PageRequest.of(page, pageSize);

        return prRepo.findApprovedPR(pageable);
    }

    public List<PurchaseRequisition> getRecentPRs(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return prRepo.findRecentPRs(pageable);
    }

    public List<RFQ> getRfqsByVendor(Long vendorId) {
        return rfqRepo.findRfqsByVendorId(vendorId);
    }

    public List<RFQ> getRecentRFQs(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return rfqRepo.findRecentRFQs(pageable);
    }

    public RFQ getRFQ(Long id) {
        return rfqRepo.findByIdWithItemsAndVendors(id).orElseThrow();
    }

    public Page<RFQ> getAllRFQ(int page, int pageSize) {

        Pageable pageable = PageRequest.of(page, pageSize);

        return rfqRepo.findAll(pageable);
    }

    public Payment getPayment(Long id) {
        return paymentRepo.findById(id).orElseThrow();
    }

    public List<Payment> getAllPayments() {
        return paymentRepo.findAll();
    }

    public GoodsReceipt getGRN(Long id) {
        return grnRepo.findById(id).orElseThrow();
    }

    public List<GoodsReceipt> getAllGRN() {
        return grnRepo.findAll();
    }

    public PurchaseOrder getPO(Long id) {
        return poRepo.findByIdWithRelations(id).orElseThrow();
    }

    public List<PurchaseOrder> getAllPO() {
        return poRepo.findAll();
    }

    public PurchaseRequisition approvePR(Long prId, String approvedBy) {

        PurchaseRequisition pr = prRepo.findById(prId)
                .orElseThrow(() -> new RuntimeException("PR not found"));

        // Check status
        if (!"RAISED".equalsIgnoreCase(pr.getStatus().toString())) {
            throw new RuntimeException("PR can be approved only when status is RAISED");
        }

        pr.setStatus(PRStatus.APPROVED);

        pr.setApprovedBy(approvedBy);
        pr.setApprovedOn(LocalDateTime.now());

        return prRepo.save(pr);
    }

    public RfqComparisonResponse getComparison(Long rfqId) {

        RFQ rfq = rfqRepo.findByIdWithItemsAndVendors(rfqId)
                .orElseThrow(() -> new RuntimeException("RFQ not found"));

        // Fetch all latest quotations
        List<Quotation> latestQuotes = quoteRepo.getLatestVendorQuotes(rfqId);

        // Group by vendor
        Map<Long, List<Quotation>> byVendor = latestQuotes.stream()
                .collect(Collectors.groupingBy(q -> q.getVendor().getId()));

        List<RfqVendorComparisonDto> vendors = new ArrayList<>();

        for (var entry : byVendor.entrySet()) {
            Vendor vendor = entry.getValue().get(0).getVendor();
            List<Quotation> qList = entry.getValue();

            double basePrice = qList.stream().mapToDouble(Quotation::getPrice).sum();
            double gst = basePrice * 0.18;
            double total = basePrice + gst;

            RfqVendorComparisonDto dto = new RfqVendorComparisonDto();
            dto.setVendorId(vendor.getId());
            dto.setVendorName(vendor.getName());
            // dto.setVendorCode(vendor.getCode());
            // dto.setRating(vendor.getRating());

            dto.setBasePrice(basePrice);
            dto.setGst(gst);
            dto.setTotal(total);

            Quotation first = qList.get(0); // latest revision, any row contains the header fields

            dto.setDelivery(first.getDeliveryTerms());
            dto.setWarranty(first.getWarranty());
            dto.setPaymentTerms(first.getPaymentTerms());

            vendors.add(dto);
        }

        // Mark Lowest
        double minTotal = vendors.stream().mapToDouble(RfqVendorComparisonDto::getTotal).min().orElse(Double.MAX_VALUE);
        vendors.forEach(v -> v.setLowest(v.getTotal() == minTotal));

        // Best Value (lowest + high rating)
        vendors.stream()
                .max(Comparator.comparingDouble(v -> v.getRating() / v.getTotal()))
                .ifPresent(v -> v.setBestValue(true));

        // Prepare final response
        RfqComparisonResponse response = new RfqComparisonResponse();
        response.setRfqId(rfq.getId());
        response.setPrNumber(rfq.getPr().getPrNumber());
        response.setItems(
                rfq.getPr().getItems().stream().map(item -> {
                    RfqItemDto dto = new RfqItemDto();
                    dto.setItemId(item.getId());
                    dto.setItemDescription(item.getItemDescription());
                    dto.setQuantity(item.getQuantity());
                    dto.setUnit(item.getUnit());
                    return dto;
                }).collect(Collectors.toList()));
        response.setVendors(vendors);
        response.setRecommendation("Based on price, quality, and terms, the best value vendor is auto-selected.");

        return response;
    }

    public RFQ selectVendor(Long rfqId, SelectVendorRequest req) {

        RFQ rfq = rfqRepo.findById(rfqId)
                .orElseThrow(() -> new RuntimeException("RFQ not found"));

        Vendor vendor = vendorRepo.findById(req.getVendorId())
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        // Validation: vendor must be part of RFQ
        boolean invited = rfqVendorRepo.existsByRfqIdAndVendorId(rfqId, req.getVendorId());
        if (!invited) {
            throw new RuntimeException("This vendor did not participate in the RFQ");
        }

        rfq.setSelectedVendor(vendor.getId());
        rfq.setSelectedBy(req.getSelectedBy());
        rfq.setSelectedOn(LocalDateTime.now());

        // Update RFQ status
        rfq.setStatus("VENDOR_SELECTED");

        return rfqRepo.save(rfq);
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepo.findAll();
    }

    public Invoice getInvoice(Long id) {
        return invoiceRepo.findByIdWithRelations(id).orElseThrow(() -> new RuntimeException("Invoice not found"));
    }
}
