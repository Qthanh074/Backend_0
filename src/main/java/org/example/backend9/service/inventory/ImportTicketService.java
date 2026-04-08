package org.example.backend9.service.inventory;

import lombok.RequiredArgsConstructor;
import org.example.backend9.dto.request.inventory.ImportTicketRequest;
import org.example.backend9.dto.response.inventory.ImportTicketResponse;
import org.example.backend9.entity.core.Employee;
import org.example.backend9.entity.core.Store;
import org.example.backend9.entity.core.Supplier;
import org.example.backend9.entity.inventory.ImportTicket;
import org.example.backend9.entity.inventory.ImportTicketDetail;
import org.example.backend9.entity.inventory.ProductVariant;
import org.example.backend9.enums.TicketStatus;
import org.example.backend9.repository.core.EmployeeRepository;
import org.example.backend9.repository.core.StoreRepository;
import org.example.backend9.repository.core.SupplierRepository;
import org.example.backend9.repository.inventory.ImportTicketDetailRepository;
import org.example.backend9.repository.inventory.ImportTicketRepository;
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
public class ImportTicketService {

    private final ImportTicketRepository importTicketRepository;
    private final ImportTicketDetailRepository detailRepository;
    private final ProductVariantRepository variantRepository;
    private final SupplierRepository supplierRepository;
    private final EmployeeRepository employeeRepository;
    // 🟢 THÊM STORE REPOSITORY
    private final StoreRepository storeRepository;
    private final GoogleSheetService googleSheetService;

