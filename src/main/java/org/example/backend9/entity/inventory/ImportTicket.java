package org.example.backend9.entity.inventory;


import org.example.backend9.entity.core.Employee;
import org.example.backend9.entity.core.Supplier;
import org.example.backend9.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "import_tickets")
@Data @NoArgsConstructor @AllArgsConstructor
public class ImportTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String code; // Mã Phiếu (PN260301)

    private LocalDateTime importDate; // Ngày Nhập

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier; // Nhà Cung Cấp

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee createdBy; // Người Lập

    private BigDecimal totalAmount; // Tổng Tiền
    private BigDecimal paidAmount; // Đã Trả
    private BigDecimal debtAmount; // Công Nợ

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TicketStatus status; // Đã thanh toán, Ghi nợ, Hủy
}
