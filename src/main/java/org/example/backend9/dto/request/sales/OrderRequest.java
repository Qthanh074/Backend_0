package org.example.backend9.dto.request.sales;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderRequest {
    private String orderType;       // VD: RETAIL (Tại quầy) hoặc ONLINE
    private Integer customerId;     // Null nếu là khách lẻ không lưu thông tin
    private Integer promotionId;    // Dùng cho logic giảm giá sau này
    private String paymentMethod;   // VD: CASH, BANK_TRANSFER, MOMO
    private BigDecimal shippingFee; // Phí giao hàng (nếu có)

    // 🟢 SỬA TẠI ĐÂY: Thêm storeId để hứng dữ liệu từ Frontend
    private Integer storeId;

    // Danh sách sản phẩm khách mua
    private List<ItemRequest> items;
    private BigDecimal discount;

    @Data
    public static class ItemRequest {
        private Integer productVariantId; // ID của biến thể sản phẩm (Integer)
        private Integer quantity;         // Số lượng mua
    }
}