package org.example.backend9.entity.inventory;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "import_ticket_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportTicketDetail {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "import_ticket_id")
    private ImportTicket importTicket; // Thuộc phiếu nhập nào

    @ManyToOne
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant; // Nhập sản phẩm nào

    private Integer quantity; // Số lượng nhập
    private BigDecimal unitPrice; // Đơn giá nhập
}