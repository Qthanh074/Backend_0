package org.example.backend9.dto.response.inventory;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ReturnTicketResponse {
    private Integer id;
    private String code;
    private String returnType;
    private String originalDocCode;
    private LocalDateTime returnDate;
    private String partnerName; // Tên khách hoặc NCC
    private String reason;
    private BigDecimal totalRefundAmount;
    private String paymentMethod;
    private String createdByName;
    private String status;
    private List<ReturnDetailResponse> details;
    private String storeName;
    @Data
    @Builder
    public static class ReturnDetailResponse {
        private Integer id;
        private String sku;
        private Long productVariantId;
        private String variantName;
        private Integer returnQuantity;
        private BigDecimal returnPrice;
        private BigDecimal total;
        private String conditionNote;
    }
}