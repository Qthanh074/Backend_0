package org.example.backend9.entity.inventory;

import org.example.backend9.entity.core.Employee;
import org.example.backend9.entity.core.Store;
import org.example.backend9.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "transfer_tickets")
@Data @NoArgsConstructor @AllArgsConstructor
public class TransferTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String code; // Mã Phiếu (CK260301)

    private LocalDateTime transferDate; // Ngày Chuyển

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_store_id", nullable = false)
    private Store fromStore; // Từ Kho (Xuất)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_store_id", nullable = false)
    private Store toStore; // Đến Kho (Nhận)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee createdBy; // Người Lập

    private Integer totalQuantity; // Tổng SL Hàng

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TicketStatus status; // Đang đi đường (IN_TRANSIT), Đã nhận (COMPLETED)
}