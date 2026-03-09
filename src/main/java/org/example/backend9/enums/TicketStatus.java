package org.example.backend9.enums;

public enum TicketStatus {
    DRAFT,          // Nháp
    PENDING,        // Chờ duyệt / Đang xử lý
    IN_TRANSIT,     // Đang đi đường (dùng cho Chuyển kho)
    COMPLETED,      // Hoàn thành / Đã nhận / Đã thanh toán
    CANCELLED,      // Đã hủy
    DEBT            // Ghi nợ (dùng cho Nhập kho chưa trả hết tiền)
}
