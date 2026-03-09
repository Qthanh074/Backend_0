package org.example.backend9.entity.inventory;


import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.example.backend9.entity.core.Employee;
import org.example.backend9.entity.core.Supplier;
import org.example.backend9.entity.sales.Customer;
import org.example.backend9.enums.TicketStatus;
import org.example.backend9.enums.PaymentMethod;

@Entity
@Table(name = "return_tickets")
@Data @NoArgsConstructor @AllArgsConstructor
public class ReturnTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String code; // Mã Phiếu (TL... hoặc TH...)

    // Quan trọng: Phân biệt đây là phiếu Trả NCC hay Khách Trả
    @Column(length = 20, nullable = false)
    private String returnType; // CUSTOMER_RETURN hoặc SUPPLIER_RETURN

    private String originalDocCode; // Hóa đơn gốc (HD260301)
    private LocalDateTime returnDate; // Ngày Trả / Ngày Nhận

    // Dùng 1 trong 2 tùy vào returnType
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    private String reason; // Lý Do Trả
    private BigDecimal totalRefundAmount; // Tổng Hoàn Tiền / Tổng giá trị

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentMethod paymentMethod; // Phương thức bù trừ / Hình thức hoàn

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee createdBy; // Người Nhận / Người Lập

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TicketStatus status;
}
