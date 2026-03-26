package org.example.backend9.repository.inventory;

import org.example.backend9.entity.inventory.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT COUNT(p) FROM Product p")
    Long countTotalProducts();

    @Query("SELECT COUNT(p) FROM Product p WHERE (SELECT SUM(pv.quantity) FROM ProductVariant pv WHERE pv.product = p) < :threshold")
    Long countLowStock(@Param("threshold") long threshold);
}