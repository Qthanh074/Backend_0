package org.example.backend9.dto.request.inventory;

import lombok.Data;

@Data
public class ProductPricingRequest {
    private Long productId;        // ID sản phẩm gốc
    private Long variantId;       // ID biến thể cụ thể (product_variant_id)
    private Integer storeId;       // ID cửa hàng áp dụng giá này

    private Double baseCostPrice;    // Giá vốn
    private Double baseRetailPrice;  // Giá bán lẻ
    private Double wholesalePrice;   // Giá bán buôn

    private String status;           // Trạng thái giá (VD: ACTIVE, HIDDEN)
}