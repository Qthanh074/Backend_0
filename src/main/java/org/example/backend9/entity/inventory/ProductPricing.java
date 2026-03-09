package org.example.backend9.entity.inventory;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import org.example.backend9.entity.core.Store;
import org.example.backend9.entity.inventory.ProductVariant;
@Entity
@Table(name = "product_pricings")
@Data @NoArgsConstructor @AllArgsConstructor
public class ProductPricing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant; // Ánh xạ Mã Hàng, Tên Hàng

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store; // Cửa Hàng / Chi Nhánh (Null = Tất cả chi nhánh)

    private BigDecimal costPrice; // Giá Vốn
    private BigDecimal retailPrice; // Giá Bán Lẻ
    private BigDecimal wholesalePrice; // Giá Bán Buôn

    @Column(length = 50)
    private String status; // Đang áp dụng, Chờ duyệt
}
