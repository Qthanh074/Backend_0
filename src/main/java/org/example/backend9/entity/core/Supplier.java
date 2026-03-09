package org.example.backend9.entity.core;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend9.enums.EntityStatus;

@Entity
@Table(name = "suppliers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false, length = 20)
    private String code; // NCC001

    @Column(nullable = false)
    private String name; // Tên nhà cung cấp

    private String contactPerson; // Người liên hệ

    private String phone;

    private String email;

    private String address;

    @Column(columnDefinition = "DECIMAL(15,2)")
    private Double debt = 0.0; // Công nợ

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EntityStatus status = EntityStatus.ACTIVE;
}
