package org.example.backend9.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

/**
 * DTO nhận các tham số lọc từ giao diện Dashboard
 */
@Data
public class DashboardFilterRequest {

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate endDate;

    // Optional: Nếu client chỉ muốn xem một khu vực nhất định
    private Integer areaId;

    // Optional: Nếu client chỉ muốn xem một cửa hàng nhất định
    private Integer storeId;
}