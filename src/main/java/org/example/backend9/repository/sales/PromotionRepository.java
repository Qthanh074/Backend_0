package org.example.backend9.repository.sales;

import org.example.backend9.entity.sales.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Integer> {
    boolean existsByCode(String code);
    Optional<Promotion> findByCode(String code);
}