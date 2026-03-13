package org.example.backend9.dto.request.inventory;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class VariantRequest {
    private Long productId;
    private String sku;
    private String barcode;
    private Integer quantity;
    private Integer colorId;
    private Integer sizeId;
    private Long unitId;

    // Thuộc tính riêng của Biến thể (ProductVariant)
    private String status; // Truyền String, xuống Service ép kiểu thành Enum
    private BigDecimal extraCost;
    private BigDecimal extraPrice;

    // Thuộc tính của bảng Giá (ProductPricing)
    private Double costPrice;
    private Double sellPrice;
    private Double wholesalePrice;
}