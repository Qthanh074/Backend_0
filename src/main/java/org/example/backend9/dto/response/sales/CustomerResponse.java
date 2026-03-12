package org.example.backend9.dto.response.sales;

import lombok.Builder;
import lombok.Data;
import org.example.backend9.entity.sales.Customer;
import org.example.backend9.enums.CustomerTier;

import java.math.BigDecimal;

@Data
@Builder
public class CustomerResponse {
    private Integer id;
    private String code;
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private CustomerTier tier;
    private Integer currentPoints;
    private BigDecimal totalSpent;
    private Boolean canPlaceOrder;

    // Hàm tiện ích map từ Entity sang DTO
    public static CustomerResponse fromEntity(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .code(customer.getCode())
                .fullName(customer.getFullName())
                .phone(customer.getPhone())
                .email(customer.getEmail())
                .address(customer.getAddress())
                .tier(customer.getTier())
                .currentPoints(customer.getCurrentPoints())
                .totalSpent(customer.getTotalSpent())
                .canPlaceOrder(customer.getCanPlaceOrder())
                .build();
    }
}