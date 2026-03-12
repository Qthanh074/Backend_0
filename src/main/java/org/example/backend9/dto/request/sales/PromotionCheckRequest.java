package org.example.backend9.dto.request.sales;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PromotionCheckRequest {
    private String code;         // Mã khách nhập (VD: KM10)
    private BigDecimal orderTotal; // Tổng tiền đơn hàng để check điều kiện tối thiểu
}