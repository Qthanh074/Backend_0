package org.example.backend9.entity.inventory;


import org.example.backend9.entity.core.Supplier;
import org.example.backend9.enums.EntityStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String code; // Mã sản phẩm chung (VD: SP001)

    @Column(nullable = false)
    private String name; // Tên sản phẩm (VD: Áo thun nam Polo)

    private String description;
    private String imageUrl;

    // Liên kết với Nhà Cung Cấp
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    // Liên kết với Danh mục
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    // Liên kết với Đơn vị tính (Cái, Chiếc, Hộp...)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    // Giá cơ bản (Áp dụng làm gốc nếu các biến thể không có giá riêng)
    private BigDecimal baseCostPrice;      // Giá vốn gốc
    private BigDecimal baseRetailPrice;    // Giá bán lẻ gốc
    private BigDecimal baseWholesalePrice; // Giá bán buôn gốc

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EntityStatus status = EntityStatus.ACTIVE;

    // LIÊN KẾT BIẾN THỂ (1 Sản phẩm -> Nhiều Biến thể)
    // cascade = CascadeType.ALL: Lưu/Xóa Product sẽ tự động Lưu/Xóa các Variant bên trong
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariant> variants = new ArrayList<>();
}