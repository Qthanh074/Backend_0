package org.example.backend9.entity.sales;

import org.example.backend9.entity.core.Area;
import org.example.backend9.entity.core.Employee;
import org.example.backend9.enums.CustomerTier;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String code; // Mã KH (VD: KH001)

    @Column(nullable = false)
    private String fullName; // Tên KH hoặc Tên Tổ chức

    @Column(unique = true, nullable = false)
    private String phone;

    private String email;
    private String address;

    // Thống nhất dùng 1 trường duy nhất cho tổng chi tiêu để tích điểm
    @Column(precision = 19, scale = 2)
    private BigDecimal totalSpent = BigDecimal.ZERO;

    // Số điểm tích lũy hiện có
    private Integer currentPoints = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id")
    private Area area; // Thuộc khu vực nào

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CustomerTier tier = CustomerTier.BRONZE;

    private LocalDate lastVisit; // Lần cuối ghé mua

    private Boolean canPlaceOrder = true; // Trạng thái hoạt động

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee managedBy; // Nhân viên phụ trách
}