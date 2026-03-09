package org.example.backend9.entity.inventory;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend9.entity.inventory.ProductVariant;
@Entity
@Table(name = "transfer_ticket_details")
@Data @NoArgsConstructor @AllArgsConstructor
public class TransferTicketDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Liên kết về phiếu chuyển kho tổng
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_ticket_id", nullable = false)
    private TransferTicket transferTicket;

    // Chuyển sản phẩm (biến thể) nào
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @Column(nullable = false)
    private Integer quantity; // Số lượng chuyển đi
}