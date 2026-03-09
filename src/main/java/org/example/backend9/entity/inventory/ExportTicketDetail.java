package org.example.backend9.entity.inventory;



import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import org.example.backend9.entity.inventory.ProductVariant;

    @Entity
    @Table(name = "export_ticket_details")
    @Data @NoArgsConstructor @AllArgsConstructor
    public class ExportTicketDetail {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;

        // Liên kết về phiếu xuất tổng
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "export_ticket_id", nullable = false)
        private ExportTicket exportTicket;

        // Xuất sản phẩm (biến thể) nào
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "product_variant_id", nullable = false)
        private ProductVariant productVariant;

        @Column(nullable = false)
        private Integer quantity; // Số lượng xuất

        private BigDecimal unitPrice; // Đơn giá xuất (dùng để tính tổng giá trị xuất)
    }

