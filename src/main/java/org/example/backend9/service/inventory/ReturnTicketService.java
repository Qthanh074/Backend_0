package org.example.backend9.service.inventory;

import lombok.RequiredArgsConstructor;
import org.example.backend9.dto.request.inventory.ReturnTicketRequest;
import org.example.backend9.dto.response.inventory.ReturnTicketResponse;
import org.example.backend9.entity.core.Supplier;
import org.example.backend9.entity.inventory.*;
import org.example.backend9.enums.TicketStatus;
import org.example.backend9.repository.core.EmployeeRepository;
import org.example.backend9.repository.core.SupplierRepository;
import org.example.backend9.repository.inventory.*;
import org.example.backend9.repository.sales.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReturnTicketService {

    private final ReturnTicketRepository ticketRepository;
    private final ReturnTicketDetailRepository detailRepository;
    private final ProductVariantRepository variantRepository;
    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;
    private final EmployeeRepository employeeRepository;

    public List<ReturnTicketResponse> getByReturnType(String type) {
        return ticketRepository.findByReturnType(type).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ReturnTicketResponse getById(Integer id) {
        ReturnTicket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu trả hàng"));
        return mapToResponse(ticket);
    }

    @Transactional
    public ReturnTicketResponse create(ReturnTicketRequest request) {
        ReturnTicket ticket = new ReturnTicket();
        // Tự động gán mã dựa trên type truyền từ Controller
        String prefix = "CUSTOMER_RETURN".equals(request.getReturnType()) ? "TH" : "TL";
        ticket.setCode(prefix + System.currentTimeMillis() % 1000000);
        ticket.setReturnType(request.getReturnType());
        ticket.setOriginalDocCode(request.getOriginalDocCode());
        ticket.setReturnDate(LocalDateTime.now());
        ticket.setReason(request.getReason());
        ticket.setPaymentMethod(request.getPaymentMethod());
        ticket.setStatus(TicketStatus.COMPLETED);

        setRelations(ticket, request);
        ReturnTicket savedTicket = ticketRepository.save(ticket);

        // Xử lý chi tiết và kho
        processDetailsAndStock(savedTicket, request.getDetails(), request.getReturnType());

        return mapToResponse(ticketRepository.save(savedTicket));
    }

    @Transactional
    public ReturnTicketResponse update(Integer id, ReturnTicketRequest request) {
        ReturnTicket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu trả hàng"));

        // 1. Hoàn tác (Undo) dữ liệu cũ
        undoStockAndFinance(ticket);

        // 2. Xóa chi tiết cũ
        List<ReturnTicketDetail> oldDetails = detailRepository.findByReturnTicketId(id);
        detailRepository.deleteAll(oldDetails);

        // 3. Cập nhật thông tin mới
        ticket.setReason(request.getReason());
        ticket.setOriginalDocCode(request.getOriginalDocCode());
        ticket.setPaymentMethod(request.getPaymentMethod());
        setRelations(ticket, request);

        // 4. Xử lý chi tiết mới và tính toán lại từ đầu
        processDetailsAndStock(ticket, request.getDetails(), ticket.getReturnType());

        return mapToResponse(ticketRepository.save(ticket));
    }

    @Transactional
    public void delete(Integer id) {
        ReturnTicket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu trả hàng"));

        // Hoàn tác kho và tài chính trước khi xóa hẳn
        undoStockAndFinance(ticket);

        List<ReturnTicketDetail> details = detailRepository.findByReturnTicketId(id);
        detailRepository.deleteAll(details);
        ticketRepository.delete(ticket);
    }

    // --- Hàm bổ trợ (Helper) ---

    private void setRelations(ReturnTicket ticket, ReturnTicketRequest request) {
        if (request.getCustomerId() != null)
            ticket.setCustomer(customerRepository.findById(request.getCustomerId()).orElse(null));
        if (request.getSupplierId() != null)
            ticket.setSupplier(supplierRepository.findById(request.getSupplierId()).orElse(null));
        if (request.getCreatedById() != null)
            ticket.setCreatedBy(employeeRepository.findById(request.getCreatedById()).orElse(null));
    }

    private void processDetailsAndStock(ReturnTicket ticket, List<ReturnTicketRequest.ReturnDetailRequest> detailRequests, String type) {
        BigDecimal totalRefund = BigDecimal.ZERO;
        List<ReturnTicketDetail> details = new ArrayList<>();

        for (ReturnTicketRequest.ReturnDetailRequest dReq : detailRequests) {
            ProductVariant variant = variantRepository.findById(dReq.getProductVariantId()).orElseThrow();

            // Logic kho
            if ("CUSTOMER_RETURN".equals(type)) {
                variant.setQuantity(variant.getQuantity() + dReq.getReturnQuantity()); // Khách trả -> Tăng kho
            } else {
                if (variant.getQuantity() < dReq.getReturnQuantity())
                    throw new RuntimeException("Kho không đủ hàng để trả NCC: " + variant.getSku());
                variant.setQuantity(variant.getQuantity() - dReq.getReturnQuantity()); // Trả NCC -> Giảm kho
            }
            variantRepository.save(variant);

            ReturnTicketDetail detail = new ReturnTicketDetail();
            detail.setReturnTicket(ticket);
            detail.setProductVariant(variant);
            detail.setReturnQuantity(dReq.getReturnQuantity());
            detail.setReturnPrice(dReq.getReturnPrice());
            detail.setConditionNote(dReq.getConditionNote());

            totalRefund = totalRefund.add(dReq.getReturnPrice().multiply(BigDecimal.valueOf(dReq.getReturnQuantity())));
            details.add(detail);
        }
        detailRepository.saveAll(details);
        ticket.setTotalRefundAmount(totalRefund);

        // Tài chính NCC
        if ("SUPPLIER_RETURN".equals(type) && ticket.getSupplier() != null) {
            Supplier s = ticket.getSupplier();
            s.setDebt(s.getDebt() - totalRefund.doubleValue()); // Giảm nợ phải trả cho NCC
            supplierRepository.save(s);
        }
    }

    private void undoStockAndFinance(ReturnTicket ticket) {
        List<ReturnTicketDetail> details = detailRepository.findByReturnTicketId(ticket.getId());
        for (ReturnTicketDetail d : details) {
            ProductVariant v = d.getProductVariant();
            if ("CUSTOMER_RETURN".equals(ticket.getReturnType())) {
                v.setQuantity(v.getQuantity() - d.getReturnQuantity()); // Hoàn tác khách trả -> Giảm kho
            } else {
                v.setQuantity(v.getQuantity() + d.getReturnQuantity()); // Hoàn tác trả NCC -> Tăng kho
            }
            variantRepository.save(v);
        }

        if ("SUPPLIER_RETURN".equals(ticket.getReturnType()) && ticket.getSupplier() != null) {
            Supplier s = ticket.getSupplier();
            s.setDebt(s.getDebt() + ticket.getTotalRefundAmount().doubleValue()); // Cộng lại nợ NCC
            supplierRepository.save(s);
        }
    }

    private ReturnTicketResponse mapToResponse(ReturnTicket t) {
        List<ReturnTicketDetail> details = detailRepository.findByReturnTicketId(t.getId());
        String partnerName = (t.getCustomer() != null) ? t.getCustomer().getFullName() :
                (t.getSupplier() != null ? t.getSupplier().getName() : "N/A");

        return ReturnTicketResponse.builder()
                .id(t.getId()).code(t.getCode()).returnType(t.getReturnType())
                .originalDocCode(t.getOriginalDocCode()).returnDate(t.getReturnDate())
                .partnerName(partnerName).reason(t.getReason())
                .totalRefundAmount(t.getTotalRefundAmount())
                .paymentMethod(t.getPaymentMethod() != null ? t.getPaymentMethod().name() : "")
                .createdByName(t.getCreatedBy() != null ? t.getCreatedBy().getFullName() : "")
                .status(t.getStatus().name())
                .details(details.stream().map(d -> ReturnTicketResponse.ReturnDetailResponse.builder()
                        .id(d.getId()).productVariantId(d.getProductVariant().getId().longValue())
                        .sku(d.getProductVariant().getSku()).variantName(d.getProductVariant().getVariantName())
                        .returnQuantity(d.getReturnQuantity()).returnPrice(d.getReturnPrice())
                        .total(d.getReturnPrice().multiply(BigDecimal.valueOf(d.getReturnQuantity())))
                        .conditionNote(d.getConditionNote()).build()).collect(Collectors.toList()))
                .build();
    }
}