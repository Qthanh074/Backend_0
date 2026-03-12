package org.example.backend9.dto.request.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backend9.enums.EntityStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColorRequest {
    private String name;
    private String hexCode; // Mã màu (VD: #FF0000)
    private EntityStatus status;
}