package org.example.backend9.dto.response.sales;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private String orderNumber;     // Mã hóa đơn (VD: HD-1736283912)
    private BigDecimal totalAmount; // Tổng tiền khách phải trả (Đã cộng ship, trừ km)
    private BigDecimal discountAmount; // Số tiền được giảm
    private Integer earnedPoints;   // Điểm tích lũy được từ đơn này
    private String status;          // Trạng thái đơn (VD: COMPLETED)
}