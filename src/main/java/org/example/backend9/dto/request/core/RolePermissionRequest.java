package org.example.backend9.dto.request.core;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RolePermissionRequest {
    @NotBlank(message = "Tên quyền (Role) không được để trống")
    private String role; // VD: ADMIN, CASHIER

    // Các chuỗi này sẽ chứa dữ liệu JSON từ Frontend gửi lên
    private String globalSettings;
    private String dashboardSettings;
    private String menuPermissions;
}