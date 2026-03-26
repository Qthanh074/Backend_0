package org.example.backend9.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StorePerformanceResponse {
    private Long storeId;
    private String storeName;
    private BigDecimal revenue;   // Tổng doanh thu
    private BigDecimal target;    // Mục tiêu (KPI)
    private Long orders;          // Tổng số đơn hàng
    private Long customers;       // Tổng số khách hàng (duy nhất)
}