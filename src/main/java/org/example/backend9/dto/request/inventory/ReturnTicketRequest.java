package org.example.backend9.dto.request.inventory;

import lombok.Data;
import org.example.backend9.enums.PaymentMethod;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ReturnTicketRequest {
    private String returnType; // CUSTOMER_RETURN hoặc SUPPLIER_RETURN
    private String originalDocCode;
    private Integer customerId; // Dùng cho khách trả
    private Integer supplierId; // Dùng cho trả NCC
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