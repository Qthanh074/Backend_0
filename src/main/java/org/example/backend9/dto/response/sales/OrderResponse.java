package org.example.backend9.dto.response.sales;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderResponse {
    private String orderNumber;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private Integer earnedPoints;
    private String status;
}