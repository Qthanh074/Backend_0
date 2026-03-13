package org.example.backend9.service.inventory;

import lombok.RequiredArgsConstructor;
import org.example.backend9.dto.request.inventory.VariantRequest;
import org.example.backend9.dto.response.inventory.VariantDetailResponse;
import org.example.backend9.entity.inventory.*;
import org.example.backend9.enums.EntityStatus;
import org.example.backend9.repository.inventory.*;
import org.example.backend9.service.GoogleSheetService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductVariantService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductPricingRepository pricingRepository;
    private final ColorRepository colorRepository;
    private final SizeRepository sizeRepository;
    private final UnitRepository unitRepository;
    private final GoogleSheetService googleSheetService;

    // --- HÀM MAP ENTITY SANG DTO ---
    private VariantDetailResponse mapToDto(ProductVariant v) {
        ProductPricing p = pricingRepository.findByVariantId(Long.valueOf(v.getId()))
                .stream().findFirst().orElse(new ProductPricing());

        return VariantDetailResponse.builder()
                .id(Long.valueOf(v.getId()))
                .productId(Long.valueOf(v.getProduct().getId()))
                .productName(v.getProduct().getName())
                .productCode(v.getProduct().getCode())
                .sku(v.getSku())
                .barcode(v.getBarcode())
                .variantName(v.getVariantName())
                .quantity(v.getQuantity())
                .colorId(v.getColor() != null ? v.getColor().getId() : null)
                .colorName(v.getColor() != null ? v.getColor().getName() : "")
                .sizeId(v.getSize() != null ? v.getSize().getId() : null)
                .sizeName(v.getSize() != null ? v.getSize().getName() : "")
                .unitId(v.getUnit() != null ? Long.valueOf(v.getUnit().getId()) : null)
                .unitName(v.getUnit() != null ? v.getUnit().getName() : "")
                // Bổ sung 3 trường mới
                .status(v.getStatus() != null ? v.getStatus().name() : null)
                .extraCost(v.getExtraCost())
                .extraPrice(v.getExtraPrice())
                .costPrice(p.getBaseCostPrice())
                .sellPrice(p.getBaseRetailPrice())
                .wholesalePrice(p.getWholesalePrice())
                .build();
    }

    public List<VariantDetailResponse> getVariantsByProductId(Long productId) {
        return variantRepository.findByProductId(productId)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public VariantDetailResponse getVariantById(Long variantId) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể ID: " + variantId));
        return mapToDto(variant);
    }

    @Transactional
    public VariantDetailResponse createVariant(VariantRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm gốc ID: " + request.getProductId()));

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);

        String sku = request.getSku();
        if (sku == null || sku.trim().isEmpty()) {
            String colorName = request.getColorId() != null ? colorRepository.findById(Long.valueOf(request.getColorId())).map(Color::getName).orElse("") : "";
            String sizeName = request.getSizeId() != null ? sizeRepository.findById(Long.valueOf(request.getSizeId())).map(Size::getName).orElse("") : "";
            sku = product.getCode() + (colorName.isEmpty() ? "" : "-" + colorName.toUpperCase()) + (sizeName.isEmpty() ? "" : "-" + sizeName.toUpperCase());
        }

        String finalSku = sku;
        int suffix = 1;
        while (variantRepository.existsBySku(finalSku)) {
            finalSku = sku + "-" + suffix;
            suffix++;
        }
        variant.setSku(finalSku);

        String barcode = request.getBarcode();
        if (barcode != null && barcode.trim().isEmpty()) barcode = null;
        variant.setBarcode(barcode);

        variant.setQuantity(request.getQuantity());
        variant.setColor(request.getColorId() != null ? colorRepository.findById(Long.valueOf(request.getColorId())).orElse(null) : null);
        variant.setSize(request.getSizeId() != null ? sizeRepository.findById(Long.valueOf(request.getSizeId())).orElse(null) : null);
        variant.setUnit(request.getUnitId() != null ? unitRepository.findById(request.getUnitId()).orElse(null) : null);

        String colorPart = variant.getColor() != null ? variant.getColor().getName() : "";
        String sizePart = variant.getSize() != null ? variant.getSize().getName() : "";
        variant.setVariantName(colorPart + (sizePart.isEmpty() ? "" : " - " + sizePart));

        // Bổ sung xử lý 3 trường mới
        variant.setStatus(request.getStatus() != null ? EntityStatus.valueOf(request.getStatus()) : EntityStatus.ACTIVE);
        variant.setExtraCost(request.getExtraCost() != null ? request.getExtraCost() : BigDecimal.ZERO);
        variant.setExtraPrice(request.getExtraPrice() != null ? request.getExtraPrice() : BigDecimal.ZERO);

        ProductVariant savedVariant = variantRepository.save(variant);

        ProductPricing pricing = new ProductPricing();
        pricing.setProduct(product);
        pricing.setVariant(savedVariant);
        pricing.setBaseCostPrice(request.getCostPrice());
        pricing.setBaseRetailPrice(request.getSellPrice());
        pricing.setWholesalePrice(request.getWholesalePrice());
        pricingRepository.save(pricing);

        syncToGoogleSheets(product, savedVariant, pricing, "Tạo mới");

        return mapToDto(savedVariant);
    }

    @Transactional
    public VariantDetailResponse updateVariant(Long variantId, VariantRequest request) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể ID: " + variantId));

        if (request.getSku() != null && !request.getSku().trim().isEmpty() && !request.getSku().equals(variant.getSku())) {
            if (variantRepository.existsBySku(request.getSku())) {
                throw new RuntimeException("Mã SKU đã tồn tại: " + request.getSku());
            }
            variant.setSku(request.getSku());
        }

        String barcode = request.getBarcode();
        if (barcode != null && barcode.trim().isEmpty()) barcode = null;
        variant.setBarcode(barcode);

        variant.setQuantity(request.getQuantity());
        variant.setColor(request.getColorId() != null ? colorRepository.findById(Long.valueOf(request.getColorId())).orElse(null) : null);
        variant.setSize(request.getSizeId() != null ? sizeRepository.findById(Long.valueOf(request.getSizeId())).orElse(null) : null);
        variant.setUnit(request.getUnitId() != null ? unitRepository.findById(request.getUnitId()).orElse(null) : null);

        String colorPart = variant.getColor() != null ? variant.getColor().getName() : "";
        String sizePart = variant.getSize() != null ? variant.getSize().getName() : "";
        variant.setVariantName(colorPart + (sizePart.isEmpty() ? "" : " - " + sizePart));

        // Bổ sung xử lý 3 trường mới
        if (request.getStatus() != null) variant.setStatus(EntityStatus.valueOf(request.getStatus()));
        if (request.getExtraCost() != null) variant.setExtraCost(request.getExtraCost());
        if (request.getExtraPrice() != null) variant.setExtraPrice(request.getExtraPrice());

        ProductVariant savedVariant = variantRepository.save(variant);

        ProductPricing pricing = pricingRepository.findByVariantId(variantId).stream().findFirst().orElse(new ProductPricing());
        pricing.setProduct(variant.getProduct());
        pricing.setVariant(savedVariant);
        pricing.setBaseCostPrice(request.getCostPrice());
        pricing.setBaseRetailPrice(request.getSellPrice());
        pricing.setWholesalePrice(request.getWholesalePrice());
        pricingRepository.save(pricing);

        syncToGoogleSheets(variant.getProduct(), savedVariant, pricing, "Cập nhật");

        return mapToDto(savedVariant);
    }

    @Transactional
    public void deleteVariant(Long variantId) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể"));
        pricingRepository.deleteAll(pricingRepository.findByVariantId(variantId));
        variantRepository.delete(variant);
    }

    private void syncToGoogleSheets(Product product, ProductVariant variant, ProductPricing pricing, String action) {
        try {
            List<Object> rowData = Arrays.asList(
                    product.getCode(), variant.getSku(), variant.getBarcode() != null ? variant.getBarcode() : "",
                    variant.getVariantName(), pricing.getBaseCostPrice() != null ? pricing.getBaseCostPrice().toString() : "0",
                    pricing.getBaseRetailPrice() != null ? pricing.getBaseRetailPrice().toString() : "0",
                    variant.getQuantity() != null ? variant.getQuantity().toString() : "0", action, LocalDateTime.now().toString()
            );
            googleSheetService.appendRowToSheet("ProductVariant", rowData);
        } catch (Exception e) {
            System.err.println("Lỗi đồng bộ Google Sheets (Variant): " + e.getMessage());
        }
    }
}