package org.example.backend9.entity.sales;


import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.example.backend9.entity.core.Area;
import org.example.backend9.entity.core.Employee;
import org.example.backend9.enums.CustomerTier;

@Entity
@Table(name = "customers")
@Data @NoArgsConstructor @AllArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String code; // Mã KH (KH001)

    @Column(nullable = false)
    private String fullName; // Tên KH hoặc Tên Tổ chức

    @Column(unique = true, nullable = false)
    private String phone;

    private String email;
    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id")
    private Area area; // Thuộc khu vực nào

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CustomerTier tier = CustomerTier.BRONZE; // Hạng thẻ (Bạc, Vàng...)

    // LIÊN QUAN TỚI TÍCH ĐIỂM (Loyalty)
    private Integer currentPoints = 0; // Điểm tích lũy hiện tại
    private BigDecimal totalSpent = BigDecimal.ZERO; // Tổng chi tiêu
    private LocalDate lastVisit; // Lần cuối ghé mua

    private Boolean canPlaceOrder = true; // Có thể đặt hàng không

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee managedBy; // Nhân viên phụ trách (nếu có)
}
