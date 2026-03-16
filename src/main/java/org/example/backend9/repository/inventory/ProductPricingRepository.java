package org.example.backend9.repository.inventory;

import org.example.backend9.entity.inventory.ProductPricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductPricingRepository extends JpaRepository<ProductPricing, Integer> {

    // Đổi từ findByProductVariantId thành findByVariantId (khớp với field 'variant' trong Entity)
    List<ProductPricing> findByVariantId(Long variantId);

    // Thêm hàm này nếu b muốn lấy giá của biến thể tại 1 cửa hàng cụ thể
    List<ProductPricing> findByVariantIdAndStoreId(Long variantId, Integer storeId);
}