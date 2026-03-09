package org.example.backend9.entity.inventory;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.example.backend9.entity.core.Store;
import org.example.backend9.entity.core.Employee;

@Entity
@Table(name = "inventory_checks")
@Data @NoArgsConstructor @AllArgsConstructor
public class InventoryCheck {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String code; // Mã Phiếu (PK260301)

    private LocalDateTime checkDate; // Ngày Kiểm

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store; // Cửa hàng / Kho

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee checker; // Người Kiểm

    private Integer totalDiscrepancyQty; // Tổng SL Lệch
    private BigDecimal totalDiscrepancyValue; // Tổng Giá Trị Lệch

    @Column(length = 50)
    private String status; // Đang kiểm, Đã cân bằng
}
