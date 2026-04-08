package org.example.backend9.dto.response.inventory;

import lombok.*;
import org.example.backend9.enums.EntityStatus;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String code;
    private String barcode;
    private Long categoryId;
    private String categoryName;
    private Long supplierId;
    private String supplierName;
    private Long unitId;
    private String unitName;
    private List<String> imageUrls;
    private String description;
    private EntityStatus status;

    private BigDecimal baseCostPrice;
    private BigDecimal baseRetailPrice;
    private BigDecimal baseWholesalePrice;

    private List<VariantResponse> variants;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariantResponse {
        private Long id;
        private String sku;
        private String variantName;
        private String barcode;
        private String colorName;
        private String sizeName;
        private String unitName;
        private Integer colorId;
        private Integer sizeId;

        private Double costPrice;
        private Double sellPrice;
        private Double wholesalePrice;
        private Integer quantity;

        private String status;
        private BigDecimal extraCost;
        private BigDecimal extraPrice;
    }
}