package org.example.backend9.dto.request.inventory;

import lombok.Data;
import org.example.backend9.enums.EntityStatus;

@Data
public class SizeRequest {
    private String name;
    private String description;
    private EntityStatus status;
}