package org.example.backend9.dto.request.core;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;


@Data
public class EmployeeUpdateRequest {
    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Số điện thoại không đúng định dạng")
    private String phone;

    @NotBlank(message = "Quyền (Role) không được để trống")
    private String role;

    private Integer storeId;

    // 👉 Dùng để đổi mật khẩu nếu cần
    private String password;
    private Boolean isActive;
}