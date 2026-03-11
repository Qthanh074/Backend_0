package org.example.backend9.entity.inventory;

import org.example.backend9.enums.EntityStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "product_variants")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Liên kết ngược lại với Sản phẩm gốc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(unique = true, nullable = false)
    private String sku; // Mã SKU của riêng biến thể (VD: SP001-RED-M)

    @Column(unique = true)
    private String barcode; // Mã vạch riêng biệt (Để máy POS quét)

    private String variantName; // Tên hiển thị cụ thể (VD: Màu Đỏ - Size M)

    // LƯU TRỮ LINH HOẠT VỚI JSON:
    // VD: {"color": "Đỏ", "size": "M", "material": "Cotton"}
    // Cách này giúp bạn không phải tạo ra hàng chục cột thừa thãi trong Database.
    @Column(columnDefinition = "JSON")
    private String attributes;

    // GIÁ CHÊNH LỆCH SO VỚI GIÁ GỐC:
    // VD: Áo size XXL tốn vải hơn nên giá nhập (extraCost) đắt hơn 10k, giá bán (extraPrice) đắt hơn 20k.
    // Nếu không chênh lệch thì để mặc định là 0.
    private BigDecimal extraCost = BigDecimal.ZERO;
    private BigDecimal extraPrice = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EntityStatus status = EntityStatus.ACTIVE;
}