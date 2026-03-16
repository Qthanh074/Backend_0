package org.example.backend9.dto.response.inventory;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ExportTicketResponse {
    private Integer id;
    private String code;
    private LocalDateTime exportDate;
    private String reason;
    private String customerName;
    private String createdByName;
    private BigDecimal totalValue;
    private String status;
    private List<ExportTicketDetailResponse> details;

    @Data
    @Builder
    public static class ExportTicketDetailResponse {
        private Integer id;
        private Integer productVariantId;
        private String variantName;
        private String sku;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalValue;
    }
}