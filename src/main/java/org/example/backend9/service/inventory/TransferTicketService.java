package org.example.backend9.service.inventory;

import lombok.RequiredArgsConstructor;
import org.example.backend9.dto.request.inventory.TransferTicketRequest;
import org.example.backend9.dto.response.inventory.TransferTicketResponse;
import org.example.backend9.entity.inventory.*;
import org.example.backend9.enums.TicketStatus;
import org.example.backend9.repository.core.EmployeeRepository;
import org.example.backend9.repository.core.StoreRepository;
import org.example.backend9.repository.inventory.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransferTicketService {

    private final TransferTicketRepository ticketRepository;
    private final TransferTicketDetailRepository detailRepository;
    private final ProductVariantRepository variantRepository;
    private final StoreRepository storeRepository;
    private final EmployeeRepository employeeRepository;

    public List<TransferTicketResponse> getAll() {
        return ticketRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public TransferTicketResponse create(TransferTicketRequest request) {
        TransferTicket ticket = new TransferTicket();
        ticket.setCode("CK" + System.currentTimeMillis() % 1000000);
        ticket.setTransferDate(LocalDateTime.now());
        ticket.setStatus(TicketStatus.PENDING);

        ticket.setFromStore(storeRepository.findById(request.getFromStoreId()).orElseThrow());
        ticket.setToStore(storeRepository.findById(request.getToStoreId()).orElseThrow());

        if (request.getCreatedById() != null) {
            ticket.setCreatedBy(employeeRepository.findById(request.getCreatedById()).orElse(null));
        }

        TransferTicket savedTicket = ticketRepository.save(ticket);
        saveDetails(savedTicket, request.getDetails());

        return mapToResponse(savedTicket);
    }

    // CHỨC NĂNG SỬA
    @Transactional
    public TransferTicketResponse update(Integer id, TransferTicketRequest request) {
        TransferTicket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu chuyển kho"));

        // Chỉ cho phép sửa khi ở trạng thái PENDING (Chưa xuất hàng)
        if (ticket.getStatus() != TicketStatus.PENDING) {
            throw new RuntimeException("Không thể sửa phiếu đã xuất hàng hoặc đã hoàn thành");
        }

        ticket.setFromStore(storeRepository.findById(request.getFromStoreId()).orElseThrow());
        ticket.setToStore(storeRepository.findById(request.getToStoreId()).orElseThrow());

        // Xóa chi tiết cũ và lưu lại chi tiết mới
        List<TransferTicketDetail> oldDetails = detailRepository.findByTransferTicketId(id);
        detailRepository.deleteAll(oldDetails);

        saveDetails(ticket, request.getDetails());

        return mapToResponse(ticketRepository.save(ticket));
    }

    // CHỨC NĂNG XÓA
    @Transactional
    public void delete(Integer id) {
        TransferTicket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu"));

        // Nếu hàng đang đi đường hoặc đã nhận thì không được xóa (để đảm bảo tính toàn vẹn kho)
        if (ticket.getStatus() != TicketStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể xóa phiếu ở trạng thái Chờ xử lý");
        }

        List<TransferTicketDetail> details = detailRepository.findByTransferTicketId(id);
        detailRepository.deleteAll(details);
        ticketRepository.delete(ticket);
    }

    private void saveDetails(TransferTicket ticket, List<TransferTicketRequest.TransferTicketDetailRequest> detailRequests) {
        int totalQty = 0;
        List<TransferTicketDetail> details = new ArrayList<>();

        for (TransferTicketRequest.TransferTicketDetailRequest dReq : detailRequests) {
            ProductVariant variant = variantRepository.findById(dReq.getProductVariantId()).orElseThrow();
            TransferTicketDetail detail = new TransferTicketDetail();
            detail.setTransferTicket(ticket);
            detail.setProductVariant(variant);
            detail.setQuantity(dReq.getQuantity());

            totalQty += dReq.getQuantity();
            details.add(detail);
        }
        detailRepository.saveAll(details);
        ticket.setTotalQuantity(totalQty);
    }

    @Transactional
    public TransferTicketResponse processTransfer(Integer id) {
        TransferTicket ticket = ticketRepository.findById(id).orElseThrow();
        if (ticket.getStatus() != TicketStatus.PENDING) throw new RuntimeException("Trạng thái không hợp lệ");

        List<TransferTicketDetail> details = detailRepository.findByTransferTicketId(id);
        for (TransferTicketDetail d : details) {
            ProductVariant v = d.getProductVariant();
            if (v.getQuantity() < d.getQuantity()) {
                throw new RuntimeException("Kho xuất không đủ hàng: " + v.getSku());
            }
            v.setQuantity(v.getQuantity() - d.getQuantity());
            variantRepository.save(v);
        }

        ticket.setStatus(TicketStatus.IN_TRANSIT);
        return mapToResponse(ticketRepository.save(ticket));
    }

    @Transactional
    public TransferTicketResponse confirmReceipt(Integer id) {
        TransferTicket ticket = ticketRepository.findById(id).orElseThrow();
        if (ticket.getStatus() != TicketStatus.IN_TRANSIT) throw new RuntimeException("Chưa xuất hàng đi");

        List<TransferTicketDetail> details = detailRepository.findByTransferTicketId(id);
        for (TransferTicketDetail d : details) {
            ProductVariant v = d.getProductVariant();
            v.setQuantity(v.getQuantity() + d.getQuantity());
            variantRepository.save(v);
        }

        ticket.setStatus(TicketStatus.COMPLETED);
        return mapToResponse(ticketRepository.save(ticket));
    }

    private TransferTicketResponse mapToResponse(TransferTicket t) {
        List<TransferTicketDetail> details = detailRepository.findByTransferTicketId(t.getId());
        return TransferTicketResponse.builder()
                .id(t.getId()).code(t.getCode()).transferDate(t.getTransferDate())
                .fromStoreName(t.getFromStore().getName()).toStoreName(t.getToStore().getName())
                .createdByName(t.getCreatedBy() != null ? t.getCreatedBy().getFullName() : "")
                .totalQuantity(t.getTotalQuantity()).status(t.getStatus().name())
                .details(details.stream().map(d -> {
                    // 🟢 ĐÂY RỒI BÁC! Xử lý ghép tên Sản phẩm + Phân loại cho đẹp 🟢
                    String pName = d.getProductVariant().getProduct() != null ? d.getProductVariant().getProduct().getName() : "";
                    String vName = d.getProductVariant().getVariantName() != null ? d.getProductVariant().getVariantName() : "";
                    String fullName = pName;

                    // Nếu có tên phân loại (như XL, L, Đỏ...) thì gạch ngang nối vào
                    if (!vName.isEmpty() && !vName.equalsIgnoreCase("Default") && !vName.equalsIgnoreCase("Mặc định")) {
                        fullName += " - " + vName;
                    }

                    return TransferTicketResponse.TransferTicketDetailResponse.builder()
                            .id(d.getId()).productVariantId(d.getProductVariant().getId().longValue())
                            .sku(d.getProductVariant().getSku())
                            .variantName(fullName) // Gọi cái fullName vừa ghép ở trên nhét vào đây
                            .quantity(d.getQuantity()).build();
                }).collect(Collectors.toList()))
                .build();
    }
}