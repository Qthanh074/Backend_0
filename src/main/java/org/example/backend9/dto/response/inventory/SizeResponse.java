package org.example.backend9.dto.response.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backend9.enums.EntityStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SizeResponse {
    private Long id;
    private String name;
    private String description;
    private EntityStatus status;
}