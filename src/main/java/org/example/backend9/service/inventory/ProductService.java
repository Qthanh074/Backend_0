package org.example.backend9.service.inventory;

import lombok.RequiredArgsConstructor;
import org.example.backend9.dto.request.inventory.ProductRequest;
import org.example.backend9.dto.request.inventory.VariantRequest; // DTO dùng cho Service
import org.example.backend9.dto.response.inventory.ProductResponse;
import org.example.backend9.dto.response.inventory.VariantDetailResponse;
import org.example.backend9.entity.inventory.*;
import org.example.backend9.repository.inventory.*;
import org.example.backend9.repository.core.SupplierRepository;
import org.example.backend9.service.GoogleSheetService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final UnitRepository unitRepository;
    private final GoogleSheetService googleSheetService;
    private final ProductVariantService variantService;

    // 👉 TIÊM THÊM 2 REPOSITORY NÀY ĐỂ XÓA TẬN GỐC LỖI KHÓA NGOẠI
    private final ProductVariantRepository variantRepository;
    private final ProductPricingRepository pricingRepository;

    public List<ProductResponse> getAll() {
        return productRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setCode(request.getCode() != null ? request.getCode() : "SP" + System.currentTimeMillis());
        product.setBarcode(request.getBarcode());
        product.setImageUrls(request.getImageUrls());
        product.setDescription(request.getDescription());
        product.setStatus(request.getStatus());

        product.setBaseCostPrice(request.getBaseCostPrice());
        product.setBaseRetailPrice(request.getBaseRetailPrice());
        product.setBaseWholesalePrice(request.getBaseWholesalePrice());

        // Xử lý Category, Supplier, Unit
        if (request.getCategoryId() != null) {
            categoryRepository.findById(request.getCategoryId().longValue()).ifPresent(product::setCategory);
        }
        if (request.getSupplierId() != null) {
            supplierRepository.findById(Math.toIntExact(request.getSupplierId())).ifPresent(product::setSupplier);
        }
        if (request.getUnitId() != null) {
            unitRepository.findById(request.getUnitId()).ifPresent(product::setUnit);
        }

        Product savedProduct = productRepository.save(product);

        // Xử lý tạo mới Biến thể (Variants)
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            for (ProductRequest.VariantRequest vReq : request.getVariants()) {
                VariantRequest serviceVariantReq = new VariantRequest();
                serviceVariantReq.setProductId(Long.valueOf(savedProduct.getId()));
                serviceVariantReq.setSku(vReq.getSku());
                serviceVariantReq.setBarcode(vReq.getBarcode());
                serviceVariantReq.setQuantity(vReq.getQuantity());
                serviceVariantReq.setColorId(vReq.getColorId());
                serviceVariantReq.setSizeId(vReq.getSizeId());
                serviceVariantReq.setUnitId(vReq.getUnitId());
                serviceVariantReq.setCostPrice(vReq.getCostPrice());
                serviceVariantReq.setSellPrice(vReq.getSellPrice());
                serviceVariantReq.setWholesalePrice(vReq.getWholesalePrice());
                serviceVariantReq.setStatus(vReq.getStatus());
                serviceVariantReq.setExtraCost(vReq.getExtraCost());
                serviceVariantReq.setExtraPrice(vReq.getExtraPrice());

                variantService.createVariant(serviceVariantReq);
            }
        }

        syncToGoogleSheets(savedProduct, request.getVariants() != null ? request.getVariants().size() : 0, "Tạo mới");
        return mapToResponse(savedProduct);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(Long.valueOf(id.intValue())).orElseThrow(() -> new RuntimeException("Không thấy SP"));

        product.setName(request.getName());
        product.setBarcode(request.getBarcode());
        product.setImageUrls(request.getImageUrls());
        product.setDescription(request.getDescription());
        product.setStatus(request.getStatus());

        product.setBaseCostPrice(request.getBaseCostPrice());
        product.setBaseRetailPrice(request.getBaseRetailPrice());
        product.setBaseWholesalePrice(request.getBaseWholesalePrice());

        if (request.getCategoryId() != null) {
            product.setCategory(categoryRepository.findById(Long.valueOf(request.getCategoryId().intValue())).orElseThrow());
        }
        if (request.getSupplierId() != null) {
            product.setSupplier(supplierRepository.findById(request.getSupplierId().intValue()).orElseThrow());
        }
        if (request.getUnitId() != null) {
            product.setUnit(unitRepository.findById(request.getUnitId()).orElseThrow());
        }

        Product savedProduct = productRepository.save(product);

        // Xóa biến thể cũ thông qua Service chuyên biệt
        List<VariantDetailResponse> oldVariants = variantService.getVariantsByProductId(id);
        for (VariantDetailResponse old : oldVariants) {
            variantService.deleteVariant(old.getId());
        }

        // Tạo lại biến thể mới
        if (request.getVariants() != null) {
            for (ProductRequest.VariantRequest vReq : request.getVariants()) {
                VariantRequest serviceVariantReq = new VariantRequest();
                serviceVariantReq.setProductId(savedProduct.getId().longValue());
                serviceVariantReq.setSku(vReq.getSku());
                serviceVariantReq.setBarcode(vReq.getBarcode());
                serviceVariantReq.setQuantity(vReq.getQuantity());
                serviceVariantReq.setColorId(vReq.getColorId());
                serviceVariantReq.setSizeId(vReq.getSizeId());
                serviceVariantReq.setUnitId(vReq.getUnitId());
                serviceVariantReq.setCostPrice(vReq.getCostPrice());
                serviceVariantReq.setSellPrice(vReq.getSellPrice());
                serviceVariantReq.setWholesalePrice(vReq.getWholesalePrice());
                serviceVariantReq.setStatus(vReq.getStatus());
                serviceVariantReq.setExtraCost(vReq.getExtraCost());
                serviceVariantReq.setExtraPrice(vReq.getExtraPrice());

                variantService.createVariant(serviceVariantReq);
            }
        }

        syncToGoogleSheets(savedProduct, request.getVariants() != null ? request.getVariants().size() : 0, "Cập nhật");
        return mapToResponse(savedProduct);
    }

    @Transactional
    public String delete(Long id) {
        Product product = productRepository.findById(Long.valueOf(id.intValue()))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        String name = product.getName();

        List<ProductVariant> variants = variantRepository.findByProductId(product.getId().longValue());

        if (variants != null && !variants.isEmpty()) {
            for (ProductVariant variant : variants) {
                List<ProductPricing> pricings = pricingRepository.findByVariantId(variant.getId().longValue());
                if (pricings != null && !pricings.isEmpty()) {
                    pricingRepository.deleteAll(pricings);
                }
            }
            variantRepository.deleteAll(variants);
        }

        productRepository.delete(product);

        return "Đã xóa thành công sản phẩm: " + name;
    }
    private void syncToGoogleSheets(Product p, int variantCount, String actionType) {
        try {
            List<Object> rowData = Arrays.asList(
                    p.getId() != null ? p.getId().toString() : "",
                    p.getCode() != null ? p.getCode() : "",
                    p.getName() != null ? p.getName() : "",
                    p.getCategory() != null ? p.getCategory().getName() : "",
                    p.getSupplier() != null ? p.getSupplier().getName() : "",
                    String.valueOf(variantCount),
                    p.getStatus() != null ? p.getStatus().name() : "ACTIVE",
                    actionType,
                    LocalDateTime.now().toString()
            );
            googleSheetService.appendRowToSheet("Product", rowData);
        } catch (Exception e) {
            System.err.println("Lỗi đồng bộ Google Sheets (Product): " + e.getMessage());
        }
    }

    private ProductResponse mapToResponse(Product product) {
        List<VariantDetailResponse> variants = variantService.getVariantsByProductId(product.getId().longValue());

        List<ProductResponse.VariantResponse> variantDtos = variants.stream().map(v ->
                ProductResponse.VariantResponse.builder()
                        .id(v.getId())
                        .sku(v.getSku())
                        .variantName(v.getVariantName())
                        .barcode(v.getBarcode())
                        .colorName(v.getColorName())
                        .sizeName(v.getSizeName())
                        .unitName(v.getUnitName())
                        .costPrice(v.getCostPrice())
                        .sellPrice(v.getSellPrice())
                        .wholesalePrice(v.getWholesalePrice())
                        .quantity(v.getQuantity())
                        .status(v.getStatus())
                        .extraCost(v.getExtraCost())
                        .extraPrice(v.getExtraPrice())
                        .build()
        ).collect(Collectors.toList());

        return ProductResponse.builder()
                .id(product.getId().longValue())
                .name(product.getName())
                .code(product.getCode())
                .barcode(product.getBarcode())
                .categoryId(product.getCategory() != null ? product.getCategory().getId().longValue() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : "")
                .supplierId(product.getSupplier() != null ? product.getSupplier().getId().longValue() : null)
                .supplierName(product.getSupplier() != null ? product.getSupplier().getName() : "")
                .unitId(product.getUnit() != null ? product.getUnit().getId().longValue() : null)
                .unitName(product.getUnit() != null ? product.getUnit().getName() : "")
                .imageUrls(product.getImageUrls())
                .description(product.getDescription())
                .status(product.getStatus())
                .baseCostPrice(product.getBaseCostPrice())
                .baseRetailPrice(product.getBaseRetailPrice())
                .baseWholesalePrice(product.getBaseWholesalePrice())
                .variants(variantDtos)
                .build();
    }
}