package org.example.backend9.service.inventory;

import lombok.RequiredArgsConstructor;
import org.example.backend9.dto.request.inventory.ExportTicketRequest;
import org.example.backend9.dto.response.inventory.ExportTicketResponse;
import org.example.backend9.entity.core.Employee;
import org.example.backend9.entity.inventory.ExportTicket;
import org.example.backend9.entity.inventory.ExportTicketDetail;
import org.example.backend9.entity.inventory.ProductVariant;
import org.example.backend9.enums.TicketStatus;
import org.example.backend9.repository.core.EmployeeRepository;
import org.example.backend9.repository.inventory.ExportTicketDetailRepository;
import org.example.backend9.repository.inventory.ExportTicketRepository;
import org.example.backend9.repository.inventory.ProductVariantRepository;
import org.example.backend9.service.GoogleSheetService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExportTicketService {

    private final ExportTicketRepository exportTicketRepository;
    private final ExportTicketDetailRepository detailRepository;
    private final ProductVariantRepository variantRepository;
    private final EmployeeRepository employeeRepository;
    private final GoogleSheetService googleSheetService; // Thêm service Google Sheets

    public List<ExportTicketResponse> getAll() {
        return exportTicketRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ExportTicketResponse getById(Integer id) {
        ExportTicket ticket = exportTicketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu xuất"));
        return mapToResponse(ticket);
    }

    @Transactional
    public ExportTicketResponse create(ExportTicketRequest request) {
        ExportTicket ticket = new ExportTicket();
        ticket.setCode("PX" + System.currentTimeMillis() % 1000000); // Sinh mã phiếu
        ticket.setExportDate(LocalDateTime.now());
        ticket.setReason(request.getReason());
        ticket.setCustomerName(request.getCustomerName());
        ticket.setStatus(TicketStatus.COMPLETED); // Xuất xong là hoàn thành luôn

        if (request.getCreatedById() != null) {
            Employee employee = employeeRepository.findById(request.getCreatedById())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên lập phiếu"));
            ticket.setCreatedBy(employee);
        }

        ExportTicket savedTicket = exportTicketRepository.save(ticket);

        BigDecimal totalValue = BigDecimal.ZERO;
        List<ExportTicketDetail> details = new ArrayList<>();

        for (ExportTicketRequest.ExportTicketDetailRequest detailReq : request.getDetails()) {
            ProductVariant variant = variantRepository.findById(detailReq.getProductVariantId().longValue())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm biến thể ID: " + detailReq.getProductVariantId()));

            // 1. Kiểm tra tồn kho trước khi xuất
            if (variant.getQuantity() < detailReq.getQuantity()) {
                throw new RuntimeException("Sản phẩm " + variant.getVariantName() + " (SKU: " + variant.getSku() + ") không đủ tồn kho!");
            }

            // 2. Trừ tồn kho
            variant.setQuantity(variant.getQuantity() - detailReq.getQuantity());
            variantRepository.save(variant);

            // 3. Tạo chi tiết phiếu xuất
            ExportTicketDetail detail = new ExportTicketDetail();
            detail.setExportTicket(savedTicket);
            detail.setProductVariant(variant);
            detail.setQuantity(detailReq.getQuantity());
            detail.setUnitPrice(detailReq.getUnitPrice() != null ? detailReq.getUnitPrice() : BigDecimal.ZERO);

            totalValue = totalValue.add(detail.getUnitPrice().multiply(new BigDecimal(detail.getQuantity())));
            details.add(detail);
        }

        detailRepository.saveAll(details);
        savedTicket.setTotalValue(totalValue);

        ExportTicket finalTicket = exportTicketRepository.save(savedTicket);

        // Đồng bộ lên Google Sheets khi tạo mới
        syncToGoogleSheets(finalTicket, "Tạo mới");

        return mapToResponse(finalTicket);
    }

    @Transactional
    public ExportTicketResponse update(Integer id, ExportTicketRequest request) {
        ExportTicket ticket = exportTicketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu xuất"));

        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new RuntimeException("Không thể sửa phiếu xuất đã bị hủy!");
        }

        // Dùng Map để tính chênh lệch tồn kho (Net Change)
        Map<Long, Integer> inventoryChanges = new HashMap<>();

        // --- 1. HOÀN TÁC CHI TIẾT CŨ ---
        List<ExportTicketDetail> oldDetails = detailRepository.findByExportTicketId(id);
        for (ExportTicketDetail old : oldDetails) {
            Long variantId = old.getProductVariant().getId().longValue();
            // Lấy lại số lượng đã xuất cũ (cộng trả lại vào kho)
            inventoryChanges.put(variantId, inventoryChanges.getOrDefault(variantId, 0) + old.getQuantity());
        }
        detailRepository.deleteAll(oldDetails); // Xóa các dòng chi tiết cũ

        // --- 2. CẬP NHẬT THÔNG TIN PHIẾU CHÍNH ---
        ticket.setReason(request.getReason());
        ticket.setCustomerName(request.getCustomerName());

        BigDecimal totalValue = BigDecimal.ZERO;
        List<ExportTicketDetail> newDetailsList = new ArrayList<>();

        // --- 3. TÍNH TOÁN CHI TIẾT MỚI VÀ TRỪ TỒN KHO ---
        for (ExportTicketRequest.ExportTicketDetailRequest detailReq : request.getDetails()) {
            Long variantId = detailReq.getProductVariantId().longValue();
            ProductVariant variant = variantRepository.findById(variantId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm biến thể ID: " + variantId));

            // Trừ đi số lượng xuất mới trong Map bù trừ
            inventoryChanges.put(variantId, inventoryChanges.getOrDefault(variantId, 0) - detailReq.getQuantity());

            ExportTicketDetail newDetail = new ExportTicketDetail();
            newDetail.setExportTicket(ticket);
            newDetail.setProductVariant(variant);
            newDetail.setQuantity(detailReq.getQuantity());
            newDetail.setUnitPrice(detailReq.getUnitPrice() != null ? detailReq.getUnitPrice() : BigDecimal.ZERO);

            totalValue = totalValue.add(newDetail.getUnitPrice().multiply(new BigDecimal(newDetail.getQuantity())));
            newDetailsList.add(newDetail);
        }
        detailRepository.saveAll(newDetailsList);

        // --- 4. ÁP DỤNG THAY ĐỔI LÊN TỒN KHO THỰC TẾ ---
        for (Map.Entry<Long, Integer> entry : inventoryChanges.entrySet()) {
            if (entry.getValue() == 0) continue; // Không có sự thay đổi thì bỏ qua
            ProductVariant variant = variantRepository.findById(entry.getKey()).orElseThrow();

            int finalQuantity = variant.getQuantity() + entry.getValue();
            if (finalQuantity < 0) {
                throw new RuntimeException("Sản phẩm " + variant.getVariantName() + " (SKU: " + variant.getSku() + ") không đủ tồn kho để cập nhật phiếu xuất này!");
            }
            variant.setQuantity(finalQuantity);
            variantRepository.save(variant);
        }

        ticket.setTotalValue(totalValue);
        ExportTicket updatedTicket = exportTicketRepository.save(ticket);

        // Đồng bộ G-Sheet
        syncToGoogleSheets(updatedTicket, "Cập nhật");

        return mapToResponse(updatedTicket);
    }

    @Transactional
    public ExportTicketResponse cancelTicket(Integer id) {
        ExportTicket ticket = exportTicketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu xuất"));

        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new RuntimeException("Phiếu xuất này đã bị hủy trước đó!");
        }

        ticket.setStatus(TicketStatus.CANCELLED);

        // Hoàn lại số lượng tồn kho cho các sản phẩm trong phiếu xuất bị hủy
        List<ExportTicketDetail> details = detailRepository.findByExportTicketId(id);
        for (ExportTicketDetail detail : details) {
            ProductVariant variant = detail.getProductVariant();
            variant.setQuantity(variant.getQuantity() + detail.getQuantity());
            variantRepository.save(variant);
        }

        ExportTicket cancelledTicket = exportTicketRepository.save(ticket);

        // Đồng bộ lên Google Sheets khi hủy phiếu
        syncToGoogleSheets(cancelledTicket, "Hủy phiếu");

        return mapToResponse(cancelledTicket);
    }

    // Hàm hỗ trợ đẩy dữ liệu lên Google Sheets
    private void syncToGoogleSheets(ExportTicket ticket, String action) {
        try {
            List<Object> rowData = Arrays.asList(
                    ticket.getId() != null ? ticket.getId().toString() : "",
                    ticket.getCode() != null ? ticket.getCode() : "",
                    ticket.getExportDate() != null ? ticket.getExportDate().toString() : "",
                    ticket.getReason() != null ? ticket.getReason() : "",
                    ticket.getCustomerName() != null ? ticket.getCustomerName() : "",
                    ticket.getCreatedBy() != null ? ticket.getCreatedBy().getFullName() : "Không rõ",
                    ticket.getTotalValue() != null ? ticket.getTotalValue().toString() : "0",
                    ticket.getStatus() != null ? ticket.getStatus().name() : "",
                    action,
                    LocalDateTime.now().toString()
            );

            googleSheetService.appendRowToSheet("ExportTicket", rowData);
        } catch (Exception e) {
            System.err.println("Lỗi đồng bộ Google Sheets (ExportTicket): " + e.getMessage());
        }
    }

    private ExportTicketResponse mapToResponse(ExportTicket ticket) {
        List<ExportTicketDetail> details = detailRepository.findByExportTicketId(ticket.getId());

        List<ExportTicketResponse.ExportTicketDetailResponse> detailResponses = details.stream().map(d ->
                ExportTicketResponse.ExportTicketDetailResponse.builder()
                        .id(d.getId())
                        .productVariantId(d.getProductVariant().getId())
                        .variantName(d.getProductVariant().getVariantName())
                        .sku(d.getProductVariant().getSku())
                        .quantity(d.getQuantity())
                        .unitPrice(d.getUnitPrice())
                        .totalValue(d.getUnitPrice().multiply(new BigDecimal(d.getQuantity())))
                        .build()
        ).collect(Collectors.toList());

        return ExportTicketResponse.builder()
                .id(ticket.getId())
                .code(ticket.getCode())
                .exportDate(ticket.getExportDate())
                .reason(ticket.getReason())
                .customerName(ticket.getCustomerName())
                .createdByName(ticket.getCreatedBy() != null ? ticket.getCreatedBy().getFullName() : "")
                .totalValue(ticket.getTotalValue())
                .status(ticket.getStatus() != null ? ticket.getStatus().name() : "")
                .details(detailResponses)
                .build();
    }
}