package org.example.backend9.repository.sales;

import org.example.backend9.entity.sales.Loyalty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoyaltyRepository extends JpaRepository<Loyalty, Integer> {
    // Vì hệ thống chỉ có 1 bản cấu hình duy nhất, ta luôn lấy ID = 1
    default Optional<Loyalty> findSystemConfig() {
        return findById(1);
    }
}