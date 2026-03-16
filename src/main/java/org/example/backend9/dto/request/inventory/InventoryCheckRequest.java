package org.example.backend9.dto.request.inventory;

import lombok.Data;
import java.util.List;

@Data
public class InventoryCheckRequest {
    private Integer storeId;
    private Integer checkerId;
    private List<InventoryCheckDetailRequest> details;

    @Data
    public static class InventoryCheckDetailRequest {
        private Long productVariantId;
        private Integer actualQuantity; // Số lượng thực tế đếm được
    }
}