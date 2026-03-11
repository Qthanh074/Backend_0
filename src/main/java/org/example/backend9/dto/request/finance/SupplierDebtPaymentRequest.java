package org.example.backend9.dto.request.finance;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.backend9.enums.PaymentMethod;
import java.math.BigDecimal;

@Data
public class SupplierDebtPaymentRequest {
    @NotNull(message = "ID Nhà cung cấp không được trống")
    private Integer supplierId;

    @NotNull(message = "Số tiền trả không được trống")
    @DecimalMin(value = "0.01", message = "Số tiền trả phải lớn hơn 0")
    private BigDecimal amount;

    @NotNull(message = "Phương thức thanh toán không được trống")
    private PaymentMethod method; // CASH hoặc BANK_TRANSFER

    private String notes; // Diễn giải / Lý do

    @NotNull(message = "ID Cửa hàng không được trống")
    private Integer storeId;

    @NotNull(message = "ID Người lập phiếu không được trống")
    private Integer creatorId;
}