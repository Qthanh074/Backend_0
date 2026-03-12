package org.example.backend9.service.inventory;

import lombok.RequiredArgsConstructor;
import org.example.backend9.dto.request.inventory.PriceSetupRequest;
import org.example.backend9.dto.response.inventory.PriceResponse;
import org.example.backend9.entity.inventory.*;
import org.example.backend9.entity.core.Store;
import org.example.backend9.repository.inventory.*;
import org.example.backend9.repository.core.StoreRepository;
import org.example.backend9.service.ExcelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductPricingService {
    private final ProductPricingRepository pricingRepository;
    private final ProductVariantRepository variantRepository;
    private final StoreRepository storeRepository;
    private final ExcelService excelService;

    public List<PriceResponse> getAll() {
        return pricingRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public PriceResponse setupPrice(PriceSetupRequest request) {
        // Kiểm tra xem đã có bản ghi giá cho Variant này tại Store này chưa (nếu có thì update, chưa thì create)
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

        // Xuất Excel sau khi thiết lập giá
        exportExcel(saved);

        return mapToResponse(saved);
    }

    @Transactional
    public String delete(Integer id) {
        pricingRepository.deleteById(Long.valueOf(id));
        return "Đã xóa bảng giá thành công";
    }

    // Chức năng "Duyệt Giá Mới" trên giao diện của b
    @Transactional
    public void approvePrice(Integer id) {
        ProductPricing pricing = pricingRepository.findById(Long.valueOf(id)).orElseThrow();
        pricing.setStatus("Đang áp dụng");
        pricingRepository.save(pricing);
    }

    private void exportExcel(ProductPricing p) {
        try {
            List<String> headers = Arrays.asList("Mã Hàng", "Tên Hàng", "Chi Nhánh", "Giá Bán", "Trạng Thái");
            List<List<Object>> data = Arrays.asList(Arrays.asList(
                    p.getProduct().getCode(),
                    p.getVariant().getVariantName(),
                    p.getStore() != null ? p.getStore().getName() : "Tất cả chi nhánh",
                    p.getBaseRetailPrice(),
                    p.getStatus()
            ));
            excelService.exportToExcel("Price_Table_Export.xlsx", headers, data);
        } catch (Exception e) { System.err.println("Lỗi Excel: " + e.getMessage()); }
    }

    private PriceResponse mapToResponse(ProductPricing p) {
        return PriceResponse.builder()
                .id(p.getId())
                .productCode(p.getProduct().getCode())
                .variantName(p.getVariant() != null ? p.getVariant().getVariantName() : p.getProduct().getName())
                .storeName(p.getStore() != null ? p.getStore().getName() : "Tất cả chi nhánh")
                .costPrice(p.getBaseCostPrice())
                .retailPrice(p.getBaseRetailPrice())
                .wholesalePrice(p.getWholesalePrice())
                .status(p.getStatus())
                .build();
    }
}