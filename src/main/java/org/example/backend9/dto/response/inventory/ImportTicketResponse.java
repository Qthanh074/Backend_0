package org.example.backend9.dto.response.inventory;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ImportTicketResponse {
    private Integer id;
    private String code;
    private LocalDateTime importDate;
    private String supplierName;
    private String createdByName;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal debtAmount;
    private String status;
    private List<ImportTicketDetailResponse> details;

    @Data
    @Builder
    public static class ImportTicketDetailResponse {
        private Integer id;
        private Integer productVariantId;
        private String variantName;
        private String sku;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalValue;
    }
}