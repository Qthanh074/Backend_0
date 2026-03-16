package org.example.backend9.dto.response.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPricingResponse {
    private Integer id;

    // Thông tin Sản phẩm gốc
    private Long productId;
    private String productName;

    // Thông tin Biến thể cụ thể
    private Long variantId;
    private String sku;
    private String variantName;

    // Thông tin Cửa hàng (Nếu hệ thống có nhiều chi nhánh)
    private Integer storeId;
    private String storeName;

    // Các loại giá (Dùng Double để đồng bộ với Request)
    private Double baseCostPrice;
    private Double baseRetailPrice;
    private Double wholesalePrice;

    private String status;
}