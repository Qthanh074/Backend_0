package org.example.backend9.dto.request.inventory;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ImportTicketRequest {
    private Integer supplierId;
    private Integer createdById;
    private BigDecimal paidAmount; // Số tiền đã trả cho NCC ngay lúc nhập

    private List<ImportTicketDetailRequest> details;

    @Data
    public static class ImportTicketDetailRequest {
        private Integer productVariantId;
        private Integer quantity;
        private BigDecimal unitPrice; // Đơn giá nhập
    }
}