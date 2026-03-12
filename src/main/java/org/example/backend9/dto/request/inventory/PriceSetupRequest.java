package org.example.backend9.dto.request.inventory;

import lombok.Data;

@Data
public class PriceSetupRequest {
    private Integer variantId; // Thiết lập giá cho biến thể nào
    private Integer storeId;   // Tại cửa hàng nào (Null = Tất cả chi nhánh)
    private Double costPrice;
    private Double retailPrice;
    private Double wholesalePrice;
    private String status;     // Đang áp dụng, Chờ duyệt
}