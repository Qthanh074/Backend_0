package org.example.backend9.dto.request.inventory;

import lombok.Data;
import java.util.List;

@Data
public class TransferTicketRequest {
    private Integer fromStoreId;
    private Integer toStoreId;
    private Integer createdById;
    private List<TransferTicketDetailRequest> details;

    @Data
    public static class TransferTicketDetailRequest {
        private Long productVariantId;
        private Integer quantity;
    }
}