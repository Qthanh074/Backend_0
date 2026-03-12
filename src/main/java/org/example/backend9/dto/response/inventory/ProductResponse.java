package org.example.backend9.dto.response.inventory;

import lombok.*;
import org.example.backend9.enums.EntityStatus;
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
    private String unitName;
    private List<String> imageUrls;
    private String description;
    private EntityStatus status;
    private List<VariantResponse> variants;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariantResponse {
        private Long id;
        private String barcode;
        private String colorName;
        private String sizeName;
        private String unitName;
        private Double costPrice;
        private Double sellPrice;
        private Double wholesalePrice;
        private Integer quantity;
    }
}