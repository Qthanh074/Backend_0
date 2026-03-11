package org.example.backend9.dto.request.finance;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.backend9.enums.PaymentMethod;
import org.example.backend9.enums.TransactionType;

import java.math.BigDecimal;

@Data
public class CashbookTransactionRequest {

    @NotNull(message = "Loại giao dịch (Thu/Chi) không được để trống")
    private TransactionType type; // Truyền lên "INCOME" hoặc "EXPENSE"

    @NotNull(message = "Phương thức thanh toán không được để trống")
    private PaymentMethod method; // Truyền lên "CASH", "BANK_TRANSFER", hoặc "CARD"

    @NotBlank(message = "Nhóm giao dịch không được để trống")
    private String category; // VD: "Bán hàng", "Nhập hàng", "Chi phí vận hành"...

    private String description; // Nội dung diễn giải chi tiết

    @NotBlank(message = "Đối tượng giao dịch không được để trống")
    private String referenceName; // Tên người nộp hoặc người nhận tiền

    @NotNull(message = "Số tiền không được để trống")
    @DecimalMin(value = "0.01", message = "Số tiền phải lớn hơn 0")
    private BigDecimal amount; // Số tiền giao dịch

    @NotNull(message = "ID Cửa hàng không được để trống")
    private Integer storeId; // ID của cửa hàng (Store)

    @NotNull(message = "ID Người lập phiếu không được để trống")
    private Integer creatorId; // ID của nhân viên (Employee) tạo giao dịch
}