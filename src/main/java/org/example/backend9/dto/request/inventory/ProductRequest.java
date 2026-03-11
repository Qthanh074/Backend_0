// src/main/java/org/example/backend9/dto/request/inventory/ProductRequest.java
package org.example.backend9.dto.request.inventory;

import lombok.Data;
import org.example.backend9.enums.EntityStatus;
import java.util.List;

@Data
public class ProductRequest {
    private String name;
    private String code;
    private Long categoryId;
    private Long supplierId;
    private List<String> imageUrls;
    private String description;
    private EntityStatus status;

    private List<VariantRequest> variants;

    @Data
    public static class VariantRequest {
        private Long colorId;
        private Long sizeId;
        private Long unitId;
        private Double costPrice;
        private Double sellPrice;
        private Integer quantity;
    }
}