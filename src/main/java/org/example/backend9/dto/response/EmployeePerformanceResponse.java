package org.example.backend9.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder; // Thêm Builder để Service dùng cho tiện
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeePerformanceResponse {
    private Integer employeeId;      // ID để React xử lý
    private String employeeCode;    // Mã NV (NV01, NV02...)
    private String fullName;        // Tên nhân viên
    private String storeName;       // Chi nhánh (Hà Nội, HCM...)
    private Long orderCount;        // Số đơn hàng
    private BigDecimal totalRevenue; // Doanh thu thực tế
    private BigDecimal kpiAmount;    // Chỉ tiêu KPI (Để vẽ cái thanh Progress)

    // Thuộc tính tính toán nhanh cho Frontend
    private Double completionRate;  // Tỉ lệ hoàn thành (%)
    private String rank;            // Xếp loại (Xuất sắc, Khá, Cần cố gắng)
}