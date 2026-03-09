package org.example.backend9.entity.inventory;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import org.example.backend9.entity.inventory.ProductVariant;

@Entity
@Table(name = "inventory_check_details")
@Data @NoArgsConstructor @AllArgsConstructor
public class InventoryCheckDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_check_id", nullable = false)
    private InventoryCheck inventoryCheck;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @Column(nullable = false)
    private Integer systemQuantity; // Tồn kho trên phần mềm lúc bắt đầu kiểm

    @Column(nullable = false)
    private Integer actualQuantity; // Số lượng đếm thực tế bằng tay

    // discrepancy = actualQuantity - systemQuantity (Dương = thừa, Âm = thiếu)
    @Column(nullable = false)
    private Integer discrepancy;

    private BigDecimal unitCost; // Giá vốn tại thời điểm kiểm để quy ra tiền chênh lệch
}