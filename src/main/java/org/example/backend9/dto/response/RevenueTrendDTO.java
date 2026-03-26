package org.example.backend9.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * DTO đại diện cho các điểm dữ liệu (Data points) trên biểu đồ chuỗi thời gian
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueTrendDTO {
    private String period; // Định dạng "YYYY-MM" hoặc "YYYY-MM-DD"
    private Long orderCount;
    private BigDecimal revenue;
}