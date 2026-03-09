package org.example.backend9.entity;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import org.example.backend9.entity.core.Store;
import org.example.backend9.entity.inventory.ProductVariant;

@Entity
@Table(name = "demand_predictions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandPrediction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    // Chu kỳ dự đoán (VD: "7_DAYS", "30_DAYS", "NEXT_MONTH")
    @Column(length = 50, nullable = false)
    private String period;

    // Nhu cầu mà AI dự đoán được (Sẽ bán được bao nhiêu cái)
    @Column(nullable = false)
    private Integer predictedDemand;

    // Số lượng AI khuyên nên nhập thêm về kho (Dựa trên tồn kho hiện tại)
    private Integer recommendedOrderQuantity;

    // Độ tin cậy của thuật toán AI (Tính theo %, VD: 85%)
    private Integer confidenceLevel;

    @Column(nullable = false, updatable = false)
    private LocalDateTime predictedAt = LocalDateTime.now();
}