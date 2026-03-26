package org.example.backend9.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductReportResponse {
    private Long productId;      // Khớp với CAST(pv.id AS long)
    private String productName;  // Khớp với p.name
    private Long quantitySold;   // 🟢 ĐỔI TỪ Integer SANG Long (Để khớp với SUM)
    private BigDecimal revenue;  // Khớp với SUM(unitPrice * quantity)
    private Integer stock;       // Khớp với CAST(pv.quantity AS int)
}