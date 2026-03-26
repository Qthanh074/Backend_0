package org.example.backend9.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RevenueReportResponse {
    private String period;      // Ngày yyyy-MM-dd
    private BigDecimal revenue; // Doanh thu
    private BigDecimal profit;  // Lợi nhuận
    private Long orders;        // Số đơn hàng
}