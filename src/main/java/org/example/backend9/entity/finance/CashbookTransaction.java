package org.example.backend9.entity.finance;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.example.backend9.entity.core.Store;
import org.example.backend9.entity.core.Employee;
import org.example.backend9.enums.TransactionType;
import org.example.backend9.enums.PaymentMethod;
import org.example.backend9.enums.TicketStatus;

@Entity
@Table(name = "cashbook_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CashbookTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String code; // Mã phiếu: VD: PT260301 (Phiếu thu), PC260301 (Phiếu chi)

    @Column(nullable = false)
    private LocalDateTime transactionDate; // Thời gian giao dịch

    // --- PHÂN LOẠI GIAO DỊCH ---
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private TransactionType type; // Thu (INCOME) hay Chi (EXPENSE)

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private PaymentMethod method; // Tiền mặt (CASH) hay Ngân hàng (BANK_TRANSFER)

    private String category; // Nhóm giao dịch (VD: Bán hàng, Nhập hàng, Chi phí vận hành)

    @Column(columnDefinition = "TEXT")
    private String description; // Diễn giải / Lý do

    // --- ĐỐI TƯỢNG GIAO DỊCH ---
    // Khách hàng nộp, NCC nhận, hoặc nhân viên tạp vụ... Lưu dạng chuỗi cho linh hoạt.
    // Nếu muốn liên kết cứng thì có thể dùng Customer/Supplier ID, nhưng Sổ quỹ nên để tự do.
    private String referenceName;

    // --- GIÁ TRỊ GIAO DỊCH ---
    @Column(nullable = false)
    private BigDecimal amount; // Số tiền (Tuyệt đối không lưu âm, thu/chi do cột `type` quyết định)

    // Tồn quỹ tại thời điểm giao dịch này xảy ra (Balance).
    // Giúp kế toán xem lại lịch sử tăng/giảm giống như sao kê ngân hàng.
    private BigDecimal balanceAfterTransaction = BigDecimal.ZERO;

    // --- THÔNG TIN HỆ THỐNG ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store; // Giao dịch thuộc cửa hàng nào

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee creator; // Người lập phiếu

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TicketStatus status = TicketStatus.COMPLETED; // Trạng thái phiếu
}
