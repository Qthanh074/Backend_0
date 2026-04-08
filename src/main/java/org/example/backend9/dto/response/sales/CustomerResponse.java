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
    private Integer areaId;
    private String areaName;
    private BigDecimal totalSpending;
    private String rank;

    public static CustomerResponse fromEntity(Customer customer) {
        String calculatedRank = "Đồng";
        if (customer.getTotalSpending() != null) {
            double spending = customer.getTotalSpending().doubleValue();
            if (spending >= 10000000) calculatedRank = "Vàng";
            else if (spending >= 2000000) calculatedRank = "Bạc";
        }

        return CustomerResponse.builder()
                .id(customer.getId())
                .code(customer.getCode())
                .fullName(customer.getFullName())
                .phone(customer.getPhone())
                .email(customer.getEmail())
                .address(customer.getAddress())
                .canPlaceOrder(customer.getCanPlaceOrder())
                .totalSpending(customer.getTotalSpending())
                .rank(calculatedRank) // 🟢 Gán hạng vừa tính được vào đây
                .areaName(customer.getArea() != null ? customer.getArea().getName() : null)
                .build();
    }
}