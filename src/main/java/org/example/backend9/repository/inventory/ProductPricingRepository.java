package org.example.backend9.repository.inventory;

import org.example.backend9.entity.inventory.ProductPricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductPricingRepository extends JpaRepository<ProductPricing, Long> {

    // Tìm giá theo Product ID
    List<ProductPricing> findByProductId(Long productId);

    // Tìm giá theo Variant ID (Dùng cho cấu trúc đa biến thể b vừa quay lại)
    List<ProductPricing> findByVariantId(Long variantId);

    // Nó sẽ tìm xem Biến thể X tại Cửa hàng Y đã có giá chưa
    Optional<ProductPricing> findByVariantIdAndStoreId(Integer variantId, Integer storeId);
}