package org.example.backend9.service.inventory;

import lombok.RequiredArgsConstructor;
import org.example.backend9.dto.request.inventory.PriceSetupRequest;
import org.example.backend9.dto.response.inventory.PriceResponse;
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

    public List<PriceResponse> getAll() {
        return pricingRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public PriceResponse setupPrice(PriceSetupRequest request) {
        ProductPricing pricing = pricingRepository
                .findByVariantIdAndStoreId(request.getVariantId(), request.getStoreId())
                .orElse(new ProductPricing());

        ProductVariant variant = variantRepository.findById(Long.valueOf(request.getVariantId()))
                .orElseThrow(() -> new RuntimeException("Không thấy biến thể"));

        pricing.setVariant(variant);
        pricing.setProduct(variant.getProduct());

        if (request.getStoreId() != null) {
            Store store = storeRepository.findById(request.getStoreId()).orElse(null);
            pricing.setStore(store);
        }

        pricing.setBaseCostPrice(request.getCostPrice());
        pricing.setBaseRetailPrice(request.getRetailPrice());
        pricing.setWholesalePrice(request.getWholesalePrice());
        pricing.setStatus(request.getStatus());

        ProductPricing saved = pricingRepository.save(pricing);

        syncToGoogleSheets(saved, "Thiết lập giá");

        return mapToResponse(saved);
    }

    @Transactional
    public String delete(Integer id) {
        pricingRepository.deleteById(Long.valueOf(id));
        return "Đã xóa bảng giá thành công";
    }

    @Transactional
    public void approvePrice(Integer id) {
        ProductPricing pricing = pricingRepository.findById(Long.valueOf(id)).orElseThrow();
        pricing.setStatus("Đang áp dụng");
        ProductPricing saved = pricingRepository.save(pricing);
        syncToGoogleSheets(saved, "Duyệt giá");
    }
    private void syncToGoogleSheets(ProductPricing p, String actionType) {
        try {
            List<Object> rowData = Arrays.asList(
                    p.getId() != null ? p.getId().toString() : "",
                    p.getProduct() != null ? p.getProduct().getCode() : "",
                    p.getVariant() != null ? p.getVariant().getVariantName() : "",
                    p.getStore() != null ? p.getStore().getName() : "Tất cả chi nhánh",
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

    private PriceResponse mapToResponse(ProductPricing p) {
        return PriceResponse.builder()
                .id(p.getId())
                .productCode(p.getProduct() != null ? p.getProduct().getCode() : null)
                .variantName(p.getVariant() != null ? p.getVariant().getVariantName() : (p.getProduct() != null ? p.getProduct().getName() : ""))
                .storeName(p.getStore() != null ? p.getStore().getName() : "Tất cả chi nhánh")
                .costPrice(p.getBaseCostPrice())
                .retailPrice(p.getBaseRetailPrice())
                .wholesalePrice(p.getWholesalePrice())
                .status(p.getStatus())
                .build();
    }
}