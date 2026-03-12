package org.example.backend9.service.inventory;

import lombok.RequiredArgsConstructor;
import org.example.backend9.dto.request.inventory.ProductRequest;
import org.example.backend9.dto.response.inventory.ProductResponse;
import org.example.backend9.entity.inventory.*;
import org.example.backend9.repository.inventory.*;
import org.example.backend9.repository.core.SupplierRepository;
import org.example.backend9.service.ExcelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductPricingRepository pricingRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final ColorRepository colorRepository;
    private final SizeRepository sizeRepository;
    private final UnitRepository unitRepository;
    private final ExcelService excelService;

    public List<ProductResponse> getAll() {
        return productRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setCode(request.getCode() != null ? request.getCode() : "SP" + System.currentTimeMillis());
        product.setImageUrls(request.getImageUrls());
        product.setDescription(request.getDescription());
        product.setStatus(request.getStatus());

        product.setCategory(categoryRepository.findById(request.getCategoryId()).orElseThrow());
        product.setSupplier(supplierRepository.findById(Math.toIntExact(request.getSupplierId())).orElseThrow());

        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            Long firstUnitId = request.getVariants().get(0).getUnitId();
            if (firstUnitId != null) {
                product.setUnit(unitRepository.findById(firstUnitId).orElse(null));
            }
        }

        Product savedProduct = productRepository.save(product);

        if (request.getVariants() != null) {
            createVariants(savedProduct, request.getVariants());
        }

        exportToExcel(savedProduct, request);
        return mapToResponse(savedProduct);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Không thấy SP"));

        product.setName(request.getName());
        product.setImageUrls(request.getImageUrls());
        product.setDescription(request.getDescription());
        product.setStatus(request.getStatus());

        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            Long firstUnitId = request.getVariants().get(0).getUnitId();
            product.setUnit(unitRepository.findById(firstUnitId).orElse(null));
        }

        productRepository.save(product);

        List<ProductVariant> oldVariants = variantRepository.findByProductId(id);
        variantRepository.deleteAll(oldVariants);

        if (request.getVariants() != null) {
            createVariants(product, request.getVariants());
        }

        return mapToResponse(product);
    }
    // Hàm Helper để xử lý lưu biến thể
    private void createVariants(Product product, List<ProductRequest.VariantRequest> variantRequests) {
        for (ProductRequest.VariantRequest vReq : variantRequests) {
            ProductVariant variant = new ProductVariant();
            variant.setProduct(product);
            variant.setQuantity(vReq.getQuantity());
            variant.setColor(vReq.getColorId() != null ? colorRepository.findById(vReq.getColorId()).orElse(null) : null);
            variant.setSize(vReq.getSizeId() != null ? sizeRepository.findById(vReq.getSizeId()).orElse(null) : null);
            variant.setUnit(vReq.getUnitId() != null ? unitRepository.findById(vReq.getUnitId()).orElse(null) : null);
            ProductVariant savedVariant = variantRepository.save(variant);

            ProductPricing pricing = new ProductPricing();
            pricing.setProduct(product);
            pricing.setVariant(savedVariant);
            pricing.setBaseCostPrice(vReq.getCostPrice());
            pricing.setBaseRetailPrice(vReq.getSellPrice());
            pricingRepository.save(pricing);
        }
    }

    @Transactional
    public String delete(Long id) {
        Product product = productRepository.findById(id).orElseThrow();
        String name = product.getName();
        productRepository.delete(product);
        return "Đã xóa thành công sản phẩm: " + name;
    }

    private void exportToExcel(Product p, ProductRequest req) {
        try {
            List<String> headers = Arrays.asList("Mã SP", "Tên SP", "Số lượng biến thể", "Trạng thái");
            int variantCount = req.getVariants() != null ? req.getVariants().size() : 0;
            List<List<Object>> data = Arrays.asList(Arrays.asList(
                    p.getCode(), p.getName(), variantCount, p.getStatus().toString()
            ));
            excelService.exportToExcel("Product_Complex_Export.xlsx", headers, data);
        } catch (Exception e) { System.err.println("Lỗi Excel: " + e.getMessage()); }
    }

    private ProductResponse mapToResponse(Product product) {
        List<ProductVariant> variants = variantRepository.findByProductId(Long.valueOf(product.getId()));

        List<ProductResponse.VariantResponse> variantDtos = variants.stream().map(v -> {
            ProductPricing p = pricingRepository.findByVariantId(Long.valueOf(v.getId())).stream().findFirst().orElse(new ProductPricing());
            return ProductResponse.VariantResponse.builder()
                    .id(Long.valueOf(v.getId()))
                    .colorName(v.getColor() != null ? v.getColor().getName() : "")
                    .sizeName(v.getSize() != null ? v.getSize().getName() : "")
                    .unitName(v.getUnit() != null ? v.getUnit().getName() : "")
                    .costPrice(p.getBaseCostPrice())
                    .sellPrice(p.getBaseRetailPrice())
                    .quantity(v.getQuantity())
                    .build();
        }).collect(Collectors.toList());

        return ProductResponse.builder()
                .id(Long.valueOf(product.getId()))
                .name(product.getName())
                .code(product.getCode())
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : "")
                .supplierName(product.getSupplier() != null ? product.getSupplier().getName() : "")
                .imageUrls(product.getImageUrls())
                .description(product.getDescription())
                .status(product.getStatus())
                .variants(variantDtos)
                .build();
    }
}