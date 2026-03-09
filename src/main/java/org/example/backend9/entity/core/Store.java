package org.example.backend9.entity.core;


import jakarta.persistence.*;
import lombok.*;
import org.example.backend9.enums.EntityStatus;
@Entity
@Table(name = "stores")
@Data @NoArgsConstructor @AllArgsConstructor
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String code;

    private String name;
    private String address;
    private String phone;
    private String email;
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EntityStatus status = EntityStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id")
    private Area area;
}