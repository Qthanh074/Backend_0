package org.example.backend9.dto.request.sales;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderRequest {
    private String orderType; // RETAIL hoặc ONLINE
    private Integer customerId; // Có thể null nếu là khách lẻ
    private Integer promotionId;
    private List<ItemRequest> items;
    private String paymentMethod;

    // Thuộc tính cho Online
    private String salesChannel;
    private BigDecimal shippingFee;

    public interface OrderItemRequest {
    }

    @Data
    public static class ItemRequest {
        private Integer productVariantId;
        private Integer quantity;
    }
}