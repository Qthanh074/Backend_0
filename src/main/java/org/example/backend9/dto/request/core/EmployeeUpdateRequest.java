package org.example.backend9.dto.request.core;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class EmployeeUpdateRequest {
    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0[3|5|7|8|9])+([0-9]{8})$", message = "Số điện thoại không đúng định dạng")
    private String phone;

    @NotBlank(message = "Quyền (Role) không được để trống")
    private String role; // VD: ADMIN, MANAGER, CASHIER

    // Có thể null nếu nhân viên đó là Admin tổng (không thuộc cửa hàng cụ thể nào)
    private Integer storeId;
}