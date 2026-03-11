package org.example.backend9.entity.inventory;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "return_ticket_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnTicketDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Nối với phiếu trả hàng tổng
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_ticket_id", nullable = false)
    private ReturnTicket returnTicket;

    // Khách trả lại mặt hàng (biến thể) nào
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @Column(nullable = false)
    private Integer returnQuantity; // Số lượng trả lại

    private BigDecimal returnPrice; // Đơn giá hoàn lại tiền

    private String conditionNote; // Tình trạng hàng (Lỗi, móp méo...)
}