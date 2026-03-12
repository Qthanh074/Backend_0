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
    private String code; // Mã KM (VD: GIAM10)

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false, columnDefinition = "VARCHAR(20)") // Thêm cái này
    private DiscountType discountType;

    @Column(nullable = false)
    private BigDecimal discountValue; // Giá trị (VD: 10 hoặc 50000)

    private BigDecimal minPurchase; // Đơn tối thiểu
    private BigDecimal maxDiscount; // Giảm tối đa (Cho loại %)

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    private Boolean isActive = true;
}
