package org.example.backend9.dto.request.inventory;

import lombok.Data;
import org.example.backend9.enums.PaymentMethod;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ReturnTicketRequest {
    private String returnType;
    private String originalDocCode;
    private Integer customerId;
    private Integer supplierId;
    private Integer storeId;
    private String reason;
    private PaymentMethod paymentMethod;
    private Integer createdById;

    private List<ReturnDetailRequest> details;

    @Data
    public static class ReturnDetailRequest {
        private Long productVariantId;
        private Integer returnQuantity;
        private BigDecimal returnPrice;
        private String conditionNote;
    }
}