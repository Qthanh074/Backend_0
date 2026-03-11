package org.example.backend9.entity.inventory;

import org.example.backend9.entity.core.Employee;
import org.example.backend9.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "export_tickets")
@Data @NoArgsConstructor @AllArgsConstructor
public class ExportTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String code; // Mã Phiếu (PX260301)

    private LocalDateTime exportDate; // Ngày Xuất

    private String reason; // Lý do xuất (Xuất bán buôn, xuất hủy...)
    private String customerName; // Khách hàng / ĐV nhận (Có thể là Text tĩnh hoặc link với Customer)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee createdBy; // Người Lập

    private BigDecimal totalValue; // Tổng Giá Trị

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TicketStatus status; // Hoàn thành, Hủy
}