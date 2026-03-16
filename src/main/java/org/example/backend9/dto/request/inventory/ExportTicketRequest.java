package org.example.backend9.dto.request.inventory;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ExportTicketRequest {
    private String reason;
    private String customerName;
    private Integer createdById;

    private List<ExportTicketDetailRequest> details;

    @Data
    public static class ExportTicketDetailRequest {
        private Integer productVariantId;
        private Integer quantity;
        private BigDecimal unitPrice;
    }
}