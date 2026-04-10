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
    private BigDecimal totalSpent; // 🟢 Thống nhất dùng totalSpent
    private Boolean canPlaceOrder;
    private Integer areaId;
    private String areaName;
    private String rank;

    public static CustomerResponse fromEntity(Customer customer) {
        // Logic tính hạng thẻ tạm thời (Nên để Service xử lý để đồng bộ hơn)
        String calculatedRank = "Đồng";
        BigDecimal spendingAmount = customer.getTotalSpent() != null ? customer.getTotalSpent() : BigDecimal.ZERO;

        double spending = spendingAmount.doubleValue();
        if (spending >= 10000000) calculatedRank = "Vàng";
        else if (spending >= 2000000) calculatedRank = "Bạc";

        return CustomerResponse.builder()
                .id(customer.getId())
                .code(customer.getCode())
                .fullName(customer.getFullName())
                .phone(customer.getPhone())
                .email(customer.getEmail())
                .address(customer.getAddress())
                .tier(customer.getTier())
                .currentPoints(customer.getCurrentPoints())
                .totalSpent(spendingAmount) // 🟢 Sử dụng trường mới
                .canPlaceOrder(customer.getCanPlaceOrder())
                .areaId(customer.getArea() != null ? customer.getArea().getId() : null)
                .areaName(customer.getArea() != null ? customer.getArea().getName() : null)
                .rank(calculatedRank)
                .build();
    }
}