    public List<ImportTicketResponse> getAll() {
        return importTicketRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ImportTicketResponse getById(Integer id) {
        ImportTicket ticket = importTicketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập"));
        return mapToResponse(ticket);
    }

    @Transactional
    public ImportTicketResponse create(ImportTicketRequest request) {
        ImportTicket ticket = new ImportTicket();
        ticket.setCode("PN" + System.currentTimeMillis() % 1000000);
        ticket.setImportDate(LocalDateTime.now());

        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà cung cấp"));
        ticket.setSupplier(supplier);

        // 🟢 TÌM VÀ GÁN CỬA HÀNG
        if (request.getStoreId() == null) {
            throw new RuntimeException("Vui lòng chọn cửa hàng để nhập kho");
        }
        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cửa hàng nhập kho"));
        ticket.setStore(store);

        if (request.getCreatedById() != null) {
            Employee employee = employeeRepository.findById(request.getCreatedById())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên lập phiếu"));
            ticket.setCreatedBy(employee);
        }

        ImportTicket savedTicket = importTicketRepository.save(ticket);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<ImportTicketDetail> details = new ArrayList<>();

        for (ImportTicketRequest.ImportTicketDetailRequest detailReq : request.getDetails()) {
            ProductVariant variant = variantRepository.findById(detailReq.getProductVariantId().longValue())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm biến thể ID: " + detailReq.getProductVariantId()));

            variant.setQuantity(variant.getQuantity() + detailReq.getQuantity());
            variantRepository.save(variant);

            ImportTicketDetail detail = new ImportTicketDetail();
            detail.setImportTicket(savedTicket);
            detail.setProductVariant(variant);
            detail.setQuantity(detailReq.getQuantity());
            detail.setUnitPrice(detailReq.getUnitPrice() != null ? detailReq.getUnitPrice() : BigDecimal.ZERO);

            totalAmount = totalAmount.add(detail.getUnitPrice().multiply(new BigDecimal(detail.getQuantity())));
            details.add(detail);
        }
        detailRepository.saveAll(details);

        savedTicket.setTotalAmount(totalAmount);
        BigDecimal paidAmount = request.getPaidAmount() != null ? request.getPaidAmount() : BigDecimal.ZERO;
        savedTicket.setPaidAmount(paidAmount);

        BigDecimal debtAmount = totalAmount.subtract(paidAmount);
        if (debtAmount.compareTo(BigDecimal.ZERO) <= 0) {
            debtAmount = BigDecimal.ZERO;
            savedTicket.setStatus(TicketStatus.COMPLETED);
        } else {
            savedTicket.setStatus(TicketStatus.DEBT);
            supplier.setDebt(supplier.getDebt() + debtAmount.doubleValue());
            supplierRepository.save(supplier);
        }
        savedTicket.setDebtAmount(debtAmount);

        ImportTicket finalTicket = importTicketRepository.save(savedTicket);
        syncToGoogleSheets(finalTicket, "Tạo mới");

        return mapToResponse(finalTicket);
    }

    @Transactional
    public ImportTicketResponse update(Integer id, ImportTicketRequest request) {
        ImportTicket ticket = importTicketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập"));

        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new RuntimeException("Không thể sửa phiếu nhập đã hủy!");
        }

        Map<Long, Integer> inventoryChanges = new HashMap<>();

        List<ImportTicketDetail> oldDetails = detailRepository.findByImportTicketId(id);
        for (ImportTicketDetail old : oldDetails) {
            Long variantId = old.getProductVariant().getId().longValue();
            inventoryChanges.put(variantId, inventoryChanges.getOrDefault(variantId, 0) - old.getQuantity());
        }
        detailRepository.deleteAll(oldDetails);

        Supplier oldSupplier = ticket.getSupplier();
        if (ticket.getDebtAmount().compareTo(BigDecimal.ZERO) > 0) {
            oldSupplier.setDebt(oldSupplier.getDebt() - ticket.getDebtAmount().doubleValue());
            supplierRepository.save(oldSupplier);
        }

        Supplier newSupplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà cung cấp"));
        ticket.setSupplier(newSupplier);

        // 🟢 CẬP NHẬT CỬA HÀNG
        if (request.getStoreId() != null) {
            Store newStore = storeRepository.findById(request.getStoreId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy cửa hàng nhập kho"));
            ticket.setStore(newStore);
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<ImportTicketDetail> newDetailsList = new ArrayList<>();

        for (ImportTicketRequest.ImportTicketDetailRequest detailReq : request.getDetails()) {
            Long variantId = detailReq.getProductVariantId().longValue();
            ProductVariant variant = variantRepository.findById(variantId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm biến thể ID: " + variantId));

            inventoryChanges.put(variantId, inventoryChanges.getOrDefault(variantId, 0) + detailReq.getQuantity());

            ImportTicketDetail newDetail = new ImportTicketDetail();
            newDetail.setImportTicket(ticket);
            newDetail.setProductVariant(variant);
            newDetail.setQuantity(detailReq.getQuantity());
            newDetail.setUnitPrice(detailReq.getUnitPrice() != null ? detailReq.getUnitPrice() : BigDecimal.ZERO);

            totalAmount = totalAmount.add(newDetail.getUnitPrice().multiply(new BigDecimal(newDetail.getQuantity())));
            newDetailsList.add(newDetail);
        }
        detailRepository.saveAll(newDetailsList);

        for (Map.Entry<Long, Integer> entry : inventoryChanges.entrySet()) {
            if (entry.getValue() == 0) continue;
            ProductVariant variant = variantRepository.findById(entry.getKey()).orElseThrow();
            int finalQuantity = variant.getQuantity() + entry.getValue();
            if (finalQuantity < 0) {
                throw new RuntimeException("Tồn kho sản phẩm " + variant.getSku() + " sẽ bị âm nếu áp dụng thay đổi này. (Do hàng đã được xuất bán)");
            }
            variant.setQuantity(finalQuantity);
            variantRepository.save(variant);
        }

        ticket.setTotalAmount(totalAmount);
        BigDecimal paidAmount = request.getPaidAmount() != null ? request.getPaidAmount() : BigDecimal.ZERO;
        ticket.setPaidAmount(paidAmount);

        BigDecimal debtAmount = totalAmount.subtract(paidAmount);
        if (debtAmount.compareTo(BigDecimal.ZERO) <= 0) {
            debtAmount = BigDecimal.ZERO;
            ticket.setStatus(TicketStatus.COMPLETED);
        } else {
            ticket.setStatus(TicketStatus.DEBT);
            newSupplier.setDebt(newSupplier.getDebt() + debtAmount.doubleValue());
            supplierRepository.save(newSupplier);
        }
        ticket.setDebtAmount(debtAmount);

        ImportTicket updatedTicket = importTicketRepository.save(ticket);
        syncToGoogleSheets(updatedTicket, "Cập nhật");

        return mapToResponse(updatedTicket);
    }

    @Transactional
    public ImportTicketResponse cancelTicket(Integer id) {
        ImportTicket ticket = importTicketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập"));

        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new RuntimeException("Phiếu nhập này đã bị hủy trước đó!");
        }

        List<ImportTicketDetail> details = detailRepository.findByImportTicketId(id);
        for (ImportTicketDetail detail : details) {
            ProductVariant variant = detail.getProductVariant();
            if (variant.getQuantity() < detail.getQuantity()) {
                throw new RuntimeException("Không thể hủy phiếu! Tồn kho của sản phẩm " + variant.getSku() + " sẽ bị âm.");
            }
            variant.setQuantity(variant.getQuantity() - detail.getQuantity());
            variantRepository.save(variant);
        }

        if (ticket.getDebtAmount().compareTo(BigDecimal.ZERO) > 0) {
            Supplier supplier = ticket.getSupplier();
            supplier.setDebt(supplier.getDebt() - ticket.getDebtAmount().doubleValue());
            supplierRepository.save(supplier);
        }

        ticket.setStatus(TicketStatus.CANCELLED);
        ImportTicket cancelledTicket = importTicketRepository.save(ticket);

        syncToGoogleSheets(cancelledTicket, "Hủy phiếu");

        return mapToResponse(cancelledTicket);
    }

    private void syncToGoogleSheets(ImportTicket ticket, String action) {
        try {
            List<Object> rowData = Arrays.asList(
                    ticket.getId() != null ? ticket.getId().toString() : "",
                    ticket.getCode() != null ? ticket.getCode() : "",
                    ticket.getImportDate() != null ? ticket.getImportDate().toString() : "",
                    ticket.getSupplier() != null ? ticket.getSupplier().getName() : "",
                    ticket.getTotalAmount() != null ? ticket.getTotalAmount().toString() : "0",
                    ticket.getPaidAmount() != null ? ticket.getPaidAmount().toString() : "0",
                    ticket.getDebtAmount() != null ? ticket.getDebtAmount().toString() : "0",
                    ticket.getCreatedBy() != null ? ticket.getCreatedBy().getFullName() : "Không rõ",
                    ticket.getStatus() != null ? ticket.getStatus().name() : "",
                    action,
                    LocalDateTime.now().toString()
            );
            googleSheetService.appendRowToSheet("ImportTicket", rowData);
        } catch (Exception e) {
            System.err.println("Lỗi đồng bộ Google Sheets (ImportTicket): " + e.getMessage());
        }
    }

    private ImportTicketResponse mapToResponse(ImportTicket ticket) {
        List<ImportTicketDetail> details = detailRepository.findByImportTicketId(ticket.getId());

        List<ImportTicketResponse.ImportTicketDetailResponse> detailResponses = details.stream().map(d ->
                ImportTicketResponse.ImportTicketDetailResponse.builder()
                        .id(d.getId())
                        .productVariantId(d.getProductVariant().getId())
                        .variantName(d.getProductVariant().getVariantName())
                        .sku(d.getProductVariant().getSku())
                        .quantity(d.getQuantity())
                        .unitPrice(d.getUnitPrice())
                        .totalValue(d.getUnitPrice().multiply(new BigDecimal(d.getQuantity())))
                        .build()
        ).collect(Collectors.toList());

        return ImportTicketResponse.builder()
                .id(ticket.getId())
                .code(ticket.getCode())
                .importDate(ticket.getImportDate())
                .supplierName(ticket.getSupplier() != null ? ticket.getSupplier().getName() : "")
                .createdByName(ticket.getCreatedBy() != null ? ticket.getCreatedBy().getFullName() : "")
                // 🟢 MAP TRẢ VỀ CHO FRONTEND
                .storeId(ticket.getStore() != null ? ticket.getStore().getId() : null)
                .storeName(ticket.getStore() != null ? ticket.getStore().getName() : "")
                .totalAmount(ticket.getTotalAmount())
                .paidAmount(ticket.getPaidAmount())
                .debtAmount(ticket.getDebtAmount())
                .status(ticket.getStatus() == TicketStatus.DEBT ? "Ghi nợ" :
                        (ticket.getStatus() == TicketStatus.COMPLETED ? "Đã thanh toán" : "Đã hủy"))
                .details(detailResponses)
                .build();
    }
}