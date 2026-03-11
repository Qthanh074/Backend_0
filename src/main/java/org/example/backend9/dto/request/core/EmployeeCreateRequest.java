package org.example.backend9.dto.request.core;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class EmployeeCreateRequest {
    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0[3|5|7|8|9])+([0-9]{8})$", message = "Số điện thoại không đúng định dạng")
    private String phone;

    @NotBlank(message = "Quyền (Role) không được để trống")
    private String role; // VD: MANAGER, CASHIER

    private Integer storeId; // Có thể null
}