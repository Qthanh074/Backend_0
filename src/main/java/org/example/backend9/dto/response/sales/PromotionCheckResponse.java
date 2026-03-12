package org.example.backend9.dto.response.sales;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class PromotionCheckResponse {
    private boolean valid;       // Hợp lệ hay không
    private String message;      // Thông báo (Lý do lỗi hoặc "Thành công")
    private BigDecimal discountAmount; // Số tiền được giảm thực tế
}