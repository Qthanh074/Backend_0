package org.example.backend9.entity.inventory;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend9.enums.EntityStatus;

@Entity
@Table(name = "sizes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Size {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name; // VD: S, M, L, XL, 39, 40, 41

    private String description; // VD: Phù hợp từ 50-60kg

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EntityStatus status = EntityStatus.ACTIVE;
}
