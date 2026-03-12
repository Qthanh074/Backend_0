package org.example.backend9.dto.response.sales;

import lombok.Builder;
import lombok.Data;
import org.example.backend9.entity.sales.Promotion;
import org.example.backend9.enums.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class PromotionResponse {
    private Integer id;
    private String code;
    private String name;
    private String description;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minPurchase;
    private BigDecimal maxDiscount;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;

    public static PromotionResponse fromEntity(Promotion p) {
        return PromotionResponse.builder()
                .id(p.getId())
                .code(p.getCode())
                .name(p.getName())
                .description(p.getDescription())
                .discountType(p.getDiscountType())
                .discountValue(p.getDiscountValue())
                .minPurchase(p.getMinPurchase())
                .maxDiscount(p.getMaxDiscount())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .isActive(p.getIsActive())
                .build();
    }
}