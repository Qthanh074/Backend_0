package org.example.backend9.entity.sales;

import org.example.backend9.entity.core.Employee;
import org.example.backend9.entity.core.Store;
import org.example.backend9.enums.OrderStatus;
import org.example.backend9.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data @NoArgsConstructor @AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String orderNumber; // Số HĐ (VD: HD260301, WEB001)

    @Column(length = 20, nullable = false)
    private String orderType; // "RETAIL" (Bán lẻ tại quầy) hoặc "ONLINE" (TMĐT)

    // LIÊN KẾT
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store; // Chi nhánh bán

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer; // Khách hàng (Null = Khách lẻ)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee; // Nhân viên bán / duyệt đơn

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    private Promotion promotion; // Mã KM áp dụng (nếu có)

    // THÔNG TIN THANH TOÁN
    private BigDecimal subtotal = BigDecimal.ZERO; // Tạm tính
    private BigDecimal discountAmount = BigDecimal.ZERO; // Tiền giảm giá
    private BigDecimal totalAmount = BigDecimal.ZERO; // Khách phải trả (Thực thu)

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // DÀNH RIÊNG CHO ĐƠN ONLINE (Có thể null nếu là đơn RETAIL)
    private String salesChannel; // Kênh bán: Website, Shopee, Facebook
    private String shippingPartner; // ĐV Vận chuyển: GHTK, SPX
    private BigDecimal shippingFee = BigDecimal.ZERO; // Phí ship

    // DÀNH CHO ĐƠN HỦY
    private String cancelReason;
    private LocalDateTime cancelledAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancelled_by")
    private Employee cancelledBy;

    // Chi tiết các sản phẩm trong đơn (Quan hệ 1-N)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails = new ArrayList<>();
}