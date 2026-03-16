package org.example.backend9.dto.response.core;

import lombok.Data;
import org.example.backend9.enums.EntityStatus;

@Data
public class AreaResponse {
    private Integer id;
    private String code;
    private String name;
    private String description;
    private EntityStatus status;
    private Integer storeCount;
}