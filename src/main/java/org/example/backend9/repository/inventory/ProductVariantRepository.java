package org.example.backend9.repository.inventory;

import org.example.backend9.entity.inventory.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    // Hàm này dùng để tìm tất cả các biến thể của một sản phẩm
    List<ProductVariant> findByProductId(Long productId);

    boolean existsBySku(String sku);
}