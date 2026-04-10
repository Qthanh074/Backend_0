package org.example.backend9.entity.sales;

import org.example.backend9.enums.DiscountType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "promotions")
@Data @NoArgsConstructor @AllArgsConstructor
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private DiscountType discountType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal discountValue;

    @Column(precision = 19, scale = 2)
    private BigDecimal minPurchase = BigDecimal.ZERO; // Mặc định là 0

    @Column(precision = 19, scale = 2)
    private BigDecimal maxDiscount; // Để null đại diện cho "Không giới hạn"

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    private Boolean isActive = true;

    private String applyFor = "ALL"; // Mặc định áp dụng cho tất cả
}