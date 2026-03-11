package org.example.backend9.entity.core;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend9.enums.EntityStatus;

@Entity
@Table(name = "employees")
@Data @NoArgsConstructor @AllArgsConstructor
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true)
    private String code; // e.g., EMP001

    @Column(nullable = false)
    private String fullName;

    @Column(unique = true, nullable = false)
    private String email; // Used as login username

    private String phone;

    @Column(nullable = false)
    private String passwordHash; // Dùng để lưu mật khẩu đã mã hóa

    @Column(name = "verification_token")
    private String verificationToken; // Mã xác thực email

    private String role; // Vd: ADMIN, MANAGER, CASHIER

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EntityStatus status = EntityStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;
}