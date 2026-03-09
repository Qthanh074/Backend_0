package org.example.backend9.entity;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.example.backend9.entity.core.Employee;
@Entity
@Table(name = "activity_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Log sinh ra rất nhiều nên dùng Long thay vì Integer

    // User thực hiện hành động
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Employee user;

    // Hành động: VD: CREATE_ORDER, DELETE_PRODUCT...
    @Column(nullable = false, length = 50)
    private String action;

    // Tác động lên cái gì: VD: Order, Product, User...
    @Column(nullable = false, length = 50)
    private String entityType;

    // ID của cái bị tác động: VD: Xóa đơn hàng số "HD001" thì lưu HD001 vào đây
    @Column(length = 50)
    private String entityId;

    // LƯU JSON CHI TIẾT
    // Dùng để lưu vết. VD: Sửa giá từ 50k lên 100k -> Lưu JSON: {"old_price": 50000, "new_price": 100000}
    // Ở UI của bạn, cái này sẽ hiện ra khi hover (chỉ chuột) vào chữ "Chi tiết"
    @Column(columnDefinition = "JSON")
    private String details;

    // THÔNG TIN TRUY VẾT MẠNG (Tùy chọn, rất hữu ích để chống hack)
    @Column(length = 45)
    private String ipAddress; // IP của người dùng (VD: 192.168.1.1)

    @Column(length = 255)
    private String userAgent; // Trình duyệt đang dùng (Chrome, Safari...)

    // Thời gian xảy ra
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
