package org.example.backend9.entity.inventory;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend9.enums.EntityStatus;

@Entity
@Table(name = "colors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Color {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name; // VD: Đỏ, Đen, Trắng

    // Tùy chọn: Có thể lưu mã màu Hex để Frontend hiển thị ô màu trực quan
    @Column(length = 20)
    private String hexCode; // VD: #FF0000, #000000

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EntityStatus status = EntityStatus.ACTIVE;
}
