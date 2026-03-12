package org.example.backend9.dto.request.inventory;

import lombok.Data;
import org.example.backend9.enums.EntityStatus;

@Data
public class CategoryRequest {
    private String name;
    private String description;
    private Long parentId;
    private EntityStatus status;
}