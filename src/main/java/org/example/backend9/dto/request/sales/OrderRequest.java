package org.example.backend9.dto.request.sales;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderRequest {
    private String orderType;       // VD: RETAIL (Tại quầy) hoặc ONLINE
    private Integer customerId;     // Null nếu là khách lẻ không lưu thông tin
    private Integer promotionId;    // ID chương trình khuyến mãi (nếu có)
    private String paymentMethod;   // VD: CASH, BANK_TRANSFER
    private BigDecimal shippingFee; // Phí giao hàng
    private Integer storeId;        // ID cửa hàng thực hiện đơn
    private BigDecimal discount;    // Số tiền giảm giá thủ công (nhân viên nhập thêm)

    // 🟢 TIỀN KHÁCH ĐƯA: Để tính tiền thừa và sinh Phiếu Chi tự động
    private BigDecimal amountPaid;

    // 🟢 SỐ ĐIỂM TIÊU DÙNG: Số điểm khách muốn dùng để trừ tiền (1 điểm = 100đ)
    private Integer usedPoints;

    private List<ItemRequest> items;

    @Data
    public static class ItemRequest {
        private Integer productVariantId; // ID của biến thể sản phẩm
        private Integer quantity;         // Số lượng mua
    }
}