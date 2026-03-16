package org.example.backend9.dto.request.core;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.example.backend9.enums.EntityStatus; // Nhớ chú ý dòng import này nhé

@Data
public class AreaRequest {
    @NotBlank(message = "Mã khu vực không được để trống")
    private String code; // VD: HCM, HN

    @NotBlank(message = "Tên khu vực không được để trống")
    private String name;

    private String description;

    // 👉 THÊM BIẾN NÀY ĐỂ HỨNG TRẠNG THÁI TỪ FRONTEND
    private EntityStatus status;
}