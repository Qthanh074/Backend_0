package org.example.backend9.dto.response.inventory;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class InventoryCheckResponse {
    private Integer id;
    private String code;
    private LocalDateTime checkDate;
    private String storeName;
    private String checkerName;
    private Integer totalDiscrepancyQty;
    private BigDecimal totalDiscrepancyValue;
    private String status;
    private List<InventoryCheckDetailResponse> details;

    @Data
    @Builder
    public static class InventoryCheckDetailResponse {
        private Integer id;
        private String sku;
        private Long productVariantId;
        private String variantName;
        private Integer systemQuantity;
        private Integer actualQuantity;
        private Integer discrepancy;
        private BigDecimal unitCost;
        private BigDecimal discrepancyValue;
    }
}