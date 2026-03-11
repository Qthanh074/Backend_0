package org.example.backend9.dto.response.core;

import lombok.Data;
import org.example.backend9.enums.EntityStatus;

@Data
public class SupplierResponse {
    private Integer id;
    private String code;
    private String name;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    private Double debt;
    private EntityStatus status;
}