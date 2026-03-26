package org.example.backend9.service.inventory;

import lombok.RequiredArgsConstructor;
import org.example.backend9.dto.request.inventory.ProductPricingRequest;
import org.example.backend9.dto.response.inventory.ProductPricingResponse;
import org.example.backend9.entity.inventory.*;
import org.example.backend9.entity.core.Store;
import org.example.backend9.repository.inventory.*;
import org.example.backend9.repository.core.StoreRepository;
import org.example.backend9.service.GoogleSheetService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductPricingService {
    private final ProductPricingRepository pricingRepository;
    private final ProductVariantRepository variantRepository;
    private final StoreRepository storeRepository;
    private final GoogleSheetService googleSheetService;

    public List<ProductPricingResponse> getAll() {
        return pricingRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public ProductPricingResponse setupPrice(ProductPricingRequest request) {
        // 1. Kiểm tra xem đã có bản ghi giá cho Variant này tại Store này chưa
        // Lưu ý: Repository cần có hàm findByVariantIdAndStoreId
        ProductPricing pricing = pricingRepository
                .findByVariantIdAndStoreId(request.getVariantId(), request.getStoreId())
                .stream().findFirst().orElse(new ProductPricing());

        // 2. Lấy thông tin Variant để gán Product gốc
        ProductVariant variant = variantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể ID: " + request.getVariantId()));

        pricing.setVariant(variant);
        pricing.setProduct(variant.getProduct()); // Tự động lấy Product từ Variant

        // 3. Gán Store nếu có
        if (request.getStoreId() != null) {
            Store store = storeRepository.findById(request.getStoreId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy cửa hàng ID: " + request.getStoreId()));
            pricing.setStore(store);
        }

        // 4. Gán giá (Đồng bộ tên field với Entity và Request)
        pricing.setBaseCostPrice(request.getBaseCostPrice());
        pricing.setBaseRetailPrice(request.getBaseRetailPrice());
        pricing.setWholesalePrice(request.getWholesalePrice());
        pricing.setStatus(request.getStatus() != null ? request.getStatus() : "ACTIVE");

        ProductPricing saved = pricingRepository.save(pricing);

        syncToGoogleSheets(saved, "Thiết lập giá");

        return mapToResponse(saved);
    }

    @Transactional
    public String delete(Integer id) {
        // Sử dụng Integer vì ID của ProductPricing là Integer
        ProductPricing pricing = pricingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bảng giá"));
        pricingRepository.delete(pricing);
        return "Đã xóa bảng giá thành công cho SKU: " + (pricing.getVariant() != null ? pricing.getVariant().getSku() : "");
    }

    @Transactional
    public void approvePrice(Integer id) {
        ProductPricing pricing = pricingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bảng giá"));
        pricing.setStatus("Đang áp dụng");
        ProductPricing saved = pricingRepository.save(pricing);
        syncToGoogleSheets(saved, "Duyệt giá");
    }

    private void syncToGoogleSheets(ProductPricing p, String actionType) {
        try {
            List<Object> rowData = Arrays.asList(
                    p.getId() != null ? p.getId().toString() : "",
                    p.getProduct() != null ? p.getProduct().getCode() : "",
                    p.getVariant() != null ? p.getVariant().getSku() : "", // Dùng SKU cho chuyên nghiệp
                    p.getStore() != null ? p.getStore().getName() : "Hệ thống chung",
                    p.getBaseCostPrice() != null ? p.getBaseCostPrice().toString() : "0",
                    p.getBaseRetailPrice() != null ? p.getBaseRetailPrice().toString() : "0",
                    p.getWholesalePrice() != null ? p.getWholesalePrice().toString() : "0",
                    p.getStatus() != null ? p.getStatus() : "",
                    actionType,
                    LocalDateTime.now().toString()
            );
            googleSheetService.appendRowToSheet("ProductPricing", rowData);
        } catch (Exception e) {
            System.err.println("Lỗi đồng bộ Google Sheets (ProductPricing): " + e.getMessage());
        }
    }

    private ProductPricingResponse mapToResponse(ProductPricing p) {
        String fullName = p.getProduct() != null ? p.getProduct().getName() : "";
        if (p.getVariant() != null && p.getVariant().getVariantName() != null) {
            fullName += " - " + p.getVariant().getVariantName();
        }

        return ProductPricingResponse.builder()
                .id(p.getId())
                .productId(p.getProduct() != null ? p.getProduct().getId().longValue() : null)
                .productName(fullName) // Trả về tên đầy đủ kèm biến thể
                .variantId(p.getVariant() != null ? p.getVariant().getId().longValue() : null)
                .sku(p.getVariant() != null ? p.getVariant().getSku() : "")
                .storeId(p.getStore() != null ? p.getStore().getId() : null)
                .storeName(p.getStore() != null ? p.getStore().getName() : "Tất cả chi nhánh")
                .baseCostPrice(p.getBaseCostPrice())
                .baseRetailPrice(p.getBaseRetailPrice())
                .wholesalePrice(p.getWholesalePrice())
                .status(p.getStatus())
                .build();
    }
    public List<ProductPricingResponse> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAll();
        }
        return pricingRepository.searchPricing(query)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}