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
    private String categoryName;
    private String supplierName;
    private List<String> imageUrls;
    private String description;
    private EntityStatus status;
    private List<VariantResponse> variants;

    @Data
    @Builder
    public static class VariantResponse {
        private Long id;
        private String colorName;
        private String sizeName;
        private String unitName;
        private Double costPrice;
        private Double sellPrice;
        private Integer quantity;
    }
}