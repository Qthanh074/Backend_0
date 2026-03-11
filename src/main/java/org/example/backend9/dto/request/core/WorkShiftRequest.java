package org.example.backend9.dto.request.core;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class WorkShiftRequest {
    @NotNull(message = "ID Cửa hàng không được để trống")
    private Integer storeId;

    @NotNull(message = "ID Nhân viên không được để trống")
    private Integer employeeId;

    @NotNull(message = "Ngày làm việc không được để trống")
    private LocalDate shiftDate; // VD: 2026-03-09

    @NotNull(message = "Giờ bắt đầu không được để trống")
    private LocalTime startTime; // VD: 08:00

    // Giờ kết thúc có thể null khi nhân viên mới check-in, sẽ được cập nhật khi check-out
    private LocalTime endTime;

    private String notes;
}
