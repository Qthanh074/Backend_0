package org.example.backend9.dto.request.inventory;

import lombok.Data;
import org.example.backend9.enums.EntityStatus;
import java.util.List;

@Data
public class ProductRequest {
    private String name;
    private String code;
    private String barcode;
    private Long categoryId;
    private Long supplierId;
    private Long unitId;
    private List<String> imageUrls;
    private String description;
    private EntityStatus status;

    private List<VariantRequest> variants;

    @Data
    public static class VariantRequest {
        private String sku;
        private String variantName;
        private Long colorId;
        private Long sizeId;
        private Long unitId;
        private String barcode;
        private Double costPrice;
        private Double sellPrice;
        private Double wholesalePrice;
        private Integer quantity;
    }
}