package org.example.backend9.dto.response.sales;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Integer id;
    private String orderNumber;
    private String orderType;
    private String paymentMethod;
    private String status;
    private BigDecimal subTotal;
    private BigDecimal discount;
    private BigDecimal totalAmount;
    private String customerName;
    private String customerPhone;
    private String employeeName;
    private String storeName;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
    private Integer earnedPoints;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {
        private String productName;
        private String variantName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
    }
}