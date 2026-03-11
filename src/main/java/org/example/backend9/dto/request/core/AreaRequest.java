package org.example.backend9.dto.request.core;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AreaRequest {
    @NotBlank(message = "Mã khu vực không được để trống")
    private String code; // VD: HCM, HN

    @NotBlank(message = "Tên khu vực không được để trống")
    private String name;

    private String description;
}