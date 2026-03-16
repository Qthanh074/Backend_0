package org.example.backend9.service.inventory;

import lombok.RequiredArgsConstructor;
import org.example.backend9.dto.request.inventory.InventoryCheckRequest;
import org.example.backend9.dto.response.inventory.InventoryCheckResponse;
import org.example.backend9.entity.core.Employee;
import org.example.backend9.entity.core.Store;
import org.example.backend9.entity.inventory.*;
import org.example.backend9.repository.core.EmployeeRepository;
import org.example.backend9.repository.core.StoreRepository;
import org.example.backend9.repository.inventory.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryCheckService {

    private final InventoryCheckRepository checkRepository;
    private final InventoryCheckDetailRepository detailRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductPricingRepository pricingRepository;
    private final StoreRepository storeRepository;
    private final EmployeeRepository employeeRepository;

    public List<InventoryCheckResponse> getAll() {
        return checkRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public InventoryCheckResponse create(InventoryCheckRequest request) {
        InventoryCheck check = new InventoryCheck();
        check.setCode("PK" + System.currentTimeMillis() % 1000000);
        check.setCheckDate(LocalDateTime.now());
        check.setStatus("Đang kiểm");

        Store store = storeRepository.findById(request.getStoreId()).orElseThrow();
        check.setStore(store);

        if (request.getCheckerId() != null) {
            Employee checker = employeeRepository.findById(request.getCheckerId()).orElseThrow();
            check.setChecker(checker);
        }

        InventoryCheck savedCheck = checkRepository.save(check);
        processDetails(savedCheck, request.getDetails());

        return mapToResponse(savedCheck);
    }

    @Transactional
    public InventoryCheckResponse update(Integer id, InventoryCheckRequest request) {
        InventoryCheck check = checkRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu kiểm kho"));

        if ("Đã cân bằng".equals(check.getStatus())) {
            throw new RuntimeException("Không thể sửa phiếu đã cân bằng kho!");
        }

        // Cập nhật thông tin chung
        Store store = storeRepository.findById(request.getStoreId()).orElseThrow();
        check.setStore(store);
        if (request.getCheckerId() != null) {
            Employee checker = employeeRepository.findById(request.getCheckerId()).orElseThrow();
            check.setChecker(checker);
        }

        // Xóa chi tiết cũ để ghi đè chi tiết mới
        List<InventoryCheckDetail> oldDetails = detailRepository.findByInventoryCheckId(id);
        detailRepository.deleteAll(oldDetails);

        // Xử lý chi tiết mới
        processDetails(check, request.getDetails());

        InventoryCheck updatedCheck = checkRepository.save(check);
        return mapToResponse(updatedCheck);
    }

    @Transactional
    public String delete(Integer id) {
        InventoryCheck check = checkRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu kiểm kho"));

        if ("Đã cân bằng".equals(check.getStatus())) {
            throw new RuntimeException("Không thể xóa phiếu đã cân bằng kho!");
        }

        List<InventoryCheckDetail> details = detailRepository.findByInventoryCheckId(id);
        detailRepository.deleteAll(details);
        checkRepository.delete(check);

        return "Đã xóa thành công phiếu kiểm kho: " + check.getCode();
    }

    // Hàm dùng chung để xử lý logic tính toán chi tiết
    private void processDetails(InventoryCheck check, List<InventoryCheckRequest.InventoryCheckDetailRequest> detailRequests) {
        int totalLệch = 0;
        BigDecimal totalValueLệch = BigDecimal.ZERO;
        List<InventoryCheckDetail> details = new ArrayList<>();

        for (InventoryCheckRequest.InventoryCheckDetailRequest dReq : detailRequests) {
            ProductVariant variant = variantRepository.findById(dReq.getProductVariantId()).orElseThrow();

            // Lấy giá vốn hiện tại từ bảng Pricing để tính giá trị chênh lệch
            Double cost = pricingRepository.findByVariantId(variant.getId().longValue())
                    .stream().findFirst().map(ProductPricing::getBaseCostPrice).orElse(0.0);

            InventoryCheckDetail detail = new InventoryCheckDetail();
            detail.setInventoryCheck(check);
            detail.setProductVariant(variant);
            detail.setSystemQuantity(variant.getQuantity());
            detail.setActualQuantity(dReq.getActualQuantity());
            detail.setDiscrepancy(detail.getActualQuantity() - detail.getSystemQuantity());
            detail.setUnitCost(BigDecimal.valueOf(cost));

            totalLệch += detail.getDiscrepancy();
            totalValueLệch = totalValueLệch.add(detail.getUnitCost().multiply(BigDecimal.valueOf(detail.getDiscrepancy())));
            details.add(detail);
        }

        detailRepository.saveAll(details);
        check.setTotalDiscrepancyQty(totalLệch);
        check.setTotalDiscrepancyValue(totalValueLệch);
    }

    @Transactional
    public InventoryCheckResponse balanceInventory(Integer checkId) {
        InventoryCheck check = checkRepository.findById(checkId).orElseThrow();
        if ("Đã cân bằng".equals(check.getStatus())) throw new RuntimeException("Phiếu này đã được cân bằng!");

        List<InventoryCheckDetail> details = detailRepository.findByInventoryCheckId(checkId);
        for (InventoryCheckDetail detail : details) {
            ProductVariant variant = detail.getProductVariant();
            // Cập nhật số lượng thực tế vào kho hệ thống
            variant.setQuantity(detail.getActualQuantity());
            variantRepository.save(variant);
        }

        check.setStatus("Đã cân bằng");
        InventoryCheck updated = checkRepository.save(check);
        return mapToResponse(updated);
    }

    private InventoryCheckResponse mapToResponse(InventoryCheck c) {
        List<InventoryCheckDetail> details = detailRepository.findByInventoryCheckId(c.getId());
        return InventoryCheckResponse.builder()
                .id(c.getId())
                .code(c.getCode())
                .checkDate(c.getCheckDate())
                .storeName(c.getStore().getName())
                .checkerName(c.getChecker() != null ? c.getChecker().getFullName() : "")
                .totalDiscrepancyQty(c.getTotalDiscrepancyQty())
                .totalDiscrepancyValue(c.getTotalDiscrepancyValue())
                .status(c.getStatus())
                .details(details.stream().map(d -> InventoryCheckResponse.InventoryCheckDetailResponse.builder()
                        .id(d.getId())
                        .productVariantId(d.getProductVariant().getId().longValue())
                        .sku(d.getProductVariant().getSku())
                        .variantName(d.getProductVariant().getVariantName())
                        .systemQuantity(d.getSystemQuantity())
                        .actualQuantity(d.getActualQuantity())
                        .discrepancy(d.getDiscrepancy())
                        .unitCost(d.getUnitCost())
                        .discrepancyValue(d.getUnitCost().multiply(BigDecimal.valueOf(d.getDiscrepancy())))
                        .build()).collect(Collectors.toList()))
                .build();
    }
}