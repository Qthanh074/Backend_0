package org.example.backend9.dto.request.core;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SupplierProductRequest {
    private Integer variantId;
    private BigDecimal importPrice;
}