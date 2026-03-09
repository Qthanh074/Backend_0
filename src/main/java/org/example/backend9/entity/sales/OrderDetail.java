package org.example.backend9.entity.sales;


import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import org.example.backend9.entity.inventory.ProductVariant;

@Entity
@Table(name = "order_details")
@Data @NoArgsConstructor @AllArgsConstructor
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order; // Thuộc đơn hàng nào

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant; // Khách mua mặt hàng nào

    @Column(nullable = false)
    private Integer quantity; // Số lượng mua

    @Column(nullable = false)
    private BigDecimal unitPrice; // Đơn giá tại thời điểm mua (Không lấy giá hiện tại của SP vì giá có thể đổi)

    private BigDecimal discount = BigDecimal.ZERO; // Giảm giá riêng cho mặt hàng này (nếu có)

    @Column(nullable = false)
    private BigDecimal total; // Thành tiền (quantity * unitPrice - discount)
}