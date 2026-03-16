package org.example.backend9.dto.response.inventory;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TransferTicketResponse {
    private Integer id;
    private String code;
    private LocalDateTime transferDate;
    private String fromStoreName;
    private String toStoreName;
    private String createdByName;
    private Integer totalQuantity;
    private String status;
    private List<TransferTicketDetailResponse> details;

    @Data
    @Builder
    public static class TransferTicketDetailResponse {
        private Integer id;
        private Long productVariantId;
        private String sku;
        private String variantName;
        private Integer quantity;
    }
}