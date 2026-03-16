package org.example.backend9.entity.core;

import org.example.backend9.enums.EntityStatus;
import jakarta.persistence.*;
import lombok.*;
import org.example.backend9.entity.core.Store;

import java.util.List;

@Entity
@Table(name = "areas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Area {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String code; // e.g., NORTH, SOUTH, HCM

    @Column(nullable = false)
    private String name;

    private String description;
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EntityStatus status = EntityStatus.ACTIVE;

    @OneToMany(mappedBy = "area", fetch = FetchType.LAZY)
    private List<Store> stores;
}