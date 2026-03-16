package org.example.backend9.dto.response.inventory;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class VariantDetailResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productCode;
    private String sku;
    private String barcode;
    private String variantName;
    private Integer quantity;
    private Integer colorId;
    private String colorName;
    private Integer sizeId;
    private String sizeName;
    private Long unitId;
    private String unitName;

    // Mới bổ sung
    private String status;
    private BigDecimal extraCost;
    private BigDecimal extraPrice;

    private Double costPrice;
    private Double sellPrice;
    private Double wholesalePrice;
}