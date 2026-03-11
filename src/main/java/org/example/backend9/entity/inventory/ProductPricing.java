package org.example.backend9.entity.inventory;

import org.example.backend9.entity.core.Store;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_pricings")
@Data // Tự động tạo getter/setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductPricing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product; // Thêm trường này để dễ quản lý theo sản phẩm gốc

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant variant; // Đổi tên thành 'variant' cho giống Service b đang gọi

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    // Chuyển sang Double cho đồng bộ với ProductRequest b đã gửi
    private Double baseCostPrice;
    private Double baseRetailPrice;

    private Double wholesalePrice;

    @Column(length = 50)
    private String status;
}