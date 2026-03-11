package org.example.backend9.dto.response.core;

import lombok.Data;
import org.example.backend9.enums.EntityStatus;

@Data
public class EmployeeResponse {
    private Integer id;
    private String code;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private EntityStatus status;

    // Chỉ trả về thông tin cơ bản của cửa hàng (tránh lồng ghép quá sâu)
    private String storeName;
}