package org.example.backend9.dto.response.finance;

import lombok.Data;
import org.example.backend9.enums.PaymentMethod;
import org.example.backend9.enums.TicketStatus;
import org.example.backend9.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CashbookTransactionResponse {
    private Integer id;

    private String code; // Mã phiếu (VD: PT260301, PC260302)

    private LocalDateTime transactionDate; // Ngày giờ tạo phiếu

    private TransactionType type; // INCOME (Thu) hoặc EXPENSE (Chi)

    private PaymentMethod method; // CASH (Tiền mặt) hoặc BANK_TRANSFER (Chuyển khoản)

    private String category; // Phân loại (Bán hàng, Trả nợ NCC...)

    private String description; // Ghi chú / Diễn giải

    private String referenceName; // Người nộp / Người nhận (Tên NCC, Tên Khách...)

    private BigDecimal amount; // Số tiền giao dịch

    private BigDecimal balanceAfterTransaction; // Tồn quỹ sau khi giao dịch (Rất quan trọng cho UI Sổ quỹ)

    private String storeName; // Tên cửa hàng

    private String creatorName; // Tên nhân viên lập phiếu

    private TicketStatus status; // Trạng thái phiếu (COMPLETED, CANCELLED)
}