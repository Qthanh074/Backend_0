package org.example.backend9.entity.sales;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "loyalty")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Loyalty {
    @Id
    private Integer id = 1; // Chỉ dùng 1 dòng duy nhất để lưu cấu hình hệ thống

    private BigDecimal exchangeRateEarn;   // VD: 100,000 (mua 100k được 1 điểm)
    private BigDecimal exchangeRateRedeem; // VD: 100 (1 điểm đổi được 100đ)
}