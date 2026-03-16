package org.example.backend9.dto.request.inventory;

import lombok.Data;
import org.example.backend9.enums.EntityStatus;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductRequest {
    private String name;
    private String code;
    private String barcode;
    private Long categoryId;
    private Long supplierId;
    private Long unitId; // Bắt buộc (nullable = false trong Entity)
    private List<String> imageUrls;
    private String description;
    private EntityStatus status;

    // --- 3 TRƯỜNG GIÁ GỐC (Mới cập nhật để khớp với Entity Product) ---
    private BigDecimal baseCostPrice;      // Giá vốn gốc
    private BigDecimal baseRetailPrice;    // Giá bán lẻ gốc
    private BigDecimal baseWholesalePrice; // Giá bán buôn gốc

    // Danh sách biến thể đi kèm khi tạo mới sản phẩm
    private List<VariantRequest> variants;

    @Data
    public static class VariantRequest {
        private String sku;
        private String variantName;
        private Integer colorId;
        private Integer sizeId;
        private Long unitId;
        private String barcode;

        // Giá bán lẻ, nhập, buôn cụ thể cho từng biến thể
        private Double costPrice;
        private Double sellPrice;
        private Double wholesalePrice;
        private Integer quantity;

        // --- 3 TRƯỜNG MỚI CỦA BIẾN THỂ (Để đồng bộ với Entity Variant) ---
        private String status;        // ACTIVE, INACTIVE...
        private BigDecimal extraCost;  // Phụ phí nhập
        private BigDecimal extraPrice; // Phụ phí bán
    }
}