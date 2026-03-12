package org.example.backend9.entity.inventory;


import jakarta.persistence.*;
import lombok.*;
import org.example.backend9.enums.EntityStatus;

@Entity
@Table(name = "units")
@Data @NoArgsConstructor @AllArgsConstructor
public class Unit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String name; // e.g., Piece, Box, Can

    private String description;

    private Boolean isBaseUnit; // true = smallest unit for conversion

    @Enumerated(EnumType.STRING)
    private EntityStatus status;
}