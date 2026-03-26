package org.example.backend9.repository.inventory;

import org.example.backend9.entity.inventory.ProductPricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductPricingRepository extends JpaRepository<ProductPricing, Integer> {

    Optional<ProductPricing> findByVariantIdAndStoreId(Long variantId, Integer storeId);

    // 🟢 Câu Query để tìm kiếm linh hoạt theo SKU, Tên sản phẩm hoặc Tên kho
    @Query("SELECT p FROM ProductPricing p " +
            "WHERE LOWER(p.variant.sku) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(p.product.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(p.store.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<ProductPricing> searchPricing(@Param("query") String query);
    // Tìm tất cả giá của một biến thể (trên mọi kho)
    List<ProductPricing> findByVariantId(Long variantId);
}