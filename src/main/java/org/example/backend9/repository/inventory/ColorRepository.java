package org.example.backend9.repository.inventory;

import org.example.backend9.entity.inventory.Color;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ColorRepository extends JpaRepository<Color, Long> {

    // Hàm này dùng để kiểm tra xem tên màu đã tồn tại trong DB chưa
    boolean existsByName(String name);
}