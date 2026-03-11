package org.example.backend9.dto.response.inventory;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceResponse {
    private Integer id;
    private String productCode;   // Mã Hàng (VD: SP001)
    private String variantName;   // Tên Hàng Hóa (VD: Nước mắm Nam Ngư 500ml)
    private String storeName;     // Cửa hàng / Chi nhánh
    private Double costPrice;     // Giá Vốn
    private Double retailPrice;   // Giá Bán Lẻ
    private Double wholesalePrice;// Giá Bán Buôn
    private String status;        // Trạng thái (Đang áp dụng, Chờ duyệt)
}