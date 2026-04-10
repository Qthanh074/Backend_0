package org.example.backend9.dto.response.core;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class SupplierProductResponse {
    private Integer variantId;
    private String sku;
    private String variantName;
    private BigDecimal costPrice; // Chính là importPrice trong DB
    private Integer quantity; // Tồn kho hiện tại để xem tham khảo
